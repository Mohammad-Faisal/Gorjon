package candor.fulki.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import candor.fulki.models.Actives;
import candor.fulki.models.ChatBuddies;
import candor.fulki.models.UserBasic;
import candor.fulki.R;
import candor.fulki.utils.GetTimeAgo;
import de.hdodenhof.circleimageview.CircleImageView;

public class InboxActivity extends AppCompatActivity {


    private static final String TAG = "InboxActivity";
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    RecyclerView vertical_recycler_view, horizontal_recycler_view;
    DatabaseReference mChatsDatabase, mFollowingsDatabase;
    private String mUserID  = FirebaseAuth.getInstance().getCurrentUser().getUid();



    private ArrayList<Actives> horizontalList = new ArrayList<>();
    private ArrayList<ChatBuddies> verticalList = new ArrayList<>();
    private HorizontalAdapter horizontalAdapter;
    private VerticalAdapter verticalAdapter;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        Log.d(TAG, "onCreate:    user id pf myself " + mUserID);

        FloatingActionButton inboxFloating;
        inboxFloating = findViewById(R.id.inbox_floating);
        inboxFloating.setVisibility(View.GONE);
        inboxFloating.setOnClickListener(v -> {
            Intent creatingMeetingIntent = new Intent(InboxActivity.this , CreateMeetingActivity.class);
            startActivity(creatingMeetingIntent);
        });


        Objects.requireNonNull(getSupportActionBar()).setTitle("  Inbox");



        //commented out code of tabs
        /*TabLayout tabLayout =  findViewById(R.id.inbox_tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Chats"));
        tabLayout.addTab(tabLayout.newTab().setText("Meetings"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = findViewById(R.id.inbox_view_pager);
        final InboxPagerAdapter adapter = new InboxPagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



*/


        mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //-- SETTING THE DATAS ABOUT MY CHATS
        mChatsDatabase = FirebaseDatabase.getInstance().getReference().child("chat_buddies").child(mUserID);
        verticalList = new ArrayList<>();
        mChatsDatabase.keepSynced(true);
        mChatsDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatBuddies chatBuddies = dataSnapshot.getValue(ChatBuddies.class);

                verticalList.add(chatBuddies);
                verticalAdapter.notifyDataSetChanged();
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


