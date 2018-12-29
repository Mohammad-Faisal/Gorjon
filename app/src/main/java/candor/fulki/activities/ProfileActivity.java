package candor.fulki.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import candor.fulki.adapters.PostsAdapter;
import candor.fulki.utils.Functions;
import candor.fulki.models.Posts;
import candor.fulki.models.Ratings;
import candor.fulki.models.Notifications;
import candor.fulki.R;
import candor.fulki.utils.PreferenceManager;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;


public class ProfileActivity extends AppCompatActivity {


    private static final String TAG = "ProfileActivity";
    private String mUserID = "" , mUserImage ,  mUserName ,  mUserThumbImage , mCurProfileId , mCurUserImage , mCurUserName , mCurUserTHumbImage;;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private PostsAdapter mPostsAdapter;
    private List<Posts> posts;
    private DocumentSnapshot lastVisible;
    private boolean isFirstPageLoad = false;
    private Button mProfileFollow;
    private TextView mProfileName , mProfileBio  , mProfileFollowersCnt , mProfileFollowingsCnt , mProfileSendMessage;
    private RecyclerView mProfilePostsRecycelr , mProfileBadgesRecycler;
    android.widget.LinearLayout cardFollowers , cardFollowings , sendMessage;
    CircleImageView mProfileImageView;

    boolean ownProfile = false;
    boolean followState = false;



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        loadDetails();
        initBottomNav();
        initViews();
        initRecycler();

        mCurProfileId = getIntent().getStringExtra("userID");
        ownProfile = mCurProfileId.equals(mUserID);

        if(ownProfile){
            mProfileSendMessage.setText("save a note");
        }else{
            mProfileSendMessage.setText("send message");
        }

