package candor.fulki.chat.conversation;


public class ChatBuddies {
    private String user_id;
    private String thumb_image_url;
    private String user_name;
    private String last_message;
    private String seen_status;
    private String last_message_from;
    private long time_stamp;

    public ChatBuddies(){}

    public String getLast_message_from() {
        return last_message_from;
    }

    public void setLast_message_from(String last_message_from) {
        this.last_message_from = last_message_from;
    }

    public ChatBuddies(String user_id, String user_name, String thumb_image_url, String last_message, String seen_status, String last_message_from, long time_stamp) {
        this.user_id = user_id;
        this.thumb_image_url = thumb_image_url;
        this.user_name = user_name;
        this.last_message = last_message;
        this.seen_status = seen_status;
        this.last_message_from = last_message_from;

        this.time_stamp = time_stamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getThumb_image_url() {
        return thumb_image_url;
    }

    public void setThumb_image_url(String thumb_image_url) {
        this.thumb_image_url = thumb_image_url;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public String getSeen_status() {
        return seen_status;
    }

    public void setSeen_status(String seen_status) {
        this.seen_status = seen_status;
    }

    public long getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(long time_stamp) {
        this.time_stamp = time_stamp;
    }
}

