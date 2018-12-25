package candor.fulki.general;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.firebase.ui.auth.AuthUI.IdpConfig.Builder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import candor.fulki.home.HomeActivity;
import candor.fulki.profile.ProfileSettingsActivity;
import candor.fulki.R;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {



    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 1;



    public static String mUserID = "";
    public static String mUserName = "";
    public static String mUserThumbImage = "";
    public static String mUserImage = "";


    public static final String SHARED_PREF_NAME = "UserBasics" ;
    public static final String Name = "nameKey";
    public static final String Id = "idKey";
    public static final String ThumbImage = "thumbKey";
    public static final String Image = "imageKey";



    //----------- FIREBASE -----//
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setElevation(1);

            mAuth = FirebaseAuth.getInstance();
            if(!isDataAvailable()){
                Toast.makeText(this, "Please enable your data to continue", Toast.LENGTH_SHORT).show();
            }else{
                mAuthStateListener = firebaseAuth -> {
                    final FirebaseUser mUser = firebaseAuth.getCurrentUser();
                    if (mUser != null) {
                        mUserID = mUser.getUid();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference docRef = db.collection("users").document(mUserID);
                        docRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                assert document != null;
                                if (!document.exists()) {
                                    Intent regIntent = new Intent(MainActivity.this, ProfileSettingsActivity.class);
                                    startActivity(regIntent);
                                    finish();
                                }else{


                                    SharedPreferences sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();

                                    editor.putString(Name, task.getResult().getString("name"));
                                    editor.putString(Image, task.getResult().getString("image"));
                                    editor.putString(ThumbImage, task.getResult().getString("thumb_image"));
                                    editor.putString(Id, mUserID);
                                    editor.apply();


                                    mUserName = task.getResult().getString("name");
                                    mUserThumbImage = task.getResult().getString("thumb_image");
                                    mUserImage = task.getResult().getString("image");

                                    Intent homeIntent = new Intent(MainActivity.this , HomeActivity.class);
                                    startActivity(homeIntent);
                                    finish();
                                }
                            } else {
                                Timber.d(task.getException(), "get failed with ");
                            }
                        });
                    }else {



                        List<IdpConfig> providers = Arrays.asList(
                                new AuthUI.IdpConfig.PhoneBuilder().build(),
                                new AuthUI.IdpConfig.FacebookBuilder().build()
                        );

                        /*List<IdpConfig> providers = Arrays.asList(
                                new AuthUI.IdpConfig.PhoneBuilder().build()
                        );*/

                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setAvailableProviders(providers)
                                        .setLogo(R.drawable.fulki_logo)
                                        .setTheme(R.style.AppTheme)
                                        .build(),
                                RC_SIGN_IN);
                    }
                };
            }

        }


    private boolean isDataAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){

                String accessToken = data.getDataString();
                Timber.tag("Fulki").d("information of login   "+accessToken);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                mUserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                DocumentReference docRef = db.collection("users").document(mUserID);
                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (!document.exists()) {
                            Intent regIntent = new Intent(MainActivity.this, ProfileSettingsActivity.class);
                            startActivity(regIntent);
                            finish();
                        }else{

                            Intent homeIntent = new Intent(MainActivity.this , HomeActivity.class);
                            startActivity(homeIntent);
                            finish();
                        }
                    } else {
                        Timber.d(task.getException(), "get failed with ");
                    }
                });

            }else{
                Toast.makeText(this, "Failed for some reason please check your internet connnection ", Toast.LENGTH_SHORT).show();
            }

        }
    }




    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

}
