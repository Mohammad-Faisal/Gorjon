package candor.fulki.chat.conversation;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
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

import candor.fulki.chat.ChatActivity;
import candor.fulki.chat.GetTimeAgo;
import candor.fulki.models.UserBasic;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;


public class ConversationFragment extends Fragment {



    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    RecyclerView vertical_recycler_view, horizontal_recycler_view;
    DatabaseReference mChatsDatabase, mFollowingsDatabase;
    private String mUserID;

    //----- CONTAINER -
    private ArrayList<Actives> horizontalList;
    private ArrayList<ChatBuddies> verticalList;
    private HorizontalAdapter horizontalAdapter;
    private VerticalAdapter verticalAdapter;


    public ConversationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_conversation, container, false);


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
                                basic.setmUserID(doc.getDocument().getString("user_id"));
                                basic.setmUserName(doc.getDocument().getString("user_name"));
                                basic.setmUserThumbImage(doc.getDocument().getString("thumb_image"));
                                horizontalList.add(new Actives(basic.getmUserID() , basic.getmUserName() , basic.getmUserThumbImage()));

                            }
                        }
                    }
                }
            }
        });



        //-- ETTING THE DATA WHO ARE ONLINE AMONG THOSE WHOM I FOLLOW -//
        /*horizontalList = new ArrayList<>();
        mFollowingsDatabase =  FirebaseDatabase.getInstance().getReference().child("followings").child(mUserID);
        mFollowingsDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot != null){
                    String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
                    String id = dataSnapshot.child("id").getValue().toString();
                    horizontalList.add(new Actives(id , name , thumb_image));
                    horizontalAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(getContext(), "Some error occurede !!!", Toast.LENGTH_SHORT).show();
                }
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
        });*/
        //mUsersDatabase  = FirebaseDatabase.getInstance().getReference().child("users");



        //----- FROM INTERNET -//
        vertical_recycler_view = mView.findViewById(R.id.vertical_recycler_view);
        horizontal_recycler_view = mView.findViewById(R.id.horizontal_recycler_view);


        horizontalAdapter = new HorizontalAdapter(horizontalList);
        verticalAdapter = new VerticalAdapter(verticalList);


        LinearLayoutManager verticalLayoutmanager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext() , 2);

        vertical_recycler_view.setLayoutManager(verticalLayoutmanager);

        LinearLayoutManager horizontalLayoutManagaer
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        horizontal_recycler_view.setLayoutManager(horizontalLayoutManagaer);

        vertical_recycler_view.setAdapter(verticalAdapter);
        horizontal_recycler_view.setAdapter(horizontalAdapter);





        return mView;
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
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chats_horizontal, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.chats_actives_name.setText(horizontalList.get(position).getName());

            String ImageUrl = horizontalList.get(position).getThumb_image();
            if (ImageUrl == null) {

            } else {
                Picasso.with(getContext()).load(ImageUrl).placeholder(R.drawable.ic_blank_profile).into(holder.chats_actives_image);
            }
            final String mUserID = horizontalList.get(position).getUserID();
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                    chatIntent.putExtra("user_id", mUserID);
                    startActivity(chatIntent);
                }
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
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chats_vertical, parent, false);
            return new MyViewHolder(itemView);
        }
        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            String message = verticalList.get(position).getLast_message();
            String upToNCharacters = message.substring(0, Math.min(message.length(), 30));
            if(message.length() >30)upToNCharacters = upToNCharacters+"...";

            holder.chats_hor_message.setText(upToNCharacters);
            holder.chats_hor_name.setText(verticalList.get(position).getUser_name());

            ////SETTING TIME AGO OF THE NOTIFICATION -//
            GetTimeAgo ob = new GetTimeAgo();
            long time = verticalList.get(position).getTime_stamp();
            String time_ago = GetTimeAgo.getTimeAgo(time ,getContext());
            holder.chats_hor_time.setText(time_ago);

            String ImageUrl = verticalList.get(position).getThumb_image_url();
            if (ImageUrl == null) {

            } else {
                Picasso.with(getContext()).load(ImageUrl).placeholder(R.drawable.ic_blank_profile).into(holder.chats_hor_image);
            }
            final String mID = verticalList.get(position).getUser_id();
            Log.d("Inbox Activity ", "onBindViewHolder: found user id"+mID);
            holder.itemView.setOnClickListener(view -> {
                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
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
