package candor.fulki;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

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

    public void setImage(final String imageURL , final Context context , CircleImageView mImage ){
        if(imageURL.equals("default")){

        }
        else{
            Picasso.with(context).load(imageURL).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.ic_blank_profile).into(mImage, new Callback() {
                @Override
                public void onSuccess() {
                    //do nothing if an image is found offline
                }
                @Override
                public void onError() {
                    Picasso.with(context).load(imageURL).placeholder(R.drawable.ic_blank_profile).into(mImage);
                }
            });
        }
    }



}
