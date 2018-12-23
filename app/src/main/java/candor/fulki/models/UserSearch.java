package candor.fulki.models;



import android.os.Parcel;
import android.os.Parcelable;


public class UserSearch implements Parcelable{

    private String display_name;
    private String district;
    private String profile_photo;
    private String username;
    private String user_id;
    private String firstname;
    private String lastname;
    private String middlename;

    public UserSearch(String display_name, String district, String profile_photo, String username, String user_id, String firstname, String lastname,String middlename) {
        this.display_name = display_name;
        this.district = district;
        this.profile_photo = profile_photo;
        this.username = username;
        this.user_id = user_id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.middlename = middlename;
    }


    public UserSearch() {

    }

    protected UserSearch(Parcel in) {
        display_name = in.readString();
        district = in.readString();
        profile_photo = in.readString();
        username = in.readString();
        user_id = in.readString();
        firstname=in.readString();
        lastname=in.readString();
        middlename=in.readString();
    }

    public static final Creator<UserSearch> CREATOR = new Creator<UserSearch>() {
        @Override
        public UserSearch createFromParcel(Parcel in) {
            return new UserSearch(in);
        }

        @Override
        public UserSearch[] newArray(int size) {
            return new UserSearch[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }



    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public String toString() {
        return "UserAccountSettings{" +
                ", display_name='" + display_name + '\'' +
                ", districts=" + district +
                ", profile_photo='" + profile_photo + '\'' +
                ", username='" + username + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", midlename='" + middlename + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(display_name);
        dest.writeString(district);
        dest.writeString(profile_photo);
        dest.writeString(username);
        dest.writeString(user_id);
        dest.writeString(firstname);
        dest.writeString(lastname);
        dest.writeString(middlename);
    }
}