package candor.fulki.chat.conversation;


public class Actives {
    private String userID;
    private String name;
    private String thumb_image;

    public Actives(String userID, String name, String thumb_image) {
        this.userID = userID;
        this.name = name;
        this.thumb_image = thumb_image;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
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
}
