package candor.fulki.models;

import java.util.HashMap;

public class CombinedPosts {

    private long timestamp;

    private String primary_user_id;
    private String secondary_user_id;

    private String primary_push_id;
    private String secondary_push_id;

    private HashMap< String  ,  String > post_image_url;
    private HashMap< String  ,  String > post_thumb_url;

    private String time_and_date;
    private String location;
    private String caption;
    private String type;

    private String privacy;

    private long like_cnt;
    private long comment_cnt;
    private long share_cnt;

    public CombinedPosts() {
    }

    public long getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPrimary_user_id() {
        return primary_user_id;
    }

    public void setPrimary_user_id(String primary_user_id) {
        this.primary_user_id = primary_user_id;
    }

    public String getSecondary_user_id() {
        return secondary_user_id;
    }

    public void setSecondary_user_id(String secondary_user_id) {
        this.secondary_user_id = secondary_user_id;
    }

    public String getPrimary_push_id() {
        return primary_push_id;
    }

    public void setPrimary_push_id(String primary_push_id) {
        this.primary_push_id = primary_push_id;
    }

    public String getSecondary_push_id() {
        return secondary_push_id;
    }

    public void setSecondary_push_id(String secondary_push_id) {
        this.secondary_push_id = secondary_push_id;
    }

    public HashMap<String, String> getPost_image_url() {
        return post_image_url;
    }

    public void setPost_image_url(HashMap<String, String> post_image_url) {
        this.post_image_url = post_image_url;
    }

    public HashMap<String, String> getPost_thumb_url() {
        return post_thumb_url;
    }

    public void setPost_thumb_url(HashMap<String, String> post_thumb_url) {
        this.post_thumb_url = post_thumb_url;
    }

    public String getTime_and_date() {
        return time_and_date;
    }

    public void setTime_and_date(String time_and_date) {
        this.time_and_date = time_and_date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public long getLike_cnt() {
        return like_cnt;
    }

    public void setLike_cnt(long like_cnt) {
        this.like_cnt = like_cnt;
    }

    public long getComment_cnt() {
        return comment_cnt;
    }

    public void setComment_cnt(long comment_cnt) {
        this.comment_cnt = comment_cnt;
    }

    public long getShare_cnt() {
        return share_cnt;
    }

    public void setShare_cnt(long share_cnt) {
        this.share_cnt = share_cnt;
    }

    public CombinedPosts(long timestamp, String primary_user_id, String secondary_user_id, String primary_push_id, String secondary_push_id, HashMap<String, String> post_image_url, HashMap<String, String> post_thumb_url, String time_and_date, String location, String caption, String type, String privacy, long like_cnt, long comment_cnt, long share_cnt) {

        this.timestamp = timestamp;
        this.primary_user_id = primary_user_id;
        this.secondary_user_id = secondary_user_id;
        this.primary_push_id = primary_push_id;
        this.secondary_push_id = secondary_push_id;
        this.post_image_url = post_image_url;
        this.post_thumb_url = post_thumb_url;
        this.time_and_date = time_and_date;
        this.location = location;
        this.caption = caption;
        this.type = type;
        this.privacy = privacy;
        this.like_cnt = like_cnt;
        this.comment_cnt = comment_cnt;
        this.share_cnt = share_cnt;
    }
}
