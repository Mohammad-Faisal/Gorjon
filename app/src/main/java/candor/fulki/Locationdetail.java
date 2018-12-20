package candor.fulki;

import com.google.android.gms.maps.model.LatLng;

public class Locationdetail {

    String user_name;
    String user_id;
    String thumb_image;
    double lat;
    double lng;
    String place_name;

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
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

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public Locationdetail(String user_name, String user_id, String thumb_image, double lat, double lng, String place_name) {

        this.user_name = user_name;
        this.user_id = user_id;
        this.thumb_image = thumb_image;
        this.lat = lat;
        this.lng = lng;
        this.place_name = place_name;
    }
}
