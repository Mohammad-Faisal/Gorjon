package candor.fulki.profile;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import candor.fulki.general.Functions;
import candor.fulki.general.MainActivity;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class RegistrationAccount extends AppCompatActivity {


    // widgets
    private TextView mRegMale , mRegFemale , mRegError;
    private CircleImageView mRegPhoto;
    private ProgressDialog mProgress;
    private EditText mRegName , mRegUserName;

    //variables
    private int mGender = 2;
    Uri imageUri;
    private String mainImageUrl = "";
    private String  thumbImageUrl = "";
    private String mUserID;

    //firebase
   private StorageReference mStorage;
   private FirebaseFirestore firebaseFirestore;
   private StorageReference imageFilePath;
   private StorageReference thumbFilePath;
   private byte[] thumb_byte;


    String photo=null;
    String name = null;
    String email = null;
    Bitmap userBitmap = null;


    ProgressDialog mProgressDialog ; // = new ProgressDialog();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_account);


        Objects.requireNonNull(getSupportActionBar()).setTitle("Create Your Account ");

        mProgressDialog = new ProgressDialog(RegistrationAccount.this);
        mProgressDialog.setTitle("Loading Data");
        mProgressDialog.setMessage("Please wait for some time ....");
        mProgressDialog.setIndeterminate(false);


        //widgets
        mRegName = findViewById(R.id.reg_name);
        mRegUserName = findViewById(R.id.reg_user_name);
        Button mRegCamera = findViewById(R.id.reg_camera);
        Button mRegSave = findViewById(R.id.reg_save);
        mRegPhoto = findViewById(R.id.reg_photo);
        mRegMale =findViewById(R.id.reg_male);
        mRegFemale =findViewById(R.id.reg_female);
        mRegError = findViewById(R.id.reg_error);
        mRegUserName.addTextChangedListener(filterTextWatcher);



        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            name  = firebaseUser.getDisplayName();
            mRegName.setText(name);
            if( firebaseUser.getPhotoUrl()!=null){
                photo  = firebaseUser.getPhotoUrl().toString();
                Timber.tag("Fulki").d("image url is    "+photo);
                //getBitmapFromURL(photo);
                photo = photo + "?height=480&width=480";
                new DownloadImage().execute(photo);
                //Functions.setUserImage(photo , this , mRegPhoto);

            }
            email = firebaseUser.getEmail();
        }


        //firebase
        mStorage = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mUserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        mRegMale.setOnClickListener(v -> {
            mGender = 1; //male
            mRegMale.setBackgroundResource(R.drawable.textview_selected);
            mRegFemale.setBackgroundResource(R.drawable.textview_not_selected);
        });

        mRegFemale.setOnClickListener(v -> {
            mGender = 0 ;//female
            mRegMale.setBackgroundResource(R.drawable.textview_not_selected);
            mRegFemale.setBackgroundResource(R.drawable.textview_selected);
        });



        mRegPhoto.setOnClickListener(v -> Functions.BringImagePicker(RegistrationAccount.this));
        mRegCamera.setOnClickListener(v -> Functions.BringImagePicker(RegistrationAccount.this));

        mRegSave.setOnClickListener(v -> {

            //now saving the data to firestore
            name = mRegName.getText().toString();
            String userName = mRegUserName.getText().toString();

            if(imageUri==null){
                Toast.makeText(RegistrationAccount.this, "please select and image", Toast.LENGTH_SHORT).show();
            }else if(name.equals("")){
                Toast.makeText(RegistrationAccount.this, "please give us your name", Toast.LENGTH_SHORT).show();
            }else if(userName.equals("")) {
                Toast.makeText(RegistrationAccount.this, "please give us your user name", Toast.LENGTH_SHORT).show();
            }else if(mGender==2){
                Toast.makeText(RegistrationAccount.this, "Please Select your gender", Toast.LENGTH_SHORT).show();
            }else if(userName.length()<6){
                mRegUserName.setError("username not specified !");
                Toast.makeText(RegistrationAccount.this, "User Name must be atleast 6 charactersr", Toast.LENGTH_SHORT).show();
            } else{

                mProgress = new ProgressDialog(RegistrationAccount.this);
                mProgress.setTitle("Saving Data.......");
                mProgress.setMessage("please wait while we create your account");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();


                //uploading the main image
                imageFilePath.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            Uri downloadUrlImage = taskSnapshot.getUploadSessionUri();
                            assert downloadUrlImage != null;
                            mainImageUrl =  downloadUrlImage.toString();
                            //uploading the thumb image
                            UploadTask uploadThumbTask = thumbFilePath.putBytes(thumb_byte);
                            uploadThumbTask.addOnFailureListener(exception -> {
                                Toast.makeText(RegistrationAccount.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                                Timber.tag("Thumb  Photo Upload:  ").w(exception);
                            }).addOnSuccessListener(taskSnapshot1 -> {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                Uri downloadUrlThumb = taskSnapshot1.getUploadSessionUri();
                                assert downloadUrlThumb != null;
                                thumbImageUrl  = downloadUrlThumb.toString();

                                //now saving the data to firestore
                                String name1 = mRegName.getText().toString();
                                String userName1 = mRegUserName.getText().toString();
                                String gender1 = "others";


                                if(mGender==1){
                                    gender1 = "male";
                                }else if(mGender==0){
                                    gender1 = "female";
                                }


                                String deviceTokenID = FirebaseInstanceId.getInstance().getToken();
                                Map < String, Object> userMap = new HashMap<>();

                                userMap.put("name" , name1);
                                userMap.put("user_name" , userName1);
                                userMap.put("user_id" , mUserID);
                                userMap.put("gender" , gender1);
                                userMap.put("image" , mainImageUrl);
                                userMap.put("thumb_image",thumbImageUrl);
                                userMap.put("bio" , "");
                                userMap.put("division", "Select One");
                                userMap.put("blood_group", "Select One");
                                userMap.put("birth_date" , "");
                                userMap.put("contact_no" , "");
                                userMap.put("email" , "");
                                userMap.put("timestamp" , "");
                                userMap.put("district" , "");
                                userMap.put("lat" , 0);
                                userMap.put("lng" , 0);
                                userMap.put("rating" , 0);
                                userMap.put("device_id" , deviceTokenID);

                                Map< String, Object> rating = new HashMap<>();
                                rating.put("rating" , 0);
                                rating.put("user_name" , userName1);
                                rating.put("user_id" , mUserID);
                                rating.put("thumb_image" , thumbImageUrl);
                                firebaseFirestore.collection("ratings").document(mUserID).set(rating);


                                firebaseFirestore.collection("users").document(mUserID).set(userMap).addOnCompleteListener(task -> {
                                    if(task.isSuccessful()){

                                        Intent mainIntent = new Intent(RegistrationAccount.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        mProgress.dismiss();
                                        finish();

                                    }else{
                                        mProgress.dismiss();
                                        String error = Objects.requireNonNull(task.getException()).getMessage();
                                        Toast.makeText(RegistrationAccount.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                                        Timber.d("onComplete: %s", error);
                                    }
                                });
                            });
                        })
                        .addOnFailureListener(exception -> {
                            mProgress.dismiss();
                            Toast.makeText(RegistrationAccount.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                            Timber.tag("Main Photo Upload   :  ").w(exception);
                        });
            }
        });
    }


    // DownloadImage AsyncTask
    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Set the bitmap into ImageView
            mRegPhoto.setImageBitmap(result);
            try {
                imageUri = getImageUri(RegistrationAccount.this , result);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mProgressDialog.dismiss();
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) throws IOException {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        tempDir.mkdir();
        File tempFile = File.createTempFile("title", ".jpg", tempDir);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        byte[] bitmapData = bytes.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(bitmapData);
        fos.flush();
        fos.close();
        return Uri.fromFile(tempFile);
    }



    private TextWatcher filterTextWatcher = new TextWatcher() {

        @SuppressLint("SetTextI18n")
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final String userName  = s.toString();
            int length= userName.length();
            if(length>=6){
                firebaseFirestore.collection("users").addSnapshotListener((documentSnapshots, e) -> {
                    if(e==null){
                        int done = 0;

                        assert documentSnapshots != null;
                        for(DocumentSnapshot doc : documentSnapshots){
                            String user_name = doc.getString("user_name");
                            Timber.d("now user name is   %s", user_name);
                            if(userName.equals(user_name)){
                                mRegError.setText("! user name not available");
                                mRegError.setTextColor(getResources().getColor(R.color.red_error));
                                mRegError.setVisibility(View.VISIBLE);
                                done =1;
                            }
                        }
                        if(done==0){
                            mRegError.setText("user name available :)");
                            mRegError.setTextColor(getResources().getColor(R.color.colorPrimary));
                            mRegError.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();
                mRegPhoto.setImageURI(imageUri);
                thumb_byte = Functions.CompressImage(imageUri , this);


                //creating filepath for uploading the image
                imageFilePath = mStorage.child("users").child(mUserID).child("Profile").child("profile_images").child(mUserID+".jpg");
                thumbFilePath = mStorage.child("users").child(mUserID).child("Profile").child("thumb_images").child(mUserID+".jpg");


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Timber.tag("Registration  :  ").w(error);
            }
        }
    }

}
