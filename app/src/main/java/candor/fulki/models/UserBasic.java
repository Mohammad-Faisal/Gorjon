package candor.fulki.models;

public class UserBasic {

    private String mUserID;
    private String mUserName;
    private String mUserThumbImage;

    public UserBasic() {
    }

    public UserBasic(String mUserID, String mUserName, String mUserThumbImage) {
        this.mUserID = mUserID;
        this.mUserName = mUserName;
        this.mUserThumbImage = mUserThumbImage;
    }

    public String getmUserID() {
        return mUserID;
    }

    public void setmUserID(String mUserID) {
        this.mUserID = mUserID;
    }

    public String getmUserName() {
        return mUserName;
    }

    public void setmUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getmUserThumbImage() {
        return mUserThumbImage;
    }

    public void setmUserThumbImage(String mUserThumbImage) {
        this.mUserThumbImage = mUserThumbImage;
    }
}
