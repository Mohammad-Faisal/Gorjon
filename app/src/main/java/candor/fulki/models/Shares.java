package candor.fulki.models;

public class Shares {

    String uid;
    String notificationID;
    public Shares() {}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        uid = uid;
    }

    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public Shares(String uid, String notificationID) {

        this.uid = uid;
        this.notificationID = notificationID;
    }
}
