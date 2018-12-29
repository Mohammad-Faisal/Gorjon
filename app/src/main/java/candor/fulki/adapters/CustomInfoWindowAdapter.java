package candor.fulki.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import candor.fulki.R;
import candor.fulki.utils.ImageManager;
import de.hdodenhof.circleimageview.CircleImageView;


public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    String thumb_image;
    Context context;

    public CustomInfoWindowAdapter(Context context ) {
        this.context = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window_map, null);
        CircleImageView mImage = mWindow.findViewById(R.id.info_window_image);
    }

    private void rendowWindowText(Marker marker, View view){

        String title = marker.getTitle();
        TextView tvTitle = view.findViewById(R.id.title);
        CircleImageView markerImage;
        markerImage = view.findViewById(R.id.info_window_image);

        if(!title.equals("")){
            tvTitle.setText(title);
        }
        String snippet = marker.getSnippet();

        if(!snippet.equals("")){
            setImage(snippet,context , markerImage);
        }
    }


    @Override
    public View getInfoWindow(Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }

    private void setImage(final String imageURL, final Context context, CircleImageView mImage){
        ImageManager.setImageWithGlide(imageURL , mImage , context);
    }



}
