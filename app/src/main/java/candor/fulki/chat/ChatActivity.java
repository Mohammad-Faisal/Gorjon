package candor.fulki.chat;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import candor.fulki.models.ChatBuddies;
import candor.fulki.R;
import candor.fulki.models.Messages;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    private static final String TAG = "CHAT ACTIVITY";


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
    private SwipeRefreshLayout mSwipeRefreshlayout;


    //variables
    private String mOtherUseID;
    private String mCurrentUserID;
    private String mOtherUserName , mCurrentUserName;
    private String mOtherUserThumbImage , mCurrentUserThumbImage;

    //containers
    private RecyclerView mMessageList;
    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mMessageAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private int itemPosition = 0;
    private String lastkey = "";
    private  String prevLastkey = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        //setting up variables
        mOtherUseID = getIntent().getStringExtra("user_id");
        mOtherUserName = "default";
        mCurrentUserName = "default";


        if(mOtherUseID.equals(mCurrentUserID)){
            //i have to save a note
        }



        //firebase
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserID = mUser.getUid();  //je app e login kore ache
        mCurrentUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUserID);
        mOtherUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mOtherUseID);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mMessageReference = FirebaseDatabase.getInstance().getReference();
        //firebase initialize end


        //----------  LOADING MESSAGES INTO RECYCLER VIEW --------- //
        mMessageAdapter = new MessageAdapter(messageList , this);
        mMessageList = findViewById(R.id.message_list);
        mLinearLayout = new LinearLayoutManager(this);
        mMessageList.hasFixedSize();
        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.setAdapter(mMessageAdapter);


        /*//setting the toolbar
        mToolbar = findViewById(R.id.chat_activity_app_bar);
        setSupportActionBar(mToolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.toolbar_chat , null);
        actionbar.setCustomView(action_bar_view);
        //end ot setting toolbar*/



        // ----------- CUSTOM BAR ITEMS  -------------
        mDisplayName = findViewById(R.id.chat_toolbar_display_name);
        //mLastSeen = findViewById(R.id.last_seen_textVIew);
        mImageIconTop = findViewById(R.id.chat_top_image);

        //------- NORMAL WIDGETS ------------
        mSendMessage  = findViewById(R.id.chat_send_message);
        // mAddContent = findViewById(R.id.chat_add_content);
        mEditMessage = findViewById(R.id.chat_write_message);
        mSwipeRefreshlayout = findViewById(R.id.swipe_message_layout);
        loadMessages();




        //last seen feature add kora hoise
        //amra timestamp ta peye jabo firebase theke er pore amader kaj hobe sudhu etake use kora
        FirebaseFirestore.getInstance().collection("users").document(mOtherUseID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().exists()){


                        mOtherUserName = task.getResult().getString("name");
                        //String online = dataSnapshot.child("online").getValue().toString();
                        String online = "true";
                        String image_url =  task.getResult().getString("image");
                        mOtherUserThumbImage = task.getResult().getString("thumb_image");

                        if(online.equals("true")){
                            // mLastSeen.setText("active now");
                        }
                        else{
                            GetTimeAgo ob = new GetTimeAgo();
                            long time = Long.parseLong(online);
                            String time_ago = GetTimeAgo.getTimeAgo(time , getApplicationContext());
                            //mLastSeen.setText("last seen " + time_ago);
                        }

                        getSupportActionBar().setTitle(mOtherUserName);
                        mDisplayName.setText(mOtherUserName);

                    }
                } else {
                }
            }
        });



        FirebaseFirestore.getInstance().collection("users").document(mCurrentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().exists()){

                        final String currentUserThumbImage = task.getResult().getString("thumb_image");
                        mCurrentUserName = task.getResult().getString("name");
                        mCurrentUserThumbImage = currentUserThumbImage;

                        Picasso.with(ChatActivity.this).load(currentUserThumbImage).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.ic_blank_profile).into(mImageIconTop, new Callback() {
                            @Override
                            public void onSuccess(){
                                //do nothing if an image is found offline
                            }
                            @Override
                            public void onError(){
                                Picasso.with(ChatActivity.this).load(currentUserThumbImage).placeholder(R.drawable.ic_blank_profile).into(mImageIconTop);
                            }
                        });

                    }
                } else {
                }
            }
        });



        //creating a new firebase entry for current chat if it doesnt exist or loading the previous ones
        mRootRef.child("messages").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(mOtherUseID)){

                }
                else{  //jehetu age chat hoynai tai amra eikhane ekta notun entry diye ditesi
                    ChatBuddies mChatBuddiesCurrent = new ChatBuddies(mOtherUseID , mOtherUserName , mOtherUserThumbImage , "n" , "n" , "n" , 0);
                    ChatBuddies mChatBuddiesOther = new ChatBuddies(mCurrentUserID , mCurrentUserName , mCurrentUserThumbImage , "n" , "n" , "n" , 0);
                    mRootRef.child("chat_buddies").child(mCurrentUserID).child(mOtherUseID).setValue(mChatBuddiesCurrent);
                    mRootRef.child("chat_buddies").child(mOtherUseID).child(mCurrentUserID).setValue(mChatBuddiesOther);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //chat er add button er kaj kortesi
        mSendMessage.setOnClickListener(view -> sendmessage());


        //------------- REFRESHING THE ITEMS----------//
        mSwipeRefreshlayout.setOnRefreshListener(() -> {
            mCurrentPage++;
            itemPosition = 0;
            loadMoreMessages();
        });

    }



    private void loadMoreMessages() {

        DatabaseReference messageQueryRef = mRootRef.child("messages").child(mCurrentUserID).child(mOtherUseID);
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

                mMessageAdapter.notifyDataSetChanged();
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

        DatabaseReference messageQueryRef = mRootRef.child("messages").child(mCurrentUserID).child(mOtherUseID);
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
                mMessageAdapter.notifyDataSetChanged();
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
            String current_user_ref = "messages/"+mCurrentUserID+"/"+mOtherUseID;
            String other_user_ref = "messages/"+mOtherUseID+"/"+mCurrentUserID;

            Map newMessageMap = new HashMap();
            newMessageMap.put("message" , message);
            newMessageMap.put("seen" , false);
            newMessageMap.put("type" , "text");
            newMessageMap.put("time" , ServerValue.TIMESTAMP);
            newMessageMap.put("from" , mCurrentUserID);

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserID).child(mOtherUseID)
                    .push();
            String pushID = user_message_push.getKey();

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + pushID , newMessageMap);
            messageUserMap.put(other_user_ref+"/"+pushID, newMessageMap);
            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d( TAG , databaseError.getMessage().toString());
                    }
                }
            });
            mEditMessage.setText("");



           /* private String user_id;
            private String thumb_image_url;
            private String user_name;
            private String last_message;
            private String seen_status;
            private String last_message_from;
            private long time_stamp;*/

            Map newChatBuddiesMap = new HashMap();
            newChatBuddiesMap.put("user_id" , mOtherUseID);
            newChatBuddiesMap.put("thumb_image_url" , mOtherUserThumbImage);
            newChatBuddiesMap.put("user_name" , mOtherUserName);
            newChatBuddiesMap.put("last_message" , message);
            newChatBuddiesMap.put("seen_status" , "n");
            newChatBuddiesMap.put("last_message_from" , mCurrentUserID);
            newChatBuddiesMap.put("time_stamp" , ServerValue.TIMESTAMP);

            Map newChatBuddiesMap1 = new HashMap();
            newChatBuddiesMap1.put("user_id" , mCurrentUserID);
            newChatBuddiesMap1.put("thumb_image_url" , mCurrentUserThumbImage);
            newChatBuddiesMap1.put("user_name" , mOtherUserName);
            newChatBuddiesMap1.put("last_message" , message);
            newChatBuddiesMap1.put("seen_status" , "n");
            newChatBuddiesMap1.put("last_message_from" , mCurrentUserID);
            newChatBuddiesMap1.put("time_stamp" , ServerValue.TIMESTAMP);


            Map ChatBuddiesMap = new HashMap();
            ChatBuddiesMap.put("chat_buddies/"+mCurrentUserID+"/"+mOtherUseID , newChatBuddiesMap);
            ChatBuddiesMap.put("chat_buddies/"+mOtherUseID+"/"+mCurrentUserID , newChatBuddiesMap1);
            mRootRef.updateChildren(ChatBuddiesMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d( TAG , databaseError.getMessage().toString());
                    }
                }
            });
        }

    }

}
