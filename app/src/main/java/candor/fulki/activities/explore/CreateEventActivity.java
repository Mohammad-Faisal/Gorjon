package candor.fulki.activities.explore;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import candor.fulki.R;
import candor.fulki.models.PostFiles;
import candor.fulki.utils.Functions;
import candor.fulki.utils.ImageManager;
import candor.fulki.utils.PreferenceManager;
import timber.log.Timber;

public class CreateEventActivity extends AppCompatActivity {

    private static final String TAG = "CreateEventActivity";

    ImageView mCreateEventImage;
    EditText mCreateEventTitle , mCreateEventLocation , mCreateEventDescription;
    TextView mCreateEventDate;
    Button mCreateEventCreate;
    Uri imageUri;
    byte [] thumb_byte;
    byte [] main_byte;
    private StorageReference imageFilePath;
    private StorageReference thumbFilePath;
    private ProgressDialog mProgress;

    String titleText , dateText = "", locationText , descriptionText , mainImageUrl , thumbImageUrl , mUserID , randomName;

    FirebaseFirestore firebaseFirestore;
    private DatePickerDialog.OnDateSetListener mDateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Create an Event");
        getSupportActionBar().setHomeButtonEnabled(true);

        PreferenceManager preferenceManager=  new PreferenceManager(this);
        mUserID = preferenceManager.getUserId();
        firebaseFirestore = FirebaseFirestore.getInstance();
        randomName = random();

        mCreateEventImage = findViewById(R.id.create_event_image);
        mCreateEventTitle = findViewById(R.id.create_event_title);
        mCreateEventCreate = findViewById(R.id.create_event_create);
        mCreateEventDate = findViewById(R.id.create_event_date);
        mCreateEventLocation = findViewById(R.id.create_event_location);
        mCreateEventDescription  =findViewById(R.id.create_event_description);

        mCreateEventImage.setOnClickListener(v -> BringImagePicker());
        mCreateEventCreate.setOnClickListener(v -> {
            if(check()){
                if(Functions.isDataAvailable(this)){
                    post();
                }else{
                    Toast.makeText(this, "Please enable your internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        });


        mCreateEventDate.setOnClickListener(v -> {
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
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            mDateListener = (view, year1, month1, dayOfMonth) -> {
                month1 = month1 +1;
                Timber.d("onDateSet: mm/dd/yyyy   " + month1 + "/" + dayOfMonth + "/" + year1);
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

    @SuppressLint({"ThrowableNotAtBeginning", "TimberArgCount"})
    public void post(){

        locationText = mCreateEventLocation.getEditableText().toString();
        descriptionText = mCreateEventDescription.getEditableText().toString();

        mProgress = new ProgressDialog(CreateEventActivity.this);
        mProgress.setTitle("Creating Event.......");
        mProgress.setMessage("please wait it may take some time ");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        Calendar c = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("h:mm a MMM d, ''yy");






        //creating filepath for uploading the image
        imageFilePath = FirebaseStorage.getInstance().getReference().child("event_images").child(mUserID).child(randomName+".jpg");
        thumbFilePath = FirebaseStorage.getInstance().getReference().child("event_thumb_images").child(mUserID).child(randomName+".jpg");



        UploadTask uploadThumbTask = thumbFilePath.putBytes(thumb_byte);
        //UploadTask uploadImageTask = imageFilePath.putBytes(main_byte);
        UploadTask uploadImageTask = imageFilePath.putFile(imageUri);

        uploadImageTask.addOnSuccessListener(taskSnapshot -> {
            if(uploadImageTask.isSuccessful()){
                imageFilePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    mainImageUrl  = uri.toString();
                    uploadThumbTask.addOnSuccessListener(taskSnapshot12 -> {
                        if(uploadThumbTask.isSuccessful()){
                            thumbFilePath.getDownloadUrl().addOnSuccessListener(uri1 -> {
                                thumbImageUrl  = uri1.toString();
                                mProgress.dismiss();
                                uploadEventDetails();
                            });
                        }else{
                            Timber.d("uploadImages:    %s", taskSnapshot12.getError());
                        }
                    });
                });
            }else{
                Timber.d(taskSnapshot.getError(), "uploadImages:    %s");
            }
        });

    }

    private void uploadEventDetails() {
        DocumentReference ref = FirebaseFirestore.getInstance().collection("events").document();
        String eventPushId = ref.getId();

        long timestamp = new Date().getTime();

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


        addRating(mUserID , 15);

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
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                mCreateEventImage.setImageURI(imageUri);
                thumb_byte =ImageManager.getByteArrayFromImageUri(imageUri , 30 , this);
                main_byte  = ImageManager.getByteArrayFromImageUri(imageUri , 100 , this);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Timber.e(error, "onActivityResult: ");
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


    private void addRating( String mUserID  , int factor) {
        Functions.addRating(mUserID,factor);
    }


}
