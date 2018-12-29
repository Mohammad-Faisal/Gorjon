package candor.fulki.activities.chat;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import candor.fulki.adapters.MessagesAdapter;
import candor.fulki.models.MeetingRooms;
import candor.fulki.models.Messages;
import candor.fulki.R;
import candor.fulki.utils.PreferenceManager;
import de.hdodenhof.circleimageview.CircleImageView;

public class MeetingActivity extends AppCompatActivity {


    private String mMeetingID;
    private static final String TAG = "Group chat ACTIVITY";


    //firebase
    private DatabaseReference mCurrentUserDatabaseReference;
    private DatabaseReference mOtherUserDatabaseReference;
    private DatabaseReference mMessageReference;
    private DatabaseReference mRootRef;
    private FirebaseUser mUser;

    //widgets
    private Toolbar mToolbar;
    private TextView mDisplayName , mLastSeen ;
    private CircleImageView mImageIconTop;
    private ImageButton mAddContent , mSendMessage;
    private EditText mEditMessage;
    private Button mGroupAdd , mGroupLeave;

    private SwipeRefreshLayout mSwipeRefreshlayout;


    //variables
    private String mUserID , mUserName;


    //containers
    private RecyclerView mMessageList;
    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessagesAdapter mMessagesAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private int itemPosition = 0;
    private String lastkey = "";
    private  String prevLastkey = "";

    PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);


        mMeetingID = getIntent().getStringExtra("meetingID");

        preferenceManager = new PreferenceManager(this);
        //firebase
        mUserID =preferenceManager.getUserId();
        mUserName = preferenceManager.getUserName();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mMessageReference = FirebaseDatabase.getInstance().getReference().child("meeting_messages");


        //----------  LOADING MESSAGES INTO RECYCLER VIEW --------- //
        mMessagesAdapter = new MessagesAdapter(messageList , this);
        mMessageList = findViewById(R.id.group_message_list);
        mLinearLayout = new LinearLayoutManager(this);
        mMessageList.hasFixedSize();
        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.setAdapter(mMessagesAdapter);



        mSendMessage  = findViewById(R.id.group_chat_send_message);
        mEditMessage = findViewById(R.id.group_chat_write_message);
        mSwipeRefreshlayout = findViewById(R.id.group_swipe_message_layout);
        loadMessages();


        //-- SETTING TITLE FOR THE TEXT VIEW -//
        final TextView mTitleTextView = findViewById(R.id.group_chat_title);
        mRootRef.child("meetings").child(mMeetingID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = dataSnapshot.child("title").getValue().toString();
                getSupportActionBar().setTitle(title);
                getSupportActionBar().setHomeButtonEnabled(true);
                mTitleTextView.setText(title);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //creating a new firebase entry for current chat if it doesnt exist or loading the previous ones
        mRootRef.child("meeting_messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(mMeetingID)){

                }
                else{  //jehetu age chat hoynai tai amra eikhane ekta notun entry diye ditesi
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("message" , "");
                    chatAddMap.put("seen" , false);
                    chatAddMap.put("timestamp" , ServerValue.TIMESTAMP);
                    chatAddMap.put("type" , "text");
                    chatAddMap.put("from" , mUserID);
                    Map chatUserMap = new HashMap();
                    chatUserMap.put("meeting_messages/"+mMeetingID+"/"+mMeetingID , chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError !=null ){
                                Toast.makeText(MeetingActivity.this, "there was an error "+ databaseError.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //chat er add button er kaj kortesi
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendmessage();
            }
        });


        //------------- REFRESHING THE ITEMS----------//
        mSwipeRefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPosition = 0;
                loadMoreMessages();
            }
        });

        mGroupAdd = findViewById(R.id.group_chat_add_people);
        mGroupLeave = findViewById(R.id.group_chat_leave);

        mGroupAdd.setOnClickListener(view -> mRootRef.child("meetings").child(mMeetingID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MeetingRooms meetingRooms = dataSnapshot.getValue(MeetingRooms.class);
                Intent invtePeopleIntent = new Intent(MeetingActivity.this , InvitePeopleToMeetingActivity.class );
                invtePeopleIntent.putExtra("meetingID" , meetingRooms);
                startActivity(invtePeopleIntent);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }));

        mGroupLeave.setOnClickListener(view -> {
            Map inviteMap = new HashMap();
            inviteMap.put("person_meeting/"+mUserID+"/"+mMeetingID , null);
            inviteMap.put("meeting_person/"+mMeetingID+"/"+mUserID , null);
            //number_of_person = number_of_person -1;
            //String t = String.valueOf(number_of_person);
            //inviteMap.put("meetings/"+mMeetingID+"/number_of_person" , t );
            inviteMap.put("notifications/"+mUserID+"/"+mMeetingID , null);
            mRootRef.updateChildren(inviteMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    finish();
                }
            });

        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_meeting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meeting_add:
                mRootRef.child("meetings").child(mMeetingID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        MeetingRooms meetingRooms = dataSnapshot.getValue(MeetingRooms.class);
                        Intent invtePeopleIntent = new Intent(MeetingActivity.this , InvitePeopleToMeetingActivity.class );
                        invtePeopleIntent.putExtra("meetingID" , meetingRooms);
                        startActivity(invtePeopleIntent);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                return true;
            case R.id.meeting_leave:


                Map inviteMap = new HashMap();
                inviteMap.put("person_meeting/"+mUserID+"/"+mMeetingID , null);
                inviteMap.put("meeting_person/"+mMeetingID+"/"+mUserID , null);
                //number_of_person = number_of_person -1;
                //String t = String.valueOf(number_of_person);
                //inviteMap.put("meetings/"+mMeetingID+"/number_of_person" , t );
                inviteMap.put("notifications/"+mUserID+"/"+mMeetingID , null);
                mRootRef.updateChildren(inviteMap, (databaseError, databaseReference) -> finish());

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void loadMoreMessages() {

        DatabaseReference messageQueryRef = mRootRef.child("meeting_messages").child(mMeetingID);
        Query messageQuery = messageQueryRef.orderByKey().endAt(lastkey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                if(!prevLastkey.equals(messageKey)){
                    messageList.add(itemPosition++ , message);
                }
                else {
                    prevLastkey = lastkey;
                }
                if(itemPosition==1){
                    lastkey = dataSnapshot.getKey();
                }

                mMessagesAdapter.notifyDataSetChanged();
                mLinearLayout.scrollToPositionWithOffset(10,0);
                mSwipeRefreshlayout.setRefreshing(false);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadMessages() {

        DatabaseReference messageQueryRef = mRootRef.child("meeting_messages").child(mMeetingID);
        Query messageQuery = messageQueryRef.limitToLast(mCurrentPage* TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                itemPosition++;
                if(itemPosition==1){
                    lastkey = dataSnapshot.getKey();
                    prevLastkey = lastkey;
                }
                Messages message = dataSnapshot.getValue(Messages.class);
                messageList.add(message);
                mMessagesAdapter.notifyDataSetChanged();
                mMessageList.scrollToPosition(messageList.size()-1); // ei line ta dile page e dhukei niche cole jabe
                mSwipeRefreshlayout.setRefreshing(false);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void sendmessage() {
        String message = mEditMessage.getText().toString();
        if(!TextUtils.isEmpty(message)){

            Log.d(TAG , message);
            String current_user_ref = "meeting_messages/"+mMeetingID;
            Map newMessageMap = new HashMap();
            newMessageMap.put("message" , message);
            newMessageMap.put("seen" , false);
            newMessageMap.put("type" , "text");
            newMessageMap.put("time" , ServerValue.TIMESTAMP);
            newMessageMap.put("from" , mUserID);

            DatabaseReference user_message_push = mRootRef.child("messages").child(mMeetingID).push();
            String pushID = user_message_push.getKey();

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + pushID , newMessageMap);
            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d( TAG , databaseError.getMessage().toString());
                    }
                }
            });
            mEditMessage.setText("");

        }

    }
}
