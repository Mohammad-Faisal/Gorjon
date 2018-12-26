package candor.fulki.chat.meeting;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import candor.fulki.general.MainActivity;
import candor.fulki.R;
import candor.fulki.models.MeetingRooms;
import candor.fulki.utils.PreferenceManager;
import id.zelory.compressor.Compressor;

public class CreateMeetingActivity extends AppCompatActivity {


    //---- WIDGETS -//
    private TextView mTitleText , mDetailsText , mTags;
    private Button mCreateChatRoomButton;
    private ImageView mCreateMeetingImage;
    private String image_download_url;
    ProgressBar progressBar;

    //------------FIREBASE -----//
    DatabaseReference mRootRef , mMeetingsRef , mNotificationsRef ;
    private StorageReference mPostsStorageRef;

    //----------VARIABLES---------//
    private String title  , details , moderator , type , notification_id , tag , mUserID , mUserName , mMeetingId , mMeetingImageUrl;
    private Uri selectedImageUri;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);

        //------widgets----
        mTitleText = findViewById(R.id.create_meeting_title_text);
        //mTags = findViewById(R.id.create_meeting_tags_text);
        mCreateChatRoomButton = findViewById(R.id.create_meeting_button);
        mCreateMeetingImage = findViewById(R.id.create_meeting_image);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        preferenceManager = new PreferenceManager(this);

        //--VARIABLES---//
        mUserID = preferenceManager.getUserId();
        mUserName  = preferenceManager.getUserName();
        moderator = mUserName;
        type = "open";
        mMeetingImageUrl = "default";

        //--- firebase
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mMeetingsRef = mRootRef.child("meetings");
        mPostsStorageRef = FirebaseStorage.getInstance().getReference().child("posts");

        mCreateMeetingImage.setOnClickListener(view -> CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(2,1)
                .setAllowRotation(true)
                .start(CreateMeetingActivity.this));
        mCreateChatRoomButton.setOnClickListener(view -> {
            mMeetingId = mMeetingsRef.push().getKey();
            details = "no details";
            title = mTitleText.getText().toString();
            tag = "default";
            if(image_download_url!=null){
                mMeetingImageUrl = image_download_url;
            }else{
                mMeetingImageUrl = "default";
            }
            final MeetingRooms meetingRooms = new MeetingRooms(title , type , details , tag ,moderator , mUserID , mMeetingId , "0" , mMeetingImageUrl);

            mMeetingsRef.child(mMeetingId).setValue(meetingRooms).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Intent invtePeopleIntent = new Intent(CreateMeetingActivity.this , InvitePeopleToMeetingActivity.class );
                    invtePeopleIntent.putExtra("meetingID" , meetingRooms);
                    startActivity(invtePeopleIntent);
                    finish();
                }
            });
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {


            progressBar.setVisibility(View.VISIBLE);

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri imagetUri = result.getUri();
                //compressing image
                Bitmap thumb_bitmap = null;
                File thumb_file = new File(imagetUri.getPath());
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(60)
                            .compressToBitmap(thumb_file);
                }catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                final byte[] thumb_byte = baos.toByteArray();
                selectedImageUri = imagetUri;
                mCreateMeetingImage.setImageURI(imagetUri);
                StorageReference imageFilePath = mPostsStorageRef.child(mUserID).child(mUserID+".jpg");
                imageFilePath.putFile(imagetUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Uri downloadUrlImage = task.getResult().getUploadSessionUri();
                            final String downLoadUriStringImage  = downloadUrlImage.toString();
                            image_download_url = downLoadUriStringImage;
                            progressBar.setVisibility(View.GONE);
                        }
                        else{
                            Toast.makeText(CreateMeetingActivity.this, "Error !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_open:
                if (checked)
                    type = "open";
                break;
            case R.id.radio_close:
                if (checked)
                    type = "closed";
                break;
            default:
                type = "open";
        }
    }
}
