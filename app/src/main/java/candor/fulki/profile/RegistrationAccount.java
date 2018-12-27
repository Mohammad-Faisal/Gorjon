package candor.fulki.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoginStatusCallback;
import com.facebook.login.LoginManager;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

import candor.fulki.adapters.CategoriesAdapter;
import candor.fulki.general.Functions;
import candor.fulki.general.MainActivity;
import candor.fulki.R;

import candor.fulki.home.CreatePostActivity;
import candor.fulki.home.HomeActivity;
import candor.fulki.models.Categories;
import candor.fulki.utils.PreferenceManager;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class RegistrationAccount extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    // widgets
    private TextView mRegMale , mRegFemale , mRegError;
    private CircleImageView mRegPhoto;
    private ProgressDialog mProgress;
    private EditText mRegName , mRegUserName;
    Button mRegSave ,mRegCamera;

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
    private String genderString = "others";

    RecyclerView mRegCategoryRecycler;
    private CategoriesAdapter mCategoriesAdapter;
    private List<Categories> categoriesList;

    private byte[] main_bytes;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_account);


        Objects.requireNonNull(getSupportActionBar()).setTitle("Create Your Account ");


        initViews();
        setupSpinner();
        loadCategories();
        getFacebookDetails();

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

    }


    private void uploadImageThenDetails(){
        mProgress = new ProgressDialog(RegistrationAccount.this);
        mProgress.setTitle("Saving Data.......");
        mProgress.setMessage("please wait while we create your account");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();


        //creating filepath for uploading the image
        imageFilePath = mStorage.child("users").child(mUserID).child("Profile").child("profile_images").child(mUserID+".jpg");
        thumbFilePath = mStorage.child("users").child(mUserID).child("Profile").child("thumb_images").child(mUserID+".jpg");


        UploadTask uploadThumbTask = thumbFilePath.putBytes(thumb_byte);
        UploadTask uploadImageTask = imageFilePath.putBytes(main_bytes);

        uploadImageTask.addOnSuccessListener(taskSnapshot -> {
            if(uploadImageTask.isSuccessful()){
                imageFilePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    mainImageUrl  = uri.toString();
                    uploadThumbTask.addOnSuccessListener(taskSnapshot12 -> {
                        if(uploadThumbTask.isSuccessful()){
                            thumbFilePath.getDownloadUrl().addOnSuccessListener(uri1 -> {
                                thumbImageUrl  = uri1.toString();
                                mProgress.dismiss();
                                uploadUserDetails();
                            });
                        }else{
                            Timber.d("uploadImages:    %s", taskSnapshot12.getError());
                        }
                    });
                });
            }else{
                Timber.d("uploadImages:    %s", taskSnapshot.getError());
            }
        });
    }

    private boolean validateData(){
        if(main_bytes == null){
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

        Timber.tag("Fulki").d("The details of the user is  ... %s", userMap.toString());

        //setting algolia search
        Client client = new Client( "YWTL46QL1P" , "fcdc55274ed56d6fb92f51c0d0fc46a0" );
        Index index = client.getIndex("users");
        List<JSONObject> userList = new ArrayList<>();
        userList.add(new JSONObject(userMap));
        index.addObjectsAsync(new JSONArray(userList), null);



        Map< String, Object> rating = new HashMap<>();
        rating.put("rating" , 0);
        rating.put("name" , name);
        rating.put("user_id" , mUserID);
        rating.put("thumb_image" , thumbImageUrl);



        WriteBatch writeBatch  = firebaseFirestore.batch();


        Map< String, String> odvut = new HashMap<>();
        for(int i=0;i<categoriesList.size();i++){
            String category = categoriesList.get(i).getName();
            boolean selected = categoriesList.get(i).getSelected();
            if(selected){
                odvut.put(category , category);
            }
        }

        DocumentReference ratingRef = firebaseFirestore.collection("ratings").document(mUserID);
        DocumentReference userRef = firebaseFirestore.collection("users").document(mUserID);
        DocumentReference categoryRef = firebaseFirestore.collection("categories").document(mUserID);

        writeBatch.set(ratingRef, rating);
        writeBatch.set(userRef , userMap);
        writeBatch.set(categoryRef,odvut);


        saveToSharedPref();


        writeBatch.commit().addOnSuccessListener(aVoid -> {
            Intent homeIntent = new Intent(RegistrationAccount.this, MainActivity.class);
            startActivity(homeIntent);
            mProgress.dismiss();
            finish();
            Timber.d("first time data upload is successful");

        }).addOnFailureListener(e -> {
            mProgress.dismiss();
            Timber.d("  aditional data upload not succesful");
        });
    }

    private Map < String, Object> getUserMap(){
        String deviceTokenID = FirebaseInstanceId.getInstance().getToken();
        Map < String, Object> userMap = new HashMap<>();

        String name = mRegName.getText().toString();
        genderString=  (mGender == 1 ) ? "male" : "female";


        userMap.put("name" , name);
        userMap.put("user_name" , "");
        userMap.put("user_id" , mUserID);
        userMap.put("gender" , genderString);
        userMap.put("image" , mainImageUrl);
        userMap.put("thumb_image",thumbImageUrl);
        userMap.put("bio" , "");
        userMap.put("division", divisionString);
        userMap.put("blood_group",bloodString);
        userMap.put("birth_date" , "");
        userMap.put("contact_no" , "");
        userMap.put("email" , email);
        userMap.put("district" , "");
        userMap.put("lat" , 0);
        userMap.put("lng" , 0);
        userMap.put("rating" , 0);
        userMap.put("timestamp" , Functions.getTimeStamp());
        userMap.put("device_id" , deviceTokenID);

        return userMap;
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
                InputStream input = new java.net.URL(imageURL).openStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mRegPhoto.setImageBitmap(result);

            if(result!=null){
                Timber.tag("Fulki").d("bitmap is not null");
                Functions.createLog("bitmap is not null");


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                result.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                main_bytes = baos.toByteArray();

            }

            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();
                mRegPhoto.setImageURI(imageUri);
                thumb_byte = Functions.CompressImage(imageUri , this);


                InputStream iStream = null;
                try {
                    iStream = getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    main_bytes = getBytes(iStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }




            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Timber.tag("Registration  :  ").w(error);
            }
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
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

    public void initViews(){
        mProgressDialog = new ProgressDialog(RegistrationAccount.this);
        mProgressDialog.setTitle("Loading Data");
        mProgressDialog.setMessage("Please wait for some time ....");
        mProgressDialog.setIndeterminate(false);


        //widgets
        mRegName = findViewById(R.id.reg_name);
        mRegUserName = findViewById(R.id.reg_user_name);
        mRegCamera = findViewById(R.id.reg_camera);
        mRegSave = findViewById(R.id.reg_save);
        mRegPhoto = findViewById(R.id.reg_photo);
        mRegMale =findViewById(R.id.reg_male);
        mRegFemale =findViewById(R.id.reg_female);
        mRegError = findViewById(R.id.reg_error);
        mRegUserName.addTextChangedListener(filterTextWatcher);
        mRegCategoryRecycler = findViewById(R.id.category_recycler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_out:
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                Intent mainIntent = new Intent(RegistrationAccount.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private void loadCategories(){
        categoriesList = new ArrayList<>();
        mCategoriesAdapter = new CategoriesAdapter(categoriesList,RegistrationAccount.this, RegistrationAccount.this);
        mRegCategoryRecycler.setLayoutManager(new LinearLayoutManager(RegistrationAccount.this));
        mRegCategoryRecycler.setAdapter(mCategoriesAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();

        DocumentReference docRef = firebaseFirestore.collection("categories_list").document("categories_list");
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    Timber.tag("Fulki").d("DocumentSnapshot data: %s", document.getData());
                    List<String> categories = (List<String>) document.get("categories_list");

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
    private void getFacebookDetails(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if(isLoggedIn){
            GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {

                    try {
                        Timber.tag("Fulki").d(object.toString());
                        if ( object.has( "first_name" ) )
                        {
                            String firstName = object.getString( "first_name" );
                            Functions.createLog(firstName);
                        }
                        if ( object.has( "last_name" ) )
                        {
                            String lastName = object.getString( "last_name" );
                            Functions.createLog(lastName);
                        }
                        if ( object.has( "email" ) )
                        {
                            email = object.getString( "email" );
                            Functions.createLog(email);
                        }
                        if ( object.has( "birthday" ) )
                        {
                            String birthday = object.getString( "birthday" );
                            Functions.createLog(birthday);
                        }
                        if ( object.has( "gender" ) )
                        {
                            String gender = object.getString( "gender" );
                            Functions.createLog(gender);
                        }
                    }catch ( JSONException e )
                    {
                        e.printStackTrace();
                    }

                }
            });
            Bundle parameters = new Bundle();
            parameters.putString( "fields", "id, first_name, last_name, email, birthday, gender" );
            request.setParameters( parameters );
            request.executeAsync();
        }
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
    private void saveToSharedPref(){
        PreferenceManager preferenceManager = new PreferenceManager(this);
        preferenceManager.setBirthDate("");
        preferenceManager.setBlood(bloodString);
        preferenceManager.setDivision(divisionString);
        preferenceManager.setGender(genderString);
        preferenceManager.setUserId(mUserID);
        preferenceManager.setUserName(name);
        preferenceManager.setUserImage(mainImageUrl);
        preferenceManager.setUserThumbImage(thumbImageUrl);
        preferenceManager.setDistrict("");
        preferenceManager.setLoggedIn(true);
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


}
