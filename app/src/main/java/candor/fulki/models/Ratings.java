package candor.fulki.models;

public class Ratings {

    private String name;
    private String user_id;
    private String thumb_image;
    private long  rating;
    private long time_stamp;

    public String getUser_name() {
        return name;
    }

    public void setUser_name(String user_name) {
        this.name = user_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public long getRating() {
        return rating;
    }

    public void setRating(long rating) {
        this.rating = rating;
    }

    public Ratings() {

    }

    public Ratings(String name, String user_id, String thumb_image, long rating) {
        this.name = name;
        this.user_id = user_id;
        this.thumb_image = thumb_image;
        this.rating = rating;
    }
}