        if(mCurProfileId!=null){
            setUpDetailsForCurrentUser();
        }

    }

    private void setUpDetailsForCurrentUser(){
        FirebaseFirestore.getInstance().collection("users").document(mCurProfileId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(Objects.requireNonNull(task.getResult()).exists()){
                    mCurUserName = task.getResult().getString("name");
                    mCurUserImage = task.getResult().getString("image");
                    mCurUserTHumbImage = task.getResult().getString("thumb_image");
                    String mUserBio = task.getResult().getString("bio");
                    if(mUserBio!=null){
                        mProfileBio.setText(mUserBio);
                    }
                    mProfileName.setText(mCurUserName);
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(mCurUserImage, mProfileImageView);
                    if(getSupportActionBar()!=null)getSupportActionBar().setTitle("  "+mCurUserName);
                }
            }
        });
        //setting the current state of follow button
        if(!ownProfile){
            FirebaseFirestore.getInstance().collection("followings/" + mUserID + "/followings").document(mCurProfileId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        followState = true;
                        mProfileFollow.setText("following");
                        mProfileFollow.setTextColor(getResources().getColor(R.color.colorPrimaryy));
                        mProfileFollow.setBackgroundColor(getResources().getColor(R.color.White));

                    } else {
                        followState = false;
                        mProfileFollow.setText("follow");
                        mProfileFollow.setTextColor(getResources().getColor(R.color.White));
                        mProfileFollow.setBackgroundColor(getResources().getColor(R.color.colorPrimaryy));
                    }
                } else {
                }
            });
        }else{
            mProfileFollow.setText("Edit Profile");
        }

        //follower and following count setting
        FirebaseFirestore.getInstance().collection("followings/" + mCurProfileId + "/followings").get().addOnCompleteListener(task -> {
            int followings_cnt = task.getResult().size();
            mProfileFollowingsCnt.setText(followings_cnt+" following");
        });
        FirebaseFirestore.getInstance().collection("followers/" + mCurProfileId + "/followers").get().addOnCompleteListener(task -> {
            int followers_cnt = task.getResult().size();
            if(followers_cnt<2)mProfileFollowersCnt.setText(""+followers_cnt+" follower");
            else mProfileFollowersCnt.setText(""+followers_cnt+" followers");
        });



        AsyncPostLoading runner = new AsyncPostLoading();
        runner.execute(1);

        mProfilePostsRecycelr.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                if(reachedBottom){
                    AsyncPostLoading runner = new AsyncPostLoading();
                    runner.execute(2);
                }
            }
        });
    }
    public void goToFollowersCount(View view) {
        addRating(mUserID , 1);
        Intent followersIntent = new Intent(ProfileActivity.this ,ShowPleopleListActivity.class);
        followersIntent.putExtra("type" , "followers");
        followersIntent.putExtra("user_id" , mCurProfileId);
        startActivity(followersIntent);
    }
    public void goToFollowingsCount(View view) {
        addRating(mUserID,1);
        Intent followersIntent = new Intent(ProfileActivity.this ,ShowPleopleListActivity.class);
        followersIntent.putExtra("type" , "followings");
        followersIntent.putExtra("user_id" , mCurProfileId);
        startActivity(followersIntent);
    }
    public void goToSendMessage(View view) {
        addRating(mUserID ,1);
        addRating(mCurProfileId , 1);
        Intent chatIntent = new Intent(ProfileActivity.this  , ChatActivity.class);
        chatIntent.putExtra("user_id" , mCurProfileId);
        startActivity(chatIntent);
    }
    public void followButtonCLickHandle(View view) {
        if(ownProfile){

            Intent showPostIntent = new Intent(ProfileActivity.this  , ProfileSettingsActivity.class);
            Pair< View , String > pair1 = Pair.create(findViewById(R.id.profile_image) ,"profile_image");
            Pair< View , String > pair2 = Pair.create(findViewById(R.id.profile_follow_button) ,"edit_photo");
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ProfileActivity.this ,pair1 , pair2);
            startActivity(showPostIntent , optionsCompat.toBundle());

        }else{

            if(!followState){   //currently not following after click i will follow this person

                addRating(mUserID , 15);
                addRating(mCurProfileId , 5);

                followState = true;
                mProfileFollow.setText("following");
                mProfileFollow.setTextColor(getResources().getColor(R.color.colorPrimaryy));
                mProfileFollow.setBackgroundColor(getResources().getColor(R.color.White));

                //  --------- GETTING THE DATE AND TIME ----------//
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("MMM  yyyy");
                String formattedDate = df.format(c.getTime());

                //building notification
                String time_stamp = String.valueOf(new Date().getTime());
                DocumentReference ref = FirebaseFirestore.getInstance().collection("notifications/"+mCurProfileId+"/notificatinos").document();
                String followNotificatoinPushID = ref.getId();
                Notifications pushNoti = new Notifications( "follow" ,mUserID , mCurProfileId , mCurProfileId ,followNotificatoinPushID , time_stamp,"n"  );
                firebaseFirestore.collection("notifications/"+mCurProfileId+"/notificatinos").document(followNotificatoinPushID).set(pushNoti);


                //------- SETTING THE INFORMATION THAT NOW I AM FOLLOWING THIS ID ------//
                Map<String, String> followingData = new HashMap<>();
                followingData.put("id" , mCurProfileId);
                followingData.put("date" , formattedDate);
                followingData.put("notificationID", followNotificatoinPushID);
                followingData.put("name" , mCurUserName);
                followingData.put("thumb_image" , mCurUserTHumbImage);
                followingData.put("timestamp" , time_stamp);
                firebaseFirestore.collection("followings/" + mUserID + "/followings").document(mCurProfileId).set(followingData);

                //------- SETTING THE INFORMATION THAT NOW I AM FOLLOWER OF THIS ID ------//
                Map<String, String> followerData = new HashMap<>();
                followerData.put("id" , mUserID);
                followerData.put("date" , formattedDate);
                followerData.put("notificationID", followNotificatoinPushID);
                followerData.put("name" , mUserName);
                followerData.put("thumb_image" ,mUserThumbImage);
                followerData.put("timestamp" , time_stamp);
                firebaseFirestore.collection("followers/" + mCurProfileId + "/followers").document(mUserID).set(followerData);

            }else{  //currently following this person after clickhin i will not fololw

                addRating(mUserID , 15);
                addRating(mCurProfileId , 5);

                followState = false;
                mProfileFollow.setText("follow");
                mProfileFollow.setTextColor(getResources().getColor(R.color.White));
                mProfileFollow.setBackgroundColor(getResources().getColor(R.color.colorPrimaryy));


                firebaseFirestore.collection("followers/" + mCurProfileId + "/followers").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().exists()){
                                String  followNotificatoinPushID = task.getResult().getString("notificationID");
                                if(followNotificatoinPushID!=null){
                                    firebaseFirestore.collection("notifications/"+mCurProfileId+"/notificatinos").document(followNotificatoinPushID).delete();
                                }
                                firebaseFirestore.collection("followings/" + mUserID + "/followings").document(mCurProfileId).delete();
                                firebaseFirestore.collection("followers/" + mCurProfileId + "/followers").document(mUserID).delete();
                            }
                        }
                    }
                });
            }
        }

    }
    public void profileImageClickHandle(View view) {
        Intent profileImageIntent = new Intent(ProfileActivity.this , ShowProfileImageActivity.class);
        profileImageIntent.putExtra("url", mCurUserImage);
        profileImageIntent.putExtra("name", mCurUserName);
        startActivity(profileImageIntent);
    }



    @SuppressLint("StaticFieldLeak")
    public class AsyncPostLoading extends AsyncTask<Integer , Posts, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            Timber.d("do in background is started !!!!");
            Query nextQuery = firebaseFirestore.collection("posts").whereEqualTo("primary_user_id" , mCurProfileId).orderBy("time_stamp" , Query.Direction.DESCENDING).limit(5);
            if(params[0] == 2){
                nextQuery = firebaseFirestore.collection("posts")
                        .orderBy("time_stamp" , Query.Direction.DESCENDING)
                        .whereEqualTo("primary_user_id" , mCurProfileId)
                        .startAfter(lastVisible)
                        .limit(5);
            }
            nextQuery.addSnapshotListener(ProfileActivity.this , (documentSnapshots, e) -> {
                if(documentSnapshots!=null){
                    if(!documentSnapshots.isEmpty()){
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                        for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                Posts singlePosts = doc.getDocument().toObject(Posts.class);
                                String uid = singlePosts.getPrimary_user_id();
                                Timber.d("found user id %s", uid);
                                publishProgress(singlePosts);
                            }
                        }
                    }
                }else{
                    Timber.d("onCreate:   document snapshot is null");
                }
            });
            return true;
        }

        @Override
        protected void onProgressUpdate(Posts... values) {
            Timber.d("progress update is calleddd !!!!!!!!!!");
            posts.add(values[0]);
            mPostsAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            Timber.d("progress post execute is calleddd !!!!!!!!!!");
            //Toast.makeText(ProfileActivity.this, "Loaded one page !!!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_out:
                Functions.logOut();
                Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
                return true;
            case R.id.action_edit_profile:
                /*Intent mProfileIntent = new Intent(ProfileActivity.this , ProfileSettingsActivity.class);
                startActivity(mProfileIntent);*/
                return true;
            case R.id.action_search:
                Intent searchIntent = new Intent(ProfileActivity.this , SearchActivityF.class);
                startActivity(searchIntent);
                return true;
            case R.id.action_message:
                Intent inboxIntent = new Intent(ProfileActivity.this , InboxActivity.class);
                startActivity(inboxIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void addRating(String mUserID  , int factor) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        timber.log.Timber.tag(TAG).d("addRating:   function calledd !!!!");
        final DocumentReference ratingRef = FirebaseFirestore.getInstance().collection("ratings")
                .document(mUserID);
        firebaseFirestore.runTransaction(transaction -> {

            Ratings ratings = transaction.get(ratingRef)
                    .toObject(Ratings.class);
            long curRating = ratings.getRating();
            long nextRating = curRating + factor;

            ratings.setRating(nextRating);
            transaction.set(ratingRef, ratings);
            return null;
        });
    }



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                Intent mainIntent = new Intent(ProfileActivity.this , HomeActivity.class);
                startActivity(mainIntent);
                finish();
                return true;
            case R.id.navigation_explore:
                Intent exploreIntent = new Intent(ProfileActivity.this , ExploreActivity.class);
                startActivity(exploreIntent);
                finish();
                return true;
            case R.id.navigation_location:
                Intent mapIntent  = new Intent(ProfileActivity.this , MapsActivity.class);
                startActivity(mapIntent);
                return true;
            case R.id.navigation_notifications:
                Intent notificaitonIntent = new Intent(ProfileActivity.this , NotificationActivity.class);
                startActivity(notificaitonIntent);
                finish();
                return true;
            case R.id.navigation_profile:
                Intent profileIntent = new Intent(ProfileActivity.this , ProfileActivity.class);
                profileIntent.putExtra("userID" , mUserID);
                startActivity(profileIntent);
                finish();
                return true;
        }
        return false;
    };

    private void loadDetails(){
        PreferenceManager preferenceManager = new PreferenceManager(this);
        mUserID = preferenceManager.getUserId();
        mUserName = preferenceManager.getUserName();
        mUserImage = preferenceManager.getUserImage();
        mUserThumbImage = preferenceManager.getUserThumbImage();
    }

    private void initBottomNav(){
        BottomNavigationViewEx mNavigation = findViewById(R.id.main_bottom_nav);
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mNavigation.enableAnimation(true);
        mNavigation.enableShiftingMode(false);
        mNavigation.enableItemShiftingMode(false);
        mNavigation.setIconSize(25, 25);
        mNavigation.setTextSize(7);
    }

    private void initRecycler(){
        posts = new ArrayList<>();
        mPostsAdapter = new PostsAdapter(posts,ProfileActivity.this, ProfileActivity.this);
        mProfilePostsRecycelr.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));
        mProfilePostsRecycelr.setAdapter(mPostsAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    private void initViews(){
         //getting views
        mProfileName  = findViewById(R.id.profile_name);
        mProfileBio  = findViewById(R.id.profile_bio);
        mProfileFollowersCnt  =findViewById(R.id.profile_followers_cnt);
        mProfileFollowingsCnt  = findViewById(R.id.profile_followings_cnt);
        mProfilePostsRecycelr = findViewById(R.id.profile_posts_recycler);
        mProfileBadgesRecycler = findViewById(R.id.profile_badges_recycler);
        mProfilePostsRecycelr.setNestedScrollingEnabled(false);
        mProfileBadgesRecycler.setNestedScrollingEnabled(false);

        mProfileFollow = findViewById(R.id.profile_follow_button);


        cardFollowers = findViewById(R.id.profile_followers_linear);
        cardFollowings = findViewById(R.id.profile_followings_linear);
        sendMessage = findViewById(R.id.profile_send_message_linear);
        mProfileSendMessage = findViewById(R.id.profile_send_message_text);

        mProfileImageView =findViewById(R.id.profile_image);

    }



}
