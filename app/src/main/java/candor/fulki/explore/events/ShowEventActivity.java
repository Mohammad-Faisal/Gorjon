package candor.fulki.explore.events;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import candor.fulki.general.MainActivity;
import candor.fulki.models.Comments;
import candor.fulki.home.PostCommentAdapter;
import candor.fulki.models.Ratings;
import candor.fulki.models.Events;
import candor.fulki.models.Joins;
import candor.fulki.models.Notifications;
import candor.fulki.profile.ShowPleopleListActivity;
import candor.fulki.R;
import candor.fulki.utils.PreferenceManager;

public class ShowEventActivity extends AppCompatActivity {


    private static final String TAG = "ShowEventActivity";

    ImageView eventImage , ownImage;
    TextView eventTitle , eventDate , eventLocation, eventPeopleCnt , eventDescription , eventCommentCnt;
    EditText eventComment;
    ImageButton eventCommentPost;
    Button eventJoinButton;

    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();


    String eventID ;
    String moderatorID;
    long peopleCount;
    int joinState = 0;
    String mUserID ;
    String mUserName;
    String mUserImage;
    String mUserThumbImage;

    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_event);
        eventID = getIntent().getStringExtra("event_id");
        Objects.requireNonNull(getSupportActionBar()).setTitle("Event");
        getSupportActionBar().setHomeButtonEnabled(true);

        preferenceManager = new PreferenceManager(this);

        eventImage = findViewById(R.id.show_event_image);
        eventDate = findViewById(R.id.show_event_date);
        eventPeopleCnt = findViewById(R.id.show_event_people_cnt);
        eventLocation = findViewById(R.id.show_event_locaiton);
        eventTitle = findViewById(R.id.show_event_title);
        eventComment = findViewById(R.id.show_event_comment_text);
        eventCommentPost = findViewById(R.id.show_event_post_comment);
        eventJoinButton = findViewById(R.id.show_event_join_btn);
        eventDescription = findViewById(R.id.show_event_description);
        eventCommentCnt = findViewById(R.id.show_event_comment_cnt);
        ownImage = findViewById(R.id.show_event_own_image);


        mUserID = preferenceManager.getUserId();
        mUserImage = preferenceManager.getUserImage();
        mUserThumbImage = preferenceManager.getUserThumbImage();
        mUserName = preferenceManager.getUserName();

        setImage(mUserThumbImage , ownImage);


        LinearLayout mLinear = findViewById(R.id.event_people_list_linear);
        mLinear.setOnClickListener(v -> {
            addRating(mUserID , 1);
            Intent showPeopleIntent = new Intent(ShowEventActivity.this , ShowPleopleListActivity.class);
            showPeopleIntent.putExtra("type" , "joins");
            showPeopleIntent.putExtra("user_id" ,eventID );
            startActivity(showPeopleIntent);
        });

        setImage(mUserThumbImage , ownImage);
        setDetails();
        setJoinButtonState();
        loadComments();
        eventJoinButton.setOnClickListener(v -> {

            Log.d(TAG, "onCreate:  join button clicked !!!!!!");
            if(joinState == 1){
                eventJoinButton.setEnabled(false);
                cancelJoin();
            }else{
                eventJoinButton.setEnabled(false);
                join();
            }
        });
        eventCommentPost.setOnClickListener(v -> postComment());
    }

    public void postComment(){

        addRating(mUserID , 5);
        addRating(moderatorID , 2);

        String time_stamp = String.valueOf(new Date().getTime());

        DocumentReference ref = FirebaseFirestore.getInstance().collection("notifications/"+moderatorID+"/notificatinos").document();
        String commentNotificatoinPushID = ref.getId();

        DocumentReference commentRef = FirebaseFirestore.getInstance().collection("comments/"+eventID+"/comments").document();
        String commentID = commentRef.getId();

        Notifications pushNoti = new Notifications( "comment_event" ,mUserID ,moderatorID, eventID ,commentNotificatoinPushID , time_stamp,"n"  );
        Comments  comment =  new Comments(eventComment.getText().toString() , mUserID ,commentID, eventID  , commentNotificatoinPushID  , time_stamp);
        eventComment.setText("");


        WriteBatch writeBatch  = firebaseFirestore.batch();
        DocumentReference notificatinoRef = firebaseFirestore.collection("notifications/"+moderatorID+"/notificatinos").document(commentNotificatoinPushID);
        writeBatch.set(notificatinoRef, pushNoti);
        DocumentReference reff = firebaseFirestore.collection("comments/"+eventID+"/comments").document(commentID);
        writeBatch.set(reff, comment);
        writeBatch.commit().addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess:  comment is done succesfully !")).addOnFailureListener(e -> Log.e(TAG, "onFailure:  some error  : ",e.getCause() ));

    }

    public void loadComments(){

        final RecyclerView mCommentList;
        final List<Comments> commentList = new ArrayList<>();
        LinearLayoutManager mLinearLayout;

        //--------- SETTING THE COMMENT ADAPTERS --//
        final PostCommentAdapter mPostCommentAdapter = new PostCommentAdapter(commentList, this , ShowEventActivity.this);
        mCommentList = findViewById(R.id.show_event_recycler);
        mLinearLayout = new LinearLayoutManager(this);
        mCommentList.hasFixedSize();
        mCommentList.setLayoutManager(mLinearLayout);
        mCommentList.setAdapter(mPostCommentAdapter);


        //-------------LOADING COMMENTS------------//
        firebaseFirestore = FirebaseFirestore.getInstance();
        Query nextQuery = firebaseFirestore.collection("comments/"+eventID+"/comments").orderBy("time_stamp" , Query.Direction.DESCENDING);
        nextQuery.addSnapshotListener((documentSnapshots, e) -> {
            for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                if(doc.getType() == DocumentChange.Type.ADDED){
                    Comments singleComment = doc.getDocument().toObject(Comments.class);
                    commentList.add(singleComment);
                    mPostCommentAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void join(){



        String time_stamp = String.valueOf(new Date().getTime());
        DocumentReference ref = FirebaseFirestore.getInstance().collection("notifications/"+moderatorID+"/notificatinos").document();
        String joinNotificatoinPushID = ref.getId();
        Joins mJoins = new Joins(mUserID , mUserName  , mUserThumbImage , joinNotificatoinPushID ,time_stamp);
        Notifications pushNoti = new Notifications( "join" ,mUserID ,moderatorID , eventID ,joinNotificatoinPushID , time_stamp,"n"  );

        WriteBatch writeBatch  = firebaseFirestore.batch();
        DocumentReference notificatinoRef = firebaseFirestore.collection("notifications/"+moderatorID+"/notificatinos").document(joinNotificatoinPushID);
        writeBatch.set(notificatinoRef, pushNoti);
        DocumentReference joinRef = firebaseFirestore.collection("joins/"+eventID+"/joins").document(mUserID);
        writeBatch.set(joinRef, mJoins);

        Log.d(TAG, "join:  join notification   "+ joinNotificatoinPushID);
        Log.d(TAG, "cancelJoin: event id    " +eventID);
        writeBatch.commit().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "onComplete: success join !");
                joinState=1;
                joinState = 1;
                eventJoinButton.setText("Joined");
                eventJoinButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                eventJoinButton.setEnabled(true);
                addRating(mUserID , 15);
                addRating(moderatorID , 3);
                addPeople(eventID , 1);
            }
        }).addOnFailureListener(e -> {
            eventJoinButton.setEnabled(true);
            Toast.makeText(ShowEventActivity.this, "Some error occured", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onFailure: join is not successful   : ",e.getCause() );
        });
    }

    public void cancelJoin(){
        firebaseFirestore.collection("joins/" + eventID + "/joins").document(mUserID).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(task.getResult().exists()){
                    String joinNotificatoinPushID = task.getResult().getString("notificationID");
                    WriteBatch writeBatch  = firebaseFirestore.batch();
                    if(joinNotificatoinPushID!=null){
                        eventJoinButton.setEnabled(true);
                        DocumentReference notificatinoRef = firebaseFirestore.collection("notifications/"+moderatorID+"/notificatinos").document(joinNotificatoinPushID);
                        writeBatch.delete(notificatinoRef);
                    }
                    DocumentReference joinRef = firebaseFirestore.collection("joins/"+eventID+"/joins").document(mUserID);
                    writeBatch.delete(joinRef);
                    writeBatch.commit().addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            Log.d(TAG, "onComplete:  join cancel is successful");
                            joinState=0;
                            joinState = 0;
                            eventJoinButton.setText("Join");
                            addPeople(eventID, -1);
                            addRating(mUserID , -15);
                            addRating(moderatorID , -3);
                            eventJoinButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryy));
                            eventJoinButton.setEnabled(true);
                        }
                    }).addOnFailureListener(e -> {
                        eventJoinButton.setEnabled(true);
                        Log.e(TAG, "onFailure: join cancel is not successful   :   ",e.getCause() );
                    });
                }else{
                    eventJoinButton.setEnabled(true);
                }
            }else{
                eventJoinButton.setEnabled(true);
            }
        });
        eventJoinButton.setEnabled(true);
    }

    public void setDetails(){

        /*FirebaseFirestore.getInstance().collection("joins/" + eventID + "/joins").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()) {
                    int count = documentSnapshots.size();
                    eventPeopleCnt.setText(""+count+" people");
                } else {
                    eventPeopleCnt.setText(""+"0"+" people");
                }
            }
        });*/


        FirebaseFirestore.getInstance().collection("comments/" + eventID + "/comments").addSnapshotListener((documentSnapshots, e) -> {
            if (!documentSnapshots.isEmpty()) {
                int count = documentSnapshots.size();
                eventCommentCnt.setText(""+count+" comments");
            } else {
                eventCommentCnt.setText(""+"0"+" comment");
            }
        });

        firebaseFirestore.collection("events").document(eventID).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(task.getResult().exists()){
                    Events events = task.getResult().toObject(Events.class);

                    eventDate.setText(events.getTime_and_date());
                    eventLocation.setText(events.getLocation());
                    eventTitle.setText(events.getTitle());
                    eventDescription.setText(events.getDescription());
                    moderatorID = events.getModerator_id();
                    peopleCount = events.getPeople_cnt();

                    setPeopleCount(peopleCount);
                    setImage(events.getImage_url() , eventImage);
                }
            }
        });
    }

    public void setJoinButtonState(){

        //setting the current state of like button
        FirebaseFirestore.getInstance().collection("joins/" + eventID + "/joins").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        joinState = 1;
                        eventJoinButton.setText("Joined");
                        eventJoinButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    } else {
                        joinState = 0;
                        eventJoinButton.setText("Join");
                        eventJoinButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryy));
                    }
                } else {
                    joinState = 0;
                    eventJoinButton.setText("Join");
                    eventJoinButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryy));
                }
            }
        });
    }

    public void setImage(String imageURl , ImageView  imageView){
        Picasso.with(ShowEventActivity.this).load(imageURl).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.ic_camera_icon).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                //do nothing if an image is found offline
            }
            @Override
            public void onError() {
                Picasso.with(ShowEventActivity.this).load(imageURl).placeholder(R.drawable.ic_blank_profile).into(imageView);
            }
        });
    }


    private Task<Void> addRating(String mUserID  , int factor) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Log.d(TAG, "addRating:   function calledd !!!!");
        final DocumentReference ratingRef = FirebaseFirestore.getInstance().collection("ratings")
                .document(mUserID);
        return firebaseFirestore.runTransaction(transaction -> {

            Ratings ratings = transaction.get(ratingRef)
                    .toObject(Ratings.class);
            long curRating = ratings.getRating();
            long nextRating = curRating + factor;

            ratings.setRating(nextRating);
            transaction.set(ratingRef, ratings);
            return null;
        });
    }

    private void setPeopleCount(long cnt){
        Log.d(TAG, "setPeopleCount:  called "+cnt);
        eventPeopleCnt.setText(cnt + " people");
    }

    private void addPeople( String eventID , int factor) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Log.d(TAG, "addPeople:   function calledd !!!!");
        final DocumentReference eventRef = FirebaseFirestore.getInstance().collection("events")
                .document(eventID);

        firebaseFirestore.runTransaction(transaction -> {
            Events event = transaction.get(eventRef)
                    .toObject(Events.class);
            long curPeople = event.getPeople_cnt();
            long nextPeople = curPeople + factor;

            Log.d(TAG, "addPeople:     join number is  "+nextPeople);
            HashMap< String ,  Object > updateMap = new HashMap<>();
            updateMap.put("people_cnt" , nextPeople);
            transaction.update(eventRef , updateMap);
            return nextPeople;
        }).addOnSuccessListener(aLong -> setPeopleCount(aLong));
    }
}
