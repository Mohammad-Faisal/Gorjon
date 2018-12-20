package candor.fulki.chat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import candor.fulki.chat.conversation.ConversationFragment;
import candor.fulki.chat.meeting.MeetingFragment;

public class InboxPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public InboxPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ConversationFragment tab1 = new ConversationFragment();
                return tab1;
            case 1:
                MeetingFragment tab2 = new MeetingFragment();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
