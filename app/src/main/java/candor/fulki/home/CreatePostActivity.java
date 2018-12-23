package candor.fulki.home;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import candor.fulki.explore.people.Ratings;
import candor.fulki.general.FileUtil;
import candor.fulki.R;
import candor.fulki.models.PostFiles;
import id.zelory.compressor.Compressor;

public class CreatePostActivity extends AppCompatActivity {


    private static final String TAG = "CreatePostActivity";

    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    List<String> imagesEncodedList;
    ArrayList<Uri> postImageUriArrayList = new ArrayList<Uri>();


    private EditText mCaption;
    private EditText mLocation;

    private StorageReference imageFilePath;
    private StorageReference thumbFilePath;

    HashMap<String , Object> postMap = new HashMap<>();
    String mUserID;
    String postPushId;
    CombinedPosts post;


    ArrayList<String> imageUrls = new ArrayList<>();
    ArrayList<String> thumbImageUrls = new ArrayList<>();
    ArrayList<PostFiles> postFIles = new ArrayList<>();

    FirebaseFirestore firebaseFirestore;

    private ProgressDialog mProgress;

    int cnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        mCaption = findViewById(R.id.caption);
        mLocation = findViewById(R.id.location);

        mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }


    public void post(View view) throws IOException {

        if(isDataAvailable()){
            if(postImageUriArrayList.size() > 0){
                cnt = 0;
                uploadImages();
            }else{
                validatePost();
            }
        }else{
            Toast.makeText(this, "Please enable your data ", Toast.LENGTH_SHORT).show();
        }

    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public void uploadImages() throws IOException {



        mProgress = new ProgressDialog(CreatePostActivity.this);
        mProgress.setTitle("Uploading Image.......");
        mProgress.setMessage("please wait while we upload your post");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        Log.d(TAG, "uploadImages:    called !!");

        ViewGroup holder = findViewById(R.id.post_image_holder);


        for(int i=0; i<holder.getChildCount()-1; i++){

            File actualImage = FileUtil.from(CreatePostActivity.this, ((Uri)holder.getChildAt(i).getTag()));
            Bitmap compressedImageFile = new Compressor(CreatePostActivity.this)
                    .setMaxWidth(1024)
                    .setMaxHeight(768)
                    .setQuality(85)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .compressToBitmap(actualImage);
            byte[] imageByte  = getFileDataFromDrawable(compressedImageFile);
            Bitmap compressedThumbFile = new Compressor(CreatePostActivity.this)
                    .setMaxWidth(400)
                    .setMaxHeight(400)
                    .setQuality(50)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .compressToBitmap(actualImage);
            byte[] thumbByte  = getFileDataFromDrawable(compressedThumbFile);


            long timestamp = 1* new Date().getTime();
            String img = String.valueOf(timestamp);


            imageFilePath = FirebaseStorage.getInstance().getReference().child("post_images").child(mUserID).child(img+".jpg");
            thumbFilePath = FirebaseStorage.getInstance().getReference().child("post_thumb_images").child(mUserID).child(img+".jpg");

            PostFiles postFiles = new PostFiles("post_images/"+mUserID+"/"+img+".jpg" ,"post_thumb_images/"+mUserID+"/"+img+".jpg", postPushId);
            postFIles.add(postFiles);

            UploadTask uploadThumbTask = thumbFilePath.putBytes(thumbByte);
            UploadTask uploadImageTask = imageFilePath.putBytes(imageByte);


            uploadImageTask.addOnSuccessListener(taskSnapshot -> {
                if(uploadImageTask.isSuccessful()){
                    Uri downloadUrlThumb = taskSnapshot.getDownloadUrl();
                    final String imageUrl  = downloadUrlThumb.toString();
                    imageUrls.add(imageUrl);

                    Log.d(TAG, "uploadImages:     main and thumb image uploading is succesful "+cnt);

                    uploadThumbTask.addOnSuccessListener(taskSnapshot12 -> {
                        if(uploadThumbTask.isSuccessful()){
                            Uri downloadUrlThumb1 = taskSnapshot12.getDownloadUrl();
                            final String thumbImageUrl  = downloadUrlThumb1.toString();
                            thumbImageUrls.add(thumbImageUrl);

                            cnt++;
                            if(cnt ==  holder.getChildCount()-1){
                                validatePost();
                            }
                        }else{
                            Log.d(TAG, "uploadImages:    "+ taskSnapshot12.getError());
                            Toast.makeText(CreatePostActivity.this, "Thumb Image Upload Failed !", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Log.d(TAG, "uploadImages:    "+ taskSnapshot.getError());
                    Toast.makeText(this, "Image Upload Failed !", Toast.LENGTH_SHORT).show();
                }
            });


        }
    }

    public void uploadPost(){
        for(int i=0;i<postFIles.size();i++){
            PostFiles  file = postFIles.get(i);
            firebaseFirestore.collection("images").document("posts").collection(postPushId).add(file);
        }

        //test er jaygay posts hobe
        firebaseFirestore.collection("posts").document(postPushId).set(postMap).addOnCompleteListener(task -> {
            //mProgress.dismiss();
            if(task.isSuccessful()){
                Log.d(TAG, "uploadPost:     post upload succesfull ");
                mProgress.dismiss();
                addRating(mUserID , 15);
                Toast.makeText(CreatePostActivity.this, "Success !", Toast.LENGTH_SHORT).show();
                Intent mainIntent = new Intent(CreatePostActivity.this , HomeActivity.class);
                startActivity(mainIntent);
                finish();
            }else{
                mProgress.dismiss();
                Log.d(TAG, "uploadPost:     error  !! "+task.getException());
                Toast.makeText(CreatePostActivity.this, "There was an error in uploading post!" + task.getException()
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void validatePost(){


        mProgress = new ProgressDialog(CreatePostActivity.this);
        mProgress.setTitle("Uploading Image.......");
        mProgress.setMessage("please wait while we upload your post");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        Log.d(TAG, "validatePost:     started ");
        
        
        String caption = mCaption.getText().toString();
        String location = mLocation.getText().toString();

        DocumentReference ref = FirebaseFirestore.getInstance().collection("posts").document();
        postPushId = ref.getId();

        long timestamp = 1* new Date().getTime();

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a MMM d, ''yy");
        final String cur_time_and_date = sdf.format(c.getTime());

        HashMap< String , String > imageMap = new HashMap<>();
        HashMap< String , String > thumbMap = new HashMap<>();

        for(int i=0;i<imageUrls.size();i++){
            imageMap.put(  imageUrls.get(i)  , "image "+ (i+1) );
            thumbMap.put( thumbImageUrls.get(i) , "image " + (i+1));
        }


        postMap.put("primary_user_id" , FirebaseAuth.getInstance().getCurrentUser().getUid());
        postMap.put("secondary_user_id" , FirebaseAuth.getInstance().getCurrentUser().getUid());

        postMap.put("primary_push_id" , postPushId);
        postMap.put("secondary_push_id" , postPushId);

        postMap.put("post_image_url" , imageMap);
        postMap.put("post_thumb_url" , thumbMap);

        postMap.put("caption" , caption);
        postMap.put("time_and_date" , cur_time_and_date);
        postMap.put("time_stamp" ,timestamp );
        postMap.put("type" ,"own" );
        postMap.put("privacy" ,"public");

        postMap.put("location" , location);

        postMap.put("like_cnt" , 0);
        postMap.put("comment_cnt" ,0);
        postMap.put("share_cnt" ,0);

        post = new CombinedPosts(timestamp , mUserID , mUserID,  postPushId , postPushId , imageMap , thumbMap , cur_time_and_date , location , caption , "own" , "public" , 0,0,0 );

        if(caption.length() == 0 && imageUrls.size() == 0){
            Toast.makeText(this, "You Have Not sepcified aything in post ! ", Toast.LENGTH_SHORT).show();
        }else{
            uploadPost();
        }
    }

    public static String random() {
        int MAX_LENGTH = 100;
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public void addImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                imagesEncodedList = new ArrayList<String>();
                if(data.getData()!=null){
                    Uri uri=data.getData();
                    Cursor cursor = getContentResolver().query(uri,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        ViewGroup holder;

                        holder = findViewById(R.id.post_image_holder);

                        int dimen = (int) getResources().getDimension(R.dimen.feedback_image_size);

                        ImageView image = new ImageView(this);
                        image.setLayoutParams(new android.view.ViewGroup.LayoutParams(dimen, dimen));
                        image.setMaxHeight(dimen);
                        image.setMaxWidth(dimen);
                        image.setImageBitmap(bitmap);
                        image.setTag(uri);

                        holder.addView(image, holder.getChildCount()-1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    postImageUriArrayList.add(uri);


                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded  = cursor.getString(columnIndex);
                    cursor.close();

                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();

                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();

                            //from maps

                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                                ViewGroup holder;

                                holder = findViewById(R.id.post_image_holder);

                                int dimen = (int) getResources().getDimension(R.dimen.feedback_image_size);

                                ImageView image = new ImageView(this);
                                image.setLayoutParams(new android.view.ViewGroup.LayoutParams(dimen, dimen));
                                image.setMaxHeight(dimen);
                                image.setMaxWidth(dimen);
                                image.setImageBitmap(bitmap);
                                image.setTag(uri);

                                holder.addView(image, holder.getChildCount()-1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            postImageUriArrayList.add(uri);

                            //end maps


                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded  = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();

                        }
                        Log.v(TAG, "Selected Images" + postImageUriArrayList.size());
                    }
                }
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean isDataAvailable() {
        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private Task<Void> addRating( String mUserID  , int factor) {

        Log.d(TAG, "addRating:    "+mUserID);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        final DocumentReference ratingRef = FirebaseFirestore.getInstance().collection("ratings")
                .document(mUserID);
        return firebaseFirestore.runTransaction(transaction -> {

            Ratings ratings = transaction.get(ratingRef)
                    .toObject(Ratings.class);
            long curRating = ratings.getRating();
            long nextRating = curRating + factor;

            ratings.setRating(nextRating);
            transaction.set(ratingRef, ratings);
            return null;
        });
    }


}
