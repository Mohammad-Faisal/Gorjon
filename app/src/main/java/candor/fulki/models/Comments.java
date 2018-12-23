package candor.fulki.models;


public class Comments {

    private String comment;
    private String uid;
    private String commentId;
    private String postID;
    private String notificationID;
    private String time_stamp;

    public Comments(){}

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }

    public Comments(String comment, String uid, String commentId, String postID, String notificationID, String time_stamp) {

        this.comment = comment;
        this.uid = uid;
        this.commentId = commentId;
        this.postID = postID;
        this.notificationID = notificationID;
        this.time_stamp = time_stamp;
    }
}
