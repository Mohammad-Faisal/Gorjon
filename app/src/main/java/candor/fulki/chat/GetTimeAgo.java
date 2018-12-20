package candor.fulki.chat;

import android.app.Application;
import android.content.Context;

import java.util.Calendar;

/**
 * Created by Mohammad Faisal on 12/2/2017.
 */

public class GetTimeAgo extends Application{


    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public GetTimeAgo() {
    }

    public static String getTimeAgo(long time, Context context) {

        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "minitue ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " " + "minutes_ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "hour_ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " " + "hours_ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " " + "days_ago";
        }
    }



}
