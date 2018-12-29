package candor.fulki.activities;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import candor.fulki.R;
import candor.fulki.activities.profile.ProfileActivity;
import candor.fulki.adapters.CustomInfoWindowAdapter;
import candor.fulki.adapters.HorizontalRecyclerAdapterfortopic;
import candor.fulki.models.Locationdetail;
import candor.fulki.utils.Functions;
import candor.fulki.utils.PreferenceManager;
import timber.log.Timber;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        HorizontalRecyclerAdapterfortopic.OnButtonClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private ArrayList<Locationdetail> arrayList=new ArrayList<>();
    Map<Marker, String> markerMap = new HashMap<Marker, String>();
    String mUserID  =FirebaseAuth.getInstance().getCurrentUser().getUid();
    ArrayList<String> subList1 = new ArrayList<>();
    Map< String , String>  category_key = new HashMap<>();

    private static final int RC_CHECK_PERMISSION_LOCATION = 2;

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    RC_CHECK_PERMISSION_LOCATION);

        }
    }


    @Override
    public void onButtonClick(int position) {
        final FirebaseFirestore firebaseFirestore  = FirebaseFirestore.getInstance();
        mMap.clear();
        markerMap.clear();
        int len=arrayList.size();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
            checkPermission();
        }



        String topic = subList1.get(position);
        Log.d(TAG, "onButtonClick:   on button click  "+topic );

        for(int i=0;i<len;i++){
            Locationdetail locationdetail = arrayList.get(i);
            String uid = locationdetail.getUser_id();
            firebaseFirestore.collection(category_key.get(topic)).document(uid).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){

                        Log.d(TAG, "onButtonClick:   this user exist in the category of   "+topic);
                        LatLng latLng = new LatLng(locationdetail.getLat() , locationdetail.getLng());

                        String snippet = topic;
                        MarkerOptions options = new MarkerOptions()
                                .position(latLng)
                                .title(locationdetail.getUser_name())
                                .snippet(locationdetail.getThumb_image())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_grey));

                        Marker userMarker=mMap.addMarker(options);
                        markerMap.put(userMarker,uid);
                    }
                }
            });
        }
    }

    private GoogleMap mMap;
    Button setvalue;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private Location mLastKnownLocation;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private CameraPosition mCameraPosition;


    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private FusedLocationProviderClient mFusedLocationProviderClient;


    LatLng dhaka = new LatLng(23.7256, 90.3925);
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private GoogleApiClient googleApiClient;
    String address;

    PreferenceManager preferenceManager;
    private String mUserName, mUserThumbImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



            Log.d(TAG, "onCreate: suru");

            if (savedInstanceState != null) {
                mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
                mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
            }
            setContentView(R.layout.activity_maps);
            setvalue= findViewById(R.id.setvalue);


        preferenceManager = new PreferenceManager(this);
        mUserID = preferenceManager.getUserId();
        mUserName = preferenceManager.getUserName();
        mUserThumbImage = preferenceManager.getUserThumbImage();

            if(!Functions.isDataAvailable(this)){
                Toast.makeText(this, "Please turn on data", Toast.LENGTH_SHORT).show();
                finish();
            }
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


            subList1.add("child marriage");
            subList1.add("violence");
            subList1.add("environment");
            subList1.add("women empowerment");
            subList1.add("education");
            subList1.add("A+ blood");
            subList1.add("A- blood");
            subList1.add("B+ blood");
            subList1.add("B- blood");
            subList1.add("AB+ blood");
            subList1.add("AB- blood");
            subList1.add("O+ blood");
            subList1.add("O- blood");


            category_key.put("child marriage" , "child_marrige");
            category_key.put("violence" , "violence");
            category_key.put("environment" , "environment");
            category_key.put("education" , "education");
            category_key.put("women empowerment" , "women_empowerment");

            category_key.put("A+ blood" , "A+");
            category_key.put("A- blood" , "A-");
            category_key.put("B+ blood" , "B+");
            category_key.put("B- blood" , "B-");
            category_key.put("AB+ blood" , "AB+");
            category_key.put("AB- blood" , "AB-");
            category_key.put("O+ blood" , "O+");
            category_key.put("O- blood" ,"O-");






            RecyclerView mRecyclerView = findViewById(R.id.topiclistmap);
            mRecyclerView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mRecyclerView.setLayoutManager(layoutManager);
            HorizontalRecyclerAdapterfortopic adapter = new HorizontalRecyclerAdapterfortopic(subList1,this);
            mRecyclerView.setAdapter(adapter);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Timber.d("onMapReady:   entered !!!!!!!!!!!!!!");
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dhaka, DEFAULT_ZOOM));
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
        mMap.setOnInfoWindowClickListener(marker -> {
            String uid = markerMap.get(marker);
            if(uid != null) {

                Intent profileIntent = new Intent(MapsActivity.this , ProfileActivity.class);
                profileIntent.putExtra("userID" , uid);
                startActivity(profileIntent);
            }
        });



        getLocationPermission();
        updateLocationUI();
        settingsRequest2(MapsActivity.this);
        changeinlocationdata();
        setvalue.setOnClickListener(view -> getDeviceLocation());
        mMap.setOnMarkerClickListener(markerfinal -> {
            try{
                if(markerfinal.isInfoWindowShown()){
                    markerfinal.hideInfoWindow();
                }else{
                    markerfinal.showInfoWindow();
                }
            }catch (NullPointerException e){
                Timber.e("onClick: NullPointerException: " + e.getMessage());
            }
            return false;
        });
    }


    private void changeinlocationdata(){
        Timber.d("changeinlocationdata: locaiton changed !!");
        arrayList.clear();

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("locations").addSnapshotListener((documentSnapshots, e) -> {
            if(e!=null){
                Timber.tag(TAG).w(e, "onEvent:  listen failed ");
            }
            if(!documentSnapshots.isEmpty()){
                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        String name = doc.getDocument().get("user_name").toString();
                        String user_id = doc.getDocument().get("user_id").toString();
                        String thumb_image = doc.getDocument().get("thumb_image").toString();
                        String place_name = doc.getDocument().get("place_name").toString();
                        double lat = doc.getDocument().getDouble("lat");
                        double lng = doc.getDocument().getDouble("lng");
                        Timber.d("changeinlocationdata:   got name   " + lat);
                        Locationdetail singleLocation  = new Locationdetail(name , user_id , thumb_image ,lat , lng , place_name);
                        arrayList.add(singleLocation);
                    }
                }
            }
        });
    }



    private void getDeviceLocation() {
        Timber.d("getDeviceLocation:      entered ");
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnSuccessListener(this, location -> {
                    if (location != null) {
                        mLastKnownLocation=location;
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        address=getaddress(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                        addtodatabase();
                    }
                    else{
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dhaka, DEFAULT_ZOOM));
                    }
                });
            }
        } catch (SecurityException e)  {
            Timber.e(e);
        }
    }

    private void addtodatabase() {
        Timber.d("addtodatabase:   enteredd !!!  ");
        final LatLng latLng=new LatLng(mLastKnownLocation.getLatitude(),
                mLastKnownLocation.getLongitude());
        String address = getaddress(latLng.latitude , latLng.longitude);
        Locationdetail locationdetail = new Locationdetail(mUserName , mUserID, mUserThumbImage,latLng.latitude, latLng.longitude,address);
        FirebaseFirestore.getInstance().collection("locations").document(mUserID).set(locationdetail);
        addmarkertodata( mUserName  ,mUserID, mUserThumbImage , latLng , address);
    }

    private void addmarkertodata(String mUserName ,  String mUserID , String mUserThumbImage ,  LatLng latlng, String  placename) {
        markerinfo(mMap,mUserThumbImage,latlng , mUserName);
    }

    private void markerinfo(GoogleMap googleMap , String thumb_image, LatLng latLng , String userName){
        if(thumb_image != null){
            try{
                String snippet = "user";
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(userName)
                        .snippet(thumb_image)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_green));
                googleMap.addMarker(options);

            }catch (NullPointerException e){
                Timber.e("moveCamera: NullPointerException: %s", e.getMessage());
            }
        }else{
            String snippet = "\n"+"user: "+userName +
                    "\n"+"Address: " + "not found"  ;
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title("Information: "+ "\n")
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_green));
            googleMap.addMarker(options);
        }

    }


    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }
    private void updateLocationUI() {
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);

            } else {
                mMap.setMyLocationEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Timber.e(e);
        }
    }
    public void settingsRequest2(final Activity activity) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        SettingsClient client = LocationServices.getSettingsClient(activity);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(activity, locationSettingsResponse -> getDeviceLocation());

        task.addOnFailureListener(activity, e -> {
            int statusCode = ((ApiException) e).getStatusCode();
            switch (statusCode) {
                case CommonStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(activity ,REQUEST_CHECK_SETTINGS);

                    } catch (IntentSender.SendIntentException sendEx) {
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    break;
            }
        });
    }


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getDeviceLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
            googleApiClient.connect();


        super.onStart();
        Timber.d("onStart: suru");

    }

    @Override
    protected void onStop() {
        // googleApiClient.disconnect();
        super.onStop();
    }

    private String getaddress(double latitude,double longitude ){
        String add="";
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            add = addresses.get(0).getAddressLine(0);
        }
        return add;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume: suru");


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(this, "Location on ! ", Toast.LENGTH_SHORT).show();
                        getDeviceLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        settingsRequest2(MapsActivity.this);
                        break;
                }
                break;
        }
    }


    private void addRating(String mUserID  , int factor) {
       Functions.addRating(mUserID, factor);
    }



    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }





}
