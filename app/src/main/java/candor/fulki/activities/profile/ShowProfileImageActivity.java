package candor.fulki.activities.profile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import java.util.Objects;

import candor.fulki.R;
import candor.fulki.utils.ImageManager;
import candor.fulki.utils.TouchImageView;


public class ShowProfileImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile_image);
        String url = getIntent().getStringExtra("url");
        String name = getIntent().getStringExtra("name");
        TouchImageView imgDetails = findViewById(R.id.showProfile_image);
        ImageManager.setImageWithGlide(url , imgDetails,this);
        Objects.requireNonNull(getSupportActionBar()).setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


}
