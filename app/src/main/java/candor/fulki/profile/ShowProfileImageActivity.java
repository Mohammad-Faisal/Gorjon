package candor.fulki.profile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.nostra13.universalimageloader.core.ImageLoader;

import candor.fulki.R;
import candor.fulki.utilities.TouchImageView;


public class ShowProfileImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile_image);
        String url = getIntent().getStringExtra("url");
        String name = getIntent().getStringExtra("name");
        TouchImageView imgDetails = findViewById(R.id.showProfile_image);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(url , imgDetails);
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


}
