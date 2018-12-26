package candor.fulki.utils;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Constants {



    //Shared Preferences constants
    public static final String USERS_SHARED_PREF = "UserBasics" ;

    public static final String KEY_USER_NAME = "nameKey";
    public static final String KEY_USER_ID   = "idKey";
    public static final String KEY_THUMB_IMAGE = "thumbKey";
    public static final String KEY_IMAGE = "imageKey";
    public static final String KEY_GENDER = "genderKey";
    public static final String KEY_BLOOD = "bloodKey";
    public static final String KEY_DIVISION = "divisionKey";
    public static final String KEY_BIRTH_DATE = "birthKey";
    public static final String KEY_DISTRICT = "districtKey";


    /*//firebase links
    FirebaseFirestore firebaseFirestore= FirebaseFirestore.getInstance();
    DocumentReference ratingRef = firebaseFirestore.collection("ratings").document(mUserID);
    DocumentReference bloodRef = firebaseFirestore.collection(bloodString).document(mUserID);
    DocumentReference timestampRef = firebaseFirestore.collection("time_stamps").document(mUserID);
    DocumentReference deviceRef = firebaseFirestore.collection("device_ids").document(mUserID);
    DocumentReference userRef = firebaseFirestore.collection("users").document(mUserID);
    DocumentReference categoryRef = firebaseFirestore.collection("categories").document(mUserID);*/



}
