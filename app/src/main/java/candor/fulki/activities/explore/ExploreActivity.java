package candor.fulki.activities.explore;

import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import candor.fulki.activities.MainActivity;
import candor.fulki.activities.MapsActivity;
import candor.fulki.activities.NotificationActivity;
import candor.fulki.activities.chat.InboxActivity;
import candor.fulki.activities.home.HomeActivity;
import candor.fulki.activities.profile.ProfileActivity;
import candor.fulki.activities.search.SearchActivityF;
import candor.fulki.adapters.ExplorePagerAdapter;
import candor.fulki.utils.Functions;
import candor.fulki.R;


public class ExploreActivity extends AppCompatActivity {



    String mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                Intent mainIntent = new Intent(ExploreActivity.this , HomeActivity.class);
                startActivity(mainIntent);
                finish();
                //setFragment(mHomeFragment);
                return true;
            case R.id.navigation_explore:
                Intent exploreIntent = new Intent(ExploreActivity.this , ExploreActivity.class);
                startActivity(exploreIntent);
                finish();
                //setFragment(mExploreFragment);
                return true;
            case R.id.navigation_location:
                Intent mapIntent  = new Intent(ExploreActivity.this , MapsActivity.class);
                startActivity(mapIntent);
                return true;
            case R.id.navigation_notifications:
                Intent notificaitonIntent = new Intent(ExploreActivity.this , NotificationActivity.class);
                startActivity(notificaitonIntent);
                finish();
                //setFragment(mNotificationFragment);
                return true;
            case R.id.navigation_profile:
                Intent profileIntent = new Intent(ExploreActivity.this , ProfileActivity.class);
                profileIntent.putExtra("userID" , mUserID);
                startActivity(profileIntent);
                finish();
                //setFragment(mProfileFragment);
                return true;
        }
        return false;
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);


        if(getSupportActionBar()!=null)getSupportActionBar().setTitle("   Explore");


        //------------- BOTTOM NAVIGATION HANDLING ------//
        BottomNavigationViewEx mNavigation = findViewById(R.id.main_bottom_nav);
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mNavigation.enableAnimation(true);
        mNavigation.enableShiftingMode(false);
        mNavigation.enableItemShiftingMode(false);
        mNavigation.setIconSize(25, 25);
        mNavigation.setTextSize(7);



        FloatingActionButton exploreFloat = findViewById(R.id.explore_floating);
        exploreFloat.setOnClickListener(v -> {
            Intent createEventIntent  = new Intent( ExploreActivity.this , CreateEventActivity.class);
            startActivity(createEventIntent);
        });





        TabLayout tabLayout =  findViewById(R.id.explore_tab_layout);

        tabLayout.addTab(tabLayout.newTab().setText("People"));
        tabLayout.addTab(tabLayout.newTab().setText("Events"));
        tabLayout.addTab(tabLayout.newTab().setText("Posts"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = findViewById(R.id.explore_view_pager);
        final ExplorePagerAdapter adapter = new ExplorePagerAdapter
                (getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);


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
                Intent mainIntent = new Intent(ExploreActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
                return true;
            case R.id.action_edit_profile:
                /*Intent mProfileIntent = new Intent(ExploreActivity.this , ProfileSettingsActivity.class);
                startActivity(mProfileIntent);*/
                return true;
            case R.id.action_search:
                Intent searchIntent = new Intent(ExploreActivity.this , SearchActivityF.class);

                startActivity(searchIntent);
                return true;
            case R.id.action_message:
                Intent inboxIntent = new Intent(ExploreActivity.this , InboxActivity.class);
                startActivity(inboxIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }




}
