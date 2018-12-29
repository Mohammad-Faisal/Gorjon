package candor.fulki.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import candor.fulki.R;
import candor.fulki.models.Ratings;
import candor.fulki.models.UserBasic;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import timber.log.Timber;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static com.sun.mail.util.ASCIIUtility.getBytes;

public class Functions {

    private UserBasic userBasic = new UserBasic();
    private static final int RC_CHECK_PERMISSION_LOCATION = 2;

    public Functions() {
    }

    public static Bitmap getBitmapFromImageUri(Uri imageUri , Context context){
        Bitmap temp_bitmap = null;
        File temp_file = new File(imageUri.getPath());
        try {
            temp_bitmap = new Compressor(context)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(30)
                    .compressToBitmap(temp_file);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return temp_bitmap;
    }

    public static byte[] getByteArrayFrombitmap(Bitmap bitmap  , int quality){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }


    public static byte[] getByteArrayFromImageUri(Uri imageUri, int quality  , Context context ){
        Bitmap temp_bitmap = getBitmapFromImageUri(imageUri, context);
        return getByteArrayFrombitmap(temp_bitmap , quality);
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



    public static Uri bitmapToUriConverter(Bitmap mBitmap , Activity activity) {
        Uri uri = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 100, 100);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap newBitmap = Bitmap.createScaledBitmap(mBitmap, 200, 200,
                    true);
            File file = new File(activity.getFilesDir(), "Image"
                    + new Random().nextInt() + ".jpeg");
            FileOutputStream out =activity.openFileOutput(file.getName(),
                    Context.MODE_WORLD_READABLE);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            //get absolute path
            String realPath = file.getAbsolutePath();
            File f = new File(realPath);
            uri = Uri.fromFile(f);

        } catch (Exception e) {
            Log.e("Your Error Message", e.getMessage());
        }
        return uri;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    public static void logOut(){
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
    }



}