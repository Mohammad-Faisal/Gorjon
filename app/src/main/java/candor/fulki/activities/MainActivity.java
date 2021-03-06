package candor.fulki.activities;

import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Window;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import candor.fulki.R;
import candor.fulki.activities.home.HomeActivity;
import candor.fulki.activities.profile.RegistrationAccountActivity;
import candor.fulki.utils.Functions;
import candor.fulki.utils.PreferenceManager;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {



    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 1;


private String mUserID , mUserName , mUserThumbImage , mUserImage , mUserGender, mUserDivision , mUserDistrict , mUserBlood;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setElevation(1);



        preferenceManager = new PreferenceManager(this);


        mAuth = FirebaseAuth.getInstance();

        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            mUserID = mUser.getUid();
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(mUserID);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (!document.exists()) {
                        Intent regIntent = new Intent(MainActivity.this, RegistrationAccountActivity.class);
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

        }else {
            startSignInFlow();
        }
    }

    private void startSignInFlow(){
        AuthUI.IdpConfig facebookIdp = new AuthUI.IdpConfig.FacebookBuilder()
                .setPermissions(Arrays.asList("user_friends" , "user_gender" ,
                        "user_hometown","user_birthday","email" ,"public_profile" ))
                .build();

        List<IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                facebookIdp
        );
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.fulki_logo)
                        .setTheme(R.style.AppTheme)
                        .build(),
                RC_SIGN_IN);
    }
    private void saveToSharedPref(){

        preferenceManager.setBirthDate("");
        preferenceManager.setBlood(mUserBlood);
        preferenceManager.setDivision(mUserDivision);
        preferenceManager.setGender(mUserGender);
        preferenceManager.setUserId(mUserID);
        preferenceManager.setUserName(mUserName);
        preferenceManager.setUserImage(mUserImage);
        preferenceManager.setUserThumbImage(mUserThumbImage);
        preferenceManager.setDistrict("");
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                mUserID = Objects.requireNonNull(user).getUid();
                DocumentReference docRef = db.collection("users").document(mUserID);
                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (!document.exists()) {
                            Intent regIntent = new Intent(MainActivity.this, RegistrationAccountActivity.class);
                            startActivity(regIntent);
                            finish();
                        }else{

                            mUserName = task.getResult().getString("name");
                            mUserThumbImage = task.getResult().getString("thumb_image");
                            mUserImage = task.getResult().getString("image");
                            mUserBlood = task.getResult().getString("blood_group");
                            mUserDistrict = task.getResult().getString("district");
                            mUserDivision = task.getResult().getString("division");
                            mUserGender = task.getResult().getString("gender");
                            mUserID = task.getResult().getString("user_id");

                        //    FirebaseFirestore.getInstance().collection("device_ids").document(mUserID).fieldset(device_id);

                            saveToSharedPref();

                            Intent homeIntent = new Intent(MainActivity.this , HomeActivity.class);
                            startActivity(homeIntent);
                            finish();
                        }
                    } else {
                        Timber.d(task.getException(), "get failed with ");
                    }
                });
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "No Response.... Please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No Internet Connection.... Please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, "An unknown error occured.... Please try again", Toast.LENGTH_SHORT).show();
                Timber.e(response.getError(), "Sign-in error: ");
            }
        }
    }




    @Override
    protected void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(mAuthStateListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        //mAuth.removeAuthStateListener(mAuthStateListener);
    }

}