        firebaseFirestore.collection("followings").document(mUserID).collection("followings").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if(!documentSnapshots.isEmpty()){
                    if(!documentSnapshots.isEmpty()){
                        for(DocumentChange doc: documentSnapshots.getDocumentChanges()){

                            if(doc.getType() == DocumentChange.Type.ADDED){
                                UserBasic basic = new UserBasic();
                                Log.d(TAG, "onSuccess: found a followings  !!!");
                                basic.setmUserID(doc.getDocument().getString("id"));
                                basic.setmUserName(doc.getDocument().getString("name"));
                                basic.setmUserThumbImage(doc.getDocument().getString("thumb_image"));
                                horizontalList.add(new Actives(basic.getmUserID() , basic.getmUserName() , basic.getmUserThumbImage()));
                                horizontalAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        });






        //----- FROM INTERNET -//
        vertical_recycler_view = findViewById(R.id.vertical_recycler_view);
        horizontal_recycler_view = findViewById(R.id.horizontal_recycler_view);


        horizontalAdapter = new HorizontalAdapter(horizontalList);
        verticalAdapter = new VerticalAdapter(verticalList);


        LinearLayoutManager verticalLayoutmanager
                = new LinearLayoutManager(InboxActivity.this, LinearLayoutManager.VERTICAL, false);

        vertical_recycler_view.setLayoutManager(verticalLayoutmanager);

        LinearLayoutManager horizontalLayoutManagaer
                = new LinearLayoutManager(InboxActivity.this, LinearLayoutManager.HORIZONTAL, false);
        horizontal_recycler_view.setLayoutManager(horizontalLayoutManagaer);

        vertical_recycler_view.setAdapter(verticalAdapter);
        horizontal_recycler_view.setAdapter(horizontalAdapter);





    }



    public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.MyViewHolder> {

        private List<Actives> horizontalList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView chats_actives_name;
            public CircleImageView chats_actives_image;

            public MyViewHolder(View view) {
                super(view);
                chats_actives_name = view.findViewById(R.id.chats_item_horizontal_text);
                chats_actives_image = view.findViewById(R.id.chats_item_horizontal_image);
            }
        }


        public HorizontalAdapter(List<Actives> horizontalList) {
            this.horizontalList = horizontalList;
        }

        @Override
        public HorizontalAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chats_horizontal, parent, false);
            return new HorizontalAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final HorizontalAdapter.MyViewHolder holder, final int position) {
            Log.d(TAG, "onBindViewHolder: found a name "+horizontalList.get(position).getName());
            holder.chats_actives_name.setText(horizontalList.get(position).getName());

            String ImageUrl = horizontalList.get(position).getThumb_image();
            String listUserID = horizontalList.get(position).getUserID();

            if (ImageUrl == null) {

            } else {
                Picasso.with(InboxActivity.this).load(ImageUrl).placeholder(R.drawable.ic_blank_profile).into(holder.chats_actives_image);
            }
            holder.itemView.setOnClickListener(view -> {
                Intent chatIntent = new Intent(InboxActivity.this, ChatActivity.class);
                chatIntent.putExtra("user_id", listUserID);
                startActivity(chatIntent);
            });
        }

        @Override
        public int getItemCount() {
            return horizontalList.size();
        }
    }


    public class VerticalAdapter extends RecyclerView.Adapter<VerticalAdapter.MyViewHolder> {

        private List<ChatBuddies> verticalList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView chats_hor_name;
            public TextView chats_hor_message;
            public TextView chats_hor_time;
            public CircleImageView chats_hor_image;

            public MyViewHolder(View view) {
                super(view);
                chats_hor_message = view.findViewById(R.id.item_chats_text);
                chats_hor_name = view.findViewById(R.id.item_chats_user_name);
                chats_hor_time = view.findViewById(R.id.item_chats_time);
                chats_hor_image = view.findViewById(R.id.item_chats_image);

            }
        }
        public VerticalAdapter(List<ChatBuddies> verticalList) {
            this.verticalList = verticalList;
        }

        @Override
        public VerticalAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chats_vertical, parent, false);
            return new VerticalAdapter.MyViewHolder(itemView);
        }
        @Override
        public void onBindViewHolder(final VerticalAdapter.MyViewHolder holder, final int position) {

            String message = verticalList.get(position).getLast_message();
            String upToNCharacters = message.substring(0, Math.min(message.length(), 30));
            if(message.length() >30)upToNCharacters = upToNCharacters+"...";

            holder.chats_hor_message.setText(upToNCharacters);
            holder.chats_hor_name.setText(verticalList.get(position).getUser_name());

            ////SETTING TIME AGO OF THE NOTIFICATION -//
            GetTimeAgo ob = new GetTimeAgo();
            long time = verticalList.get(position).getTime_stamp();
            String time_ago = GetTimeAgo.getTimeAgo(time ,InboxActivity.this);
            holder.chats_hor_time.setText(time_ago);

            String ImageUrl = verticalList.get(position).getThumb_image_url();
            if (ImageUrl == null) {

            } else {
                Picasso.with(InboxActivity.this).load(ImageUrl).placeholder(R.drawable.ic_blank_profile).into(holder.chats_hor_image);
            }
            final String mID = verticalList.get(position).getUser_id();
            Log.d("Inbox Activity ", "onBindViewHolder: found user id"+mID);
            holder.itemView.setOnClickListener(view -> {
                Intent chatIntent = new Intent(InboxActivity.this, ChatActivity.class);
                chatIntent.putExtra("user_id", mID);
                startActivity(chatIntent);
            });
        }
        @Override
        public int getItemCount() {
            return verticalList.size();
        }
    }


}
