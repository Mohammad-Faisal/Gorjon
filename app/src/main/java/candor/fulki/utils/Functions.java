package candor.fulki.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import candor.fulki.models.Ratings;
import candor.fulki.models.UserBasic;
import timber.log.Timber;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class Functions {

    private UserBasic userBasic = new UserBasic();
    private static final int RC_CHECK_PERMISSION_LOCATION = 2;

    public Functions() {
    }



    public static void checkPermission(Context context , Activity activity){
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    RC_CHECK_PERMISSION_LOCATION);

        }
    }

    public static boolean isDataAvailable(Context context) {
        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    public static void addRating(String mUserID , long rating) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        final DocumentReference ratingRef = FirebaseFirestore.getInstance().collection("ratings")
                .document(mUserID);
        firebaseFirestore.runTransaction(transaction -> {

            Ratings ratings = transaction.get(ratingRef)
                    .toObject(Ratings.class);
            Timber.tag("Fulki").d("I am inside rating transaction");
            Timber.tag("Fulki").d(ratings.getName());
            Timber.tag("Fulki").d(ratings.getThumb_image());
            Timber.tag("Fulki").d(ratings.getUser_id());


            long curRating = ratings.getRating();
            long nextRating = curRating + rating;

            ratings.setRating(nextRating);
            transaction.set(ratingRef, ratings);
            return null;
        });
    }

    public static long getTimeStamp(){
        return new Date().getTime();
    }


    public static void createLog(String message){
        Timber.tag("Fulki").d(message);
    }


    public  static void printHashKey(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                //Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
                Timber.tag("Fulki").d(hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e("Fulki", "printHashKey()", e);
        } catch (Exception e) {
            Log.e("Fulki", "printHashKey()", e);
        }
    }




    public static void logOut(){
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
    }



}
