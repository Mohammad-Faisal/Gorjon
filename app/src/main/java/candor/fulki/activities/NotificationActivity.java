package candor.fulki.activities;

import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;

import candor.fulki.adapters.NotificationsAdapter;
import candor.fulki.utils.Functions;
import candor.fulki.models.Notifications;
import candor.fulki.R;

public class NotificationActivity extends AppCompatActivity {



    private static final String TAG = "NotificationFragment";

    private List<Notifications> notifications;
    private List<String> notificationIDs;
    private NotificationsAdapter mNotificationsAdapter;
    private String mUserID;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                Intent mainIntent = new Intent(NotificationActivity.this , HomeActivity.class);
                startActivity(mainIntent);
                finish();
                //setFragment(mHomeFragment);
                return true;
            case R.id.navigation_explore:
                Intent exploreIntent = new Intent(NotificationActivity.this , ExploreActivity.class);
                startActivity(exploreIntent);
                finish();
                //setFragment(mExploreFragment);
                return true;
            case R.id.navigation_location:
                Intent mapIntent  = new Intent(NotificationActivity.this , MapsActivity.class);
                startActivity(mapIntent);
                return true;
            case R.id.navigation_notifications:
                Intent notificaitonIntent = new Intent(NotificationActivity.this , NotificationActivity.class);
                startActivity(notificaitonIntent);
                finish();
                //setFragment(mNotificationFragment);
                return true;
            case R.id.navigation_profile:
                Intent profileIntent = new Intent(NotificationActivity.this , ProfileActivity.class);
                profileIntent.putExtra("userID" , mUserID);
                startActivity(profileIntent);
                finish();
                //setFragment(mProfileFragment);
                return true;
        }
        return false;
    };

    private void initBottomNav(){
         BottomNavigationViewEx mNavigation = findViewById(R.id.main_bottom_nav);
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mNavigation.enableAnimation(true);
        mNavigation.enableShiftingMode(false);
        mNavigation.enableItemShiftingMode(false);
        mNavigation.setIconSize(25, 25);
        mNavigation.setTextSize(7);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);


        initBottomNav();

        if(getSupportActionBar() != null)getSupportActionBar().setTitle("  Notifications");


        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            if(FirebaseAuth.getInstance().getCurrentUser() != null)mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();


            //setting RecyclerView
            notifications = new ArrayList<>();
            notificationIDs = new ArrayList<>();
            RecyclerView recyclerView =findViewById(R.id.notification_recycler);
            recyclerView.hasFixedSize();
            recyclerView.setLayoutManager(new LinearLayoutManager(NotificationActivity.this));
            mNotificationsAdapter = new NotificationsAdapter(notifications, notificationIDs, NotificationActivity.this , NotificationActivity.this);
            recyclerView.setAdapter(mNotificationsAdapter);

            FirebaseFirestore firebaseFirestore;
            firebaseFirestore = FirebaseFirestore.getInstance();
            Query nextQuery = firebaseFirestore.collection("notifications/"+mUserID+"/notificatinos").orderBy("time_stamp" , Query.Direction.DESCENDING).limit(200);
            nextQuery.addSnapshotListener((documentSnapshots, e) -> {
                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        Notifications singleNotifications = doc.getDocument().toObject(Notifications.class);
                        notifications.add(singleNotifications);
                        mNotificationsAdapter.notifyDataSetChanged();
                    }else{
                        timber.log.Timber.d("onEvent: notification type is not added");
                    }
                }
            });
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
                Intent mainIntent = new Intent(NotificationActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
                return true;
            case R.id.action_edit_profile:
                /*Intent mProfileIntent = new Intent(NotificationActivity.this , ProfileSettingsActivity.class);
                startActivity(mProfileIntent);*/
                return true;
            case R.id.action_search:
                Intent searchIntent = new Intent(NotificationActivity.this , SearchActivityF.class);

                startActivity(searchIntent);
                return true;
            case R.id.action_message:
                Intent inboxIntent = new Intent(NotificationActivity.this , InboxActivity.class);
                startActivity(inboxIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



}
