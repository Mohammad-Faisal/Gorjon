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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import candor.fulki.adapters.CategoriesAdapter;
import candor.fulki.general.Functions;
import candor.fulki.general.MainActivity;
import candor.fulki.R;
import candor.fulki.home.CombinedHomeAdapter;
import candor.fulki.home.CombinedPosts;
import candor.fulki.models.Categories;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class RegistrationAccount extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


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


    Spinner mDivisionSpinner;
    Spinner mBloodSpinner;
    List< String > mDivisionList = new ArrayList<>();
    List < String > mBloodList = new ArrayList<>();

    private String districtString  ="", divisionString = "" , bloodString = "" , birthDateString = "" , professionString = "" , contactString = "";


    RecyclerView mRegCategoryRecycler;
    private CategoriesAdapter mCategoriesAdapter;
    private List<Categories> categoriesList;

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
        mRegCategoryRecycler = findViewById(R.id.category_recycler);



        setupSpinner();



        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            name  = firebaseUser.getDisplayName();
            mRegName.setText(name);
            if( firebaseUser.getPhotoUrl()!=null){
                photo  = firebaseUser.getPhotoUrl().toString();
                Timber.tag("Fulki").d("image url is    "+photo);
                photo = photo + "?height=480&width=480";
                new DownloadImage().execute(photo);
            }
        }


        //firebase
        mStorage = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mUserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        mRegPhoto.setOnClickListener(v -> Functions.BringImagePicker(RegistrationAccount.this));
        mRegCamera.setOnClickListener(v -> Functions.BringImagePicker(RegistrationAccount.this));

        mRegSave.setOnClickListener(v -> {
            name = mRegName.getText().toString();
            if(validateData()){
                uploadImageThenDetails();
            }
        });




        loadCategories();



    }

    private void loadCategories(){
        categoriesList = new ArrayList<>();
        mCategoriesAdapter = new CategoriesAdapter(categoriesList,RegistrationAccount.this, RegistrationAccount.this);
        mRegCategoryRecycler.setLayoutManager(new LinearLayoutManager(RegistrationAccount.this));
        mRegCategoryRecycler.setAdapter(mCategoriesAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();

        DocumentReference docRef = firebaseFirestore.collection("categories").document("categories");
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    Timber.tag("Fulki").d("DocumentSnapshot data: %s", document.getData());
                    List<String> categories = (List<String>) document.get("categories");

                    assert categories != null;
                    int len = categories.size();
                    for(int i=0;i<len;i++){
                        String category = categories.get(i);
                        Categories temp= new Categories(category , false);
                        categoriesList.add(temp);
                    }
                    mCategoriesAdapter.notifyDataSetChanged();
                } else {
                    Timber.tag("Fulki").d("No such document");
                }
            } else {
                Timber.tag("Fulki").d(task.getException(), "get failed with ");
            }
        });
    }

    private void uploadImageThenDetails(){
        mProgress = new ProgressDialog(RegistrationAccount.this);
        mProgress.setTitle("Saving Data.......");
        mProgress.setMessage("please wait while we create your account");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();



        imageFilePath.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Uri downloadUrlImage = taskSnapshot.getUploadSessionUri();
                    mainImageUrl =  downloadUrlImage.toString();
                    UploadTask uploadThumbTask = thumbFilePath.putBytes(thumb_byte);
                    uploadThumbTask.addOnFailureListener(exception -> {
                        mProgress.dismiss();
                        Toast.makeText(RegistrationAccount.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                        Timber.tag("Thumb  Photo Upload:  ").w(exception);
                    }).addOnSuccessListener(taskSnapshot1 -> {
                        Uri downloadUrlThumb = taskSnapshot1.getUploadSessionUri();
                        assert downloadUrlThumb != null;
                        thumbImageUrl  = downloadUrlThumb.toString();
                        Timber.d("uploadImage:    is succesfull ");
                        mProgress.dismiss();
                        uploadUserDetails();
                    });
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(RegistrationAccount.this, "Some error occured. check your internet connection", Toast.LENGTH_SHORT).show();
                    Timber.tag("Main Photo Upload   :  ").w(exception);
                });
    }

    private boolean validateData(){
        if(imageUri==null){
            Toast.makeText(RegistrationAccount.this, "please select your image", Toast.LENGTH_SHORT).show();
            return false;
        }else if(name.equals("")){
            Toast.makeText(RegistrationAccount.this, "please give us your name", Toast.LENGTH_SHORT).show();
            return false;
        }else if(mGender==2){
            Toast.makeText(RegistrationAccount.this, "Please Select your gender", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void uploadUserDetails(){

        Map < String, Object> userMap = getUserMap();
        Map< String, Object> rating = new HashMap<>();
        rating.put("rating" , 0);
        rating.put("user_name" , name);
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
    }

    private Map < String, Object> getUserMap(){
        String deviceTokenID = FirebaseInstanceId.getInstance().getToken();
        Map < String, Object> userMap = new HashMap<>();

        String name = mRegName.getText().toString();
        String userName = mRegUserName.getText().toString();
        String gender = "others";

        if(mGender==1){
            gender = "male";
        }else if(mGender==0){
            gender = "female";
        }

        userMap.put("name" , name);
        userMap.put("user_name" , "");
        userMap.put("user_id" , mUserID);
        userMap.put("gender" , gender);
        userMap.put("image" , mainImageUrl);
        userMap.put("thumb_image",thumbImageUrl);
        userMap.put("bio" , "");
        userMap.put("division", divisionString);
        userMap.put("blood_group",bloodString);
        userMap.put("birth_date" , "");
        userMap.put("contact_no" , "");
        userMap.put("email" , "");
        userMap.put("timestamp" , "");
        userMap.put("district" , "");
        userMap.put("lat" , 0);
        userMap.put("lng" , 0);
        userMap.put("rating" , 0);
        userMap.put("device_id" , deviceTokenID);
        return userMap;
    }

    private void setupSpinner(){


        String[] myResArray = getResources().getStringArray(R.array.divisions_array);
        mDivisionList = Arrays.asList(myResArray);

        myResArray = getResources().getStringArray(R.array.blood_array);
        mBloodList = Arrays.asList(myResArray);


        //adapter for spinner we changed the view by using layout given bellow
        mDivisionSpinner =  findViewById(R.id.settings_division_spinner);
        ArrayAdapter<CharSequence> mDivisionAdapter = ArrayAdapter.createFromResource(this,
                R.array.divisions_array, R.layout.spinner_layout);
        mDivisionAdapter.setDropDownViewResource(R.layout.spinner_layout);
        mDivisionSpinner.setAdapter(mDivisionAdapter);
        mDivisionSpinner.setAdapter(mDivisionAdapter);
        mDivisionSpinner.setOnItemSelectedListener(this);


        mBloodSpinner = findViewById(R.id.settings_blood_spinner);
        ArrayAdapter<CharSequence> mBloodAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_array, R.layout.spinner_layout);
        mBloodAdapter.setDropDownViewResource(R.layout.spinner_layout);
        mBloodSpinner.setAdapter(mBloodAdapter);
        mBloodSpinner.setAdapter(mBloodAdapter);
        mBloodSpinner.setOnItemSelectedListener(this);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch(parent.getId()){
            case R.id.settings_division_spinner:
                divisionString =  mDivisionSpinner.getSelectedItem().toString();
                break;
            case R.id.settings_blood_spinner:
                bloodString= mBloodSpinner.getSelectedItem().toString();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void handleMaleClick(View view) {
        mGender = 1; //male
        mRegMale.setBackgroundResource(R.drawable.textview_selected);
        mRegFemale.setBackgroundResource(R.drawable.textview_not_selected);
    }

    public void handleFemaleClick(View view) {
        mGender = 0 ;//female
        mRegMale.setBackgroundResource(R.drawable.textview_not_selected);
        mRegFemale.setBackgroundResource(R.drawable.textview_selected);
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
