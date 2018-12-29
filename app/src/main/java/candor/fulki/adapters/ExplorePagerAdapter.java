package candor.fulki.adapters;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import candor.fulki.fragments.EventFragment;
import candor.fulki.fragments.PeopleFragment;
import candor.fulki.fragments.PostsFragment;

public class ExplorePagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs=3;

    public ExplorePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                PeopleFragment tab1 = new PeopleFragment();
                return tab1;
            case 1:
                EventFragment tab2 = new EventFragment();
                return tab2;
            case 2:
                PostsFragment tab3 = new PostsFragment();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
