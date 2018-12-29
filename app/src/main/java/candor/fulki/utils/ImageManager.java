package candor.fulki.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import candor.fulki.R;
import id.zelory.compressor.Compressor;

public class ImageManager {

    public static Bitmap getBitmapFromImageUri(Uri imageUri , Context context){
        Bitmap temp_bitmap = null;
        File temp_file = new File(imageUri.getPath());
        try {
            temp_bitmap = new Compressor(context)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(30)
                    .compressToBitmap(temp_file);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return temp_bitmap;
    }

    public static byte[] getByteArrayFrombitmap(Bitmap bitmap  , int quality){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

    public static byte[] getByteArrayFromImageUri(Uri imageUri, int quality  , Context context ){
        Bitmap temp_bitmap = getBitmapFromImageUri(imageUri, context);
        return getByteArrayFrombitmap(temp_bitmap , quality);
    }

    public static void BringImagePicker(Activity context) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(context);
    }

    public static void setImageWithGlide(String imageUrl , ImageView imageView, Context context){

        GlideApp.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_blank_profile)
                .into(imageView);
    }

}
