package candor.fulki.general;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import candor.fulki.Fulki;
import candor.fulki.R;
import candor.fulki.models.Ratings;
import candor.fulki.models.UserBasic;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import timber.log.Timber;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class Functions {

    private UserBasic userBasic = new UserBasic();
    private static final int RC_CHECK_PERMISSION_LOCATION = 2;

    public Functions() {
    }


    // give image uri and context and return byte array
    public static byte[] CompressImage(Uri imagetUri , Activity context){
        Bitmap thumb_bitmap = null;
        File thumb_file = new File(imagetUri.getPath());
        try {
            thumb_bitmap = new Compressor(context)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(30)
                    .compressToBitmap(thumb_file);
        }catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assert thumb_bitmap != null;
        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        return baos.toByteArray();
    }


    public static void BringImagePicker(Activity context) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(context);
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

        Log.d("Functions", "addRating:    "+mUserID);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        final DocumentReference ratingRef = FirebaseFirestore.getInstance().collection("ratings")
                .document(mUserID);
        firebaseFirestore.runTransaction(transaction -> {

            Ratings ratings = transaction.get(ratingRef)
                    .toObject(Ratings.class);

            assert ratings != null;
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

    public UserBasic getUserBasicData(String mUserID){
        //setting user details

        FirebaseFirestore.getInstance().collection("users").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String mUserName = task.getResult().getString("name");
                    String mUserImage = task.getResult().getString("thumb_image");
                    String mUserThumbImage = task.getResult().getString("image");
                    userBasic = new UserBasic(mUserName , mUserThumbImage , mUserImage);
                }
            }
        });
        return userBasic;
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


    public static void setUserImage(String imageURL, Context context ,  CircleImageView circleImageView){
        Picasso.with(context).load(imageURL).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.ic_blank_profile).into(circleImageView, new Callback() {
            @Override
            public void onSuccess() {
                //do nothing if an image is found offline
            }
            @Override
            public void onError() {
                Picasso.with(context).load(imageURL).placeholder(R.drawable.ic_blank_profile).into(circleImageView);
            }
        });
    }

}
