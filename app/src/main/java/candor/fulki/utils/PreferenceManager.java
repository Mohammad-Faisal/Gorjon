package candor.fulki.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class PreferenceManager {

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Context context;

    @SuppressLint("CommitPrefEdits")
    public PreferenceManager(Context context){
        this.context = context;
        sp = context.getSharedPreferences(Constants.USERS_SHARED_PREF, MODE_PRIVATE);
        editor = sp.edit();
    }

    public void setUserName(String userName){
        editor.putString(Constants.KEY_USER_NAME, userName);
        editor.commit();
    }

    public String getUserName(){
        return sp.getString(Constants.KEY_USER_NAME, "User Name");
    }

    public void setUserImage(String userImage ){
        editor.putString(Constants.KEY_IMAGE, userImage);
        editor.commit();
    }

    public String getUserImage(){
        return sp.getString(Constants.KEY_IMAGE, "");
    }


    public void setUserThumbImage(String userThumbImage ){
        editor.putString(Constants.KEY_THUMB_IMAGE, userThumbImage);
        editor.commit();
    }

    public String getUserThumbImage(){
        return sp.getString(Constants.KEY_THUMB_IMAGE, "");
    }

    public void setUserId(String userId ){
        editor.putString(Constants.KEY_USER_ID, userId);
        editor.commit();
    }

    public String getUserId(){
        return sp.getString(Constants.KEY_USER_ID, "");
    }


    public void setGender(String gender){
        editor.putString(Constants.KEY_GENDER, gender);
        editor.commit();
    }

    public String getGender(){
        return sp.getString(Constants.KEY_GENDER, "others");
    }


    public void setDivision(String division ){
        editor.putString(Constants.KEY_DIVISION, division);
        editor.commit();
    }

    public String getDivision(){
        return sp.getString(Constants.KEY_DIVISION, "others");
    }


    public void setDistrict(String district ){
        editor.putString(Constants.KEY_DISTRICT, district);
        editor.commit();
    }

    public String getDistrict(){
        return sp.getString(Constants.KEY_DISTRICT, "others");
    }


    public void setBlood(String blood ){
        editor.putString(Constants.KEY_BLOOD, blood);
        editor.commit();
    }

    public String getBlood(){
        return sp.getString(Constants.KEY_BLOOD, "");
    }

    public void setBirthDate(String blood ){
        editor.putString(Constants.KEY_BIRTH_DATE, blood);
        editor.commit();
    }

    public String getBirthDate(){
        return sp.getString(Constants.KEY_BIRTH_DATE, "");
    }

    public void setLoggedIn(Boolean  login) {
        editor.putBoolean("isLoggedIn", login);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return sp.getBoolean("isLoggedIn", false);
    }



}
