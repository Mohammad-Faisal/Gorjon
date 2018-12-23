package candor.fulki.general;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import candor.fulki.models.UserBasic;
import id.zelory.compressor.Compressor;

public class Functions {

    UserBasic userBasic = new UserBasic();

    public Functions() {
    }


    // give image uri and context and return byte array
    public static byte[] CompressImage(Uri imagetUri , Activity context){
        Bitmap thumb_bitmap = null;
        File thumb_file = new File(imagetUri.getPath());
        try {
            thumb_bitmap = new Compressor(context)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(30)
                    .compressToBitmap(thumb_file);
        }catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assert thumb_bitmap != null;
        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        final byte[] thumb_byte = baos.toByteArray();
        return thumb_byte;
    }


    public static void BringImagePicker(Activity context) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(context);
    }

    public static long getTimeStamp(){
        return new Date().getTime();
    }

    public UserBasic getUserBasicData(String mUserID){
        //setting user details

        FirebaseFirestore.getInstance().collection("users").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String mUserName = task.getResult().getString("name");
                    String mUserImage = task.getResult().getString("thumb_image");
                    String mUserThumbImage = task.getResult().getString("image");
                    userBasic = new UserBasic(mUserName , mUserThumbImage , mUserImage);
                }
            }
        });
        return userBasic;
    }

}
