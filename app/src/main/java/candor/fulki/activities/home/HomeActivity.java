package candor.fulki.activities.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import candor.fulki.activities.explore.ExploreActivity;
import candor.fulki.activities.MainActivity;
import candor.fulki.activities.MapsActivity;
import candor.fulki.activities.NotificationActivity;
import candor.fulki.activities.profile.ProfileActivity;
import candor.fulki.activities.chat.InboxActivity;
import candor.fulki.activities.search.SearchActivityF;
import candor.fulki.adapters.PostsAdapter;


import candor.fulki.utils.Functions;
import candor.fulki.models.Posts;
import candor.fulki.R;
import candor.fulki.utils.PreferenceManager;
import timber.log.Timber;


public class HomeActivity extends AppCompatActivity {






    public  String mUserID = null;
    public  String mUserName = "";
    public  String mUserThumbImage = "";
    public  String mUserImage = "";


    // home fragments recycler view
    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore;
    private PostsAdapter mPostsAdapter;
    private List<Posts> posts;
    LinearLayoutManager mLinearManager;
    private DocumentSnapshot lastVisible = null;



    int scroll_count = 1;


    public static final String Name = "nameKey";


    private void loadDetails(){
        PreferenceManager preferenceManager = new PreferenceManager(this);
        mUserID = preferenceManager.getUserId();
        mUserName = preferenceManager.getUserName();
        mUserImage = preferenceManager.getUserImage();
        mUserThumbImage = preferenceManager.getUserThumbImage();
        Timber.tag("Fulki").d("from shared pref .... "+mUserThumbImage);
        Timber.tag("Fulki").d("from shared pref .... "+mUserName);
        Functions.createLog(mUserThumbImage);
    }

    private void initRecycler(){
        posts = new ArrayList<>();
            recyclerView = findViewById(R.id.home_recycler_view);
            mLinearManager = new LinearLayoutManager(HomeActivity.this);
            recyclerView.setLayoutManager(mLinearManager);
            recyclerView.setNestedScrollingEnabled(false);
            mPostsAdapter = new PostsAdapter(posts,HomeActivity.this, HomeActivity.this);
            mPostsAdapter.setHasStableIds(true);
            recyclerView.setAdapter(mPostsAdapter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle("  Gorjon");
            getSupportActionBar().setElevation(1);
        }

        loadDetails();
        initBottomNavigation();
        initRecycler();



        findViewById(R.id.home_floating).setOnClickListener(v -> {
            Intent createPostIntent = new Intent(HomeActivity.this , CreatePostActivity.class);
            startActivity(createPostIntent);
        });

        if (mUserID != null) {

            firebaseFirestore =FirebaseFirestore.getInstance();
            Map< String, Object> device_id = new HashMap<>();
            String deviceTokenID = FirebaseInstanceId.getInstance().getToken();
            device_id.put("user_id" , mUserID);
            device_id.put("device_id" , deviceTokenID);




            firebaseFirestore = FirebaseFirestore.getInstance();


            AsyncPostLoading runner = new AsyncPostLoading();
            runner.execute(1);


          recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if(reachedBottom){
                        Timber.d("onScrolled:    reached bottom %s", scroll_count++);
                        AsyncPostLoading runner = new AsyncPostLoading();
                        runner.execute(2);
                    }
                }
            });
        }
    }


    @SuppressLint("StaticFieldLeak")
    public class AsyncPostLoading extends AsyncTask<Integer , Posts, Boolean>{

        @Override
        protected Boolean doInBackground(Integer... params) {
            Timber.d("do in background is started !!!!");
            Query nextQuery = firebaseFirestore.collection("posts").orderBy("time_stamp" , Query.Direction.DESCENDING).limit(5);
            if(params[0] == 2){
                nextQuery = firebaseFirestore.collection("posts")
                        .orderBy("time_stamp" , Query.Direction.DESCENDING)
                        .startAfter(lastVisible)
                        .limit(5);
            }
            nextQuery.addSnapshotListener(HomeActivity.this , (documentSnapshots, e) -> {
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
           // Toast.makeText(HomeActivity.this, "Loaded one page !!!!", Toast.LENGTH_SHORT).show();
        }
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
                Intent mainIntent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
                return true;
            case R.id.action_edit_profile:
                /*Intent mProfileIntent = new Intent(HomeActivity.this , ProfileSettingsActivity.class);
                startActivity(mProfileIntent);*/
                return true;
            case R.id.action_search:
                Intent searchIntent = new Intent(HomeActivity.this , SearchActivityF.class);
                startActivity(searchIntent);
                return true;
            case R.id.action_message:
                Intent inboxIntent = new Intent(HomeActivity.this , InboxActivity.class);
                startActivity(inboxIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                Intent mainIntent = new Intent(HomeActivity.this , HomeActivity.class);
                startActivity(mainIntent);
                finish();
                return true;
            case R.id.navigation_explore:
                Intent exploreIntent = new Intent(HomeActivity.this , ExploreActivity.class);
                startActivity(exploreIntent);
                finish();
                return true;
            case R.id.navigation_location:
                Intent mapIntent  = new Intent(HomeActivity.this , MapsActivity.class);
                startActivity(mapIntent);
                return true;
            case R.id.navigation_notifications:
                Intent notificaitonIntent = new Intent(HomeActivity.this , NotificationActivity.class);
                startActivity(notificaitonIntent);
                finish();
                return true;
            case R.id.navigation_profile:
                Intent profileIntent = new Intent(HomeActivity.this , ProfileActivity.class);
                profileIntent.putExtra("userID" , mUserID);
                startActivity(profileIntent);
                finish();
                return true;
        }
        return false;
    };

    private void initBottomNavigation() {
        BottomNavigationViewEx mNavigation = findViewById(R.id.main_bottom_nav);
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mNavigation.enableAnimation(true);
        mNavigation.enableShiftingMode(false);
        mNavigation.enableItemShiftingMode(false);
        mNavigation.setIconSize(25, 25);
        mNavigation.setTextSize(7);

    }


}
