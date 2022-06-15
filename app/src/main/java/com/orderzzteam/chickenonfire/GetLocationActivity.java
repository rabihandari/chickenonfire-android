package com.orderzzteam.chickenonfire;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class GetLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    LatLng latLngSelected;
    LatLng currentLatLng;
    GoogleMap map;
    private static String TAG = "Get Location Activity";
    private static final float mapZoom = 15.0f;

    private FusedLocationProviderClient fusedLocationClient;
    boolean locationPermissionGranted = false;

    private int editingIndex = -1;
    BranchArea branchArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);
        setLightStatusBar(this);

        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","GetLocationActivity");
            startActivity(intent);
            finish();
        }


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("General Area", null);
        branchArea = gson.fromJson(json, BranchArea.class);
        latLngSelected = new LatLng(branchArea.getLatitude(), branchArea.getLongitude());

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            editingIndex = extras.getInt("Editing Index", -1);
            if (editingIndex != -1){
                gson = new Gson();
                json = preferences.getString("User Address" + editingIndex, null);
                UserAddress userAddress = gson.fromJson(json, UserAddress.class);
                latLngSelected = userAddress.getLatLng();
            }
        }

        getLocationPermission();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }




    }



    public void GoBack(View view) {
        onBackPressed();
    }

    public void searchCalled(View view){

        List<String> allowedCountries = new ArrayList<>();
        allowedCountries.add("KW");
        allowedCountries.add("LB");
        allowedCountries.add("DE");

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields).setCountries(allowedCountries)
                .build(this);
        startActivityForResult(intent, 2);

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        final Button confirmButton = findViewById(R.id.get_location_confirm_button);
        final ImageView fadedMarker = findViewById(R.id.get_location_marker);

        map = googleMap;
        final Marker markerName = googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)).position(latLngSelected).title(getResources().getString(R.string.your_location)));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngSelected ,mapZoom));

        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if(!markerName.isVisible())
                    return;
                markerName.setVisible(false);
                confirmButton.setVisibility(View.INVISIBLE);
                fadedMarker.setVisibility(View.VISIBLE);
            }
        });


        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                latLngSelected = new LatLng(googleMap.getCameraPosition().target.latitude,googleMap.getCameraPosition().target.longitude);
                markerName.setPosition(latLngSelected);
                markerName.setVisible(true);
                confirmButton.setVisibility(View.VISIBLE);
                fadedMarker.setVisibility(View.INVISIBLE);

                if(PolyUtil.containsLocation(new LatLng(latLngSelected.latitude, latLngSelected.longitude), branchArea.getPoints(), true)){
                    confirmButton.setEnabled(true);
                }else{
                    confirmButton.setEnabled(false);
                }
            }
        });

    }
    public void getCurrentLocation(View view) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if(!isLocationEnabled())
            return;

        try {
            if (locationPermissionGranted) {

                LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if(location != null){
                            latLngSelected = new LatLng(location.getLatitude(),location.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngSelected, mapZoom));
                        }else if(currentLatLng != null){
                            latLngSelected = new LatLng(currentLatLng.latitude,currentLatLng.longitude);
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngSelected, mapZoom));
                        }
                    }
                });

            }else{
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }

    }

    private boolean isLocationEnabled(){
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ignored) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ignored) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.location_not_enabled)).setNegativeButton(getResources().getString(R.string.cancel),null)
                    .setPositiveButton(getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .show();

            return false;

        }else{

            return true;
        }

    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getAddress());
                //Toast.makeText(GetLocationActivity.this, "ID: " + place.getId() + "address:" + place.getAddress() + "Name:" + place.getName() + " latlong: " + place.getLatLng(), Toast.LENGTH_LONG).show();

                latLngSelected = place.getLatLng();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngSelected,mapZoom));

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(GetLocationActivity.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }


    private void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void confirmLocation(View view) {

        Intent intent = new Intent(this,AddAddressActivity.class);
        intent.putExtra("Latitude",latLngSelected.latitude);
        intent.putExtra("Longitude",latLngSelected.longitude);
        intent.putExtra("Editing Index",editingIndex);
        startActivity(intent);
        finish();

    }

    public void closeHint(View view){
        CardView hintCard = findViewById(R.id.get_location_hint_box);
        hintCard.setVisibility(View.GONE);
    }

    private boolean getConnectivity(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }

    @Override
    protected void onStart() {
        super.onStart();
       requestLocation();
    }

    private void requestLocation() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                    }
                }
            }
        };
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }


}
