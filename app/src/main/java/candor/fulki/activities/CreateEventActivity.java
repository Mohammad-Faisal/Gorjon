package candor.fulki.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import candor.fulki.models.PostFiles;
import candor.fulki.models.Ratings;
import candor.fulki.R;
import id.zelory.compressor.Compressor;

public class CreateEventActivity extends AppCompatActivity {

    private static final String TAG = "CreateEventActivity";

    ImageView mCreateEventImage;
    EditText mCreateEventTitle , mCreateEventLocation , mCreateEventDescription;
    TextView mCreateEventDate;
    Button mCreateEventCreate;
    Uri imageUri;
    byte [] thumb_byte;
    private StorageReference imageFilePath;
    private StorageReference thumbFilePath;
    private ProgressDialog mProgress;

    String titleText , dateText = "", locationText , descriptionText;

    private DatePickerDialog.OnDateSetListener mDateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        getSupportActionBar().setTitle("Create an Event");
        getSupportActionBar().setHomeButtonEnabled(true);

        mCreateEventImage = findViewById(R.id.create_event_image);
        mCreateEventTitle = findViewById(R.id.create_event_title);
        mCreateEventCreate = findViewById(R.id.create_event_create);
        mCreateEventDate = findViewById(R.id.create_event_date);
        mCreateEventLocation = findViewById(R.id.create_event_location);
        mCreateEventDescription  =findViewById(R.id.create_event_description);

        mCreateEventImage.setOnClickListener(v -> BringImagePicker());
        mCreateEventCreate.setOnClickListener(v -> {
            if(check()){
                if(isDataAvailable()){
                    post();
                }else{
                    Toast.makeText(this, "Please enable your internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        });


        mCreateEventDate.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: clickedd on date picker!!!!!");
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    CreateEventActivity.this,
                    android.R.style.Theme_Holo_Dialog_MinWidth,
                    mDateListener,
                    year , month , day
            );
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            mDateListener = (view, year1, month1, dayOfMonth) -> {
                month1 = month1 +1;
                Log.d(TAG, "onDateSet: mm/dd/yyyy   "+ month1 +"/" + dayOfMonth + "/" + year1);
                dateText  = dayOfMonth+"/"+month1 + "/" + year1;
                mCreateEventDate.setText(dateText);
                mCreateEventDate.setText(dateText);
            };
        });

    }


    private boolean check() {
        titleText = mCreateEventTitle.getEditableText().toString();
        if(imageUri == null){
            Toast.makeText(this, "Please select and image", Toast.LENGTH_SHORT).show();
            return false;
        }else if(titleText.equals("")){
            Toast.makeText(this, "Please give a title to your event ", Toast.LENGTH_SHORT).show();
            return false;
        }else if(dateText.equals("")){
            Toast.makeText(this, "Please specify the date of your event ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void post(){

        locationText = mCreateEventLocation.getEditableText().toString();
        descriptionText = mCreateEventDescription.getEditableText().toString();

        Log.d(TAG, "post:  locationText  "  + locationText);
        Log.d(TAG, "post:  description  "  + descriptionText);
        Log.d(TAG, "post:  title  "  + titleText);
        Log.d(TAG, "post:  date  "  + dateText);


        mProgress = new ProgressDialog(CreateEventActivity.this);
        mProgress.setTitle("Creating Event.......");
        mProgress.setMessage("please wait it may take some time ");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a MMM d, ''yy");
        final String cur_time_and_date = sdf.format(c.getTime());


        final String mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String randomName = random();
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();



        //creating filepath for uploading the image
        imageFilePath = FirebaseStorage.getInstance().getReference().child("event_images").child(mUserID).child(randomName+".jpg");
        thumbFilePath = FirebaseStorage.getInstance().getReference().child("event_thumb_images").child(mUserID).child(randomName+".jpg");






        //uploading the main image
        imageFilePath.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {

                    Uri downloadUrlImage = taskSnapshot.getUploadSessionUri();
                    final String mainImageUrl =  downloadUrlImage.toString();
                    UploadTask uploadThumbTask = thumbFilePath.putBytes(thumb_byte);
                    uploadThumbTask.addOnFailureListener(exception -> {
                        Toast.makeText(CreateEventActivity.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                        Log.w("Thumb  Photo Upload:  " , exception);
                    }).addOnSuccessListener(taskSnapshot1 -> {
                        Uri downloadUrlThumb = taskSnapshot1.getUploadSessionUri();
                        final String thumbImageUrl  = downloadUrlThumb.toString();


                        DocumentReference ref = FirebaseFirestore.getInstance().collection("events").document();
                        String eventPushId = ref.getId();

                        long timestamp = 1* new Date().getTime();

                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();


                        Map<String , Object> postMap = new HashMap<>();
                        postMap.put("moderator_id" , mUserID);
                        postMap.put("title" , titleText);    //user name of post is equal to our title
                        postMap.put("image_url" , mainImageUrl);
                        postMap.put("thumb_image_url" , thumbImageUrl);
                        postMap.put("description" , descriptionText);
                        postMap.put("time_and_date" , dateText);
                        postMap.put("timestamp" ,timestamp);
                        postMap.put("event_push_id" , eventPushId);
                        postMap.put("location" , locationText);
                        postMap.put("people_cnt" , 0);
                        postMap.put("discussion_cnt",0);
                        postMap.put("clap_cnt" , 0);
                        postMap.put("love_cnt" , 0);


                        addRating(mUserID , 30);

                        //setting the path to file so that later we can delete this post
                        PostFiles postFiles = new PostFiles("event_images/"+mUserID+"/"+randomName+".jpg" ,"event_thumb_images/"+mUserID+"/"+randomName+".jpg", eventPushId);
                        firebaseFirestore.collection("images").document(mUserID).collection("events").document(eventPushId).set(postFiles);


                        firebaseFirestore.collection("events").document(eventPushId).set(postMap).addOnCompleteListener(task -> {
                            mProgress.dismiss();
                            if(task.isSuccessful()){
                                Toast.makeText(CreateEventActivity.this, "Success !", Toast.LENGTH_SHORT).show();
                                Intent mainIntent = new Intent(CreateEventActivity.this , ExploreActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }else{
                                Toast.makeText(CreateEventActivity.this, "There was an error !", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .addOnFailureListener(exception -> {
                    mProgress.dismiss();
                    Toast.makeText(CreateEventActivity.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                    Log.w("Main Photo Upload   :  " , exception);
                });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                imageUri = result.getUri();
                mCreateEventImage.setImageURI(imageUri);
                thumb_byte = CompressImage(imageUri , this );

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, "onActivityResult: ",error);
            }
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


    public void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(10, 9)
                .setMinCropResultSize(512 , 512)
                .start(CreateEventActivity.this);
    }

    private  final byte[] CompressImage(Uri imagetUri , Activity context){
        Bitmap thumb_bitmap = null;
        File thumb_file = new File(imagetUri.getPath());
        try {
            thumb_bitmap = new Compressor(context)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(50)
                    .compressToBitmap(thumb_file);
        }catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        final byte[] thumb_byte = baos.toByteArray();
        return thumb_byte;
    }


    private Task<Void> addRating( String mUserID  , int factor) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Log.d(TAG, "addRating:   function calledd !!!!");
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

    private boolean isDataAvailable() {
        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
