package candor.fulki.models;

public class Likes {



    private String id;
    private String name;
    private String thumb_image;
    private String notification_id;
    private String timestamp;


    public Likes() {
    }

    Likes(String id, String name, String thumb_image, String notification_id, String timestamp) {

        this.id = id;
        this.name = name;
        this.thumb_image = thumb_image;
        this.notification_id = notification_id;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }

    public String getTime_stamp() {
        return timestamp;
    }

    public void setTime_stamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
