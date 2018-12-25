package candor.fulki.models;

/**
 * Created by Mohammad Faisal on 1/30/2018.
 */

public class Notifications {

    private String type;
    private String notification_from;
    private String notification_to;
    private String content_id;
    private String notification_id;
    private String time_stamp;
    private String seen;

    public Notifications(){}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNotification_from() {
        return notification_from;
    }

    public void setNotification_from(String notification_from) {
        this.notification_from = notification_from;
    }

    public String getNotification_to() {
        return notification_to;
    }

    public void setNotification_to(String notification_to) {
        this.notification_to = notification_to;
    }

    public String getContent_id() {
        return content_id;
    }

    public void setContent_id(String content_id) {
        this.content_id = content_id;
    }

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }

    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public Notifications(String type, String notification_from, String notification_to, String content_id, String notification_id, String time_stamp, String seen) {

        this.type = type;
        this.notification_from = notification_from;
        this.notification_to = notification_to;
        this.content_id = content_id;
        this.notification_id = notification_id;
        this.time_stamp = time_stamp;
        this.seen = seen;
    }
}
