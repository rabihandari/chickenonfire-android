package com.orderzzteam.chickenonfire;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class GeneralAreaMap extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;

    LatLng latLngSelected;
    LatLng currentLatLng;

    private FusedLocationProviderClient fusedLocationClient;
    boolean locationPermissionGranted = false;

    private String areaName = null;
    float cameraZoom = 10.0f;

    CardView bottomSheetLayout;
    TextView bottomSheetAreaName;

    ArrayList<Polygon> polygons = new ArrayList<>();
    List<LatLng> selectedPoints = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.general_area_map);
        setLightStatusBar(this);

        if (!getConnectivity()) {
            Intent intent = new Intent(this, NoInternetActivity.class);
            intent.putExtra("Activity", "GeneralAreaMap");
            startActivity(intent);
            finish();
        }


        getLocationPermission();

        latLngSelected = new LatLng(Double.parseDouble(getResources().getString(R.string.default_restaurant_latitude)), Double.parseDouble(getResources().getString(R.string.default_restaurant_longitude)));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("General Area", null);
        BranchArea branchArea = null;
        try {
            branchArea = gson.fromJson(json, BranchArea.class);
            if (branchArea != null){
                latLngSelected = new LatLng(branchArea.getLatitude(), branchArea.getLongitude());
            }
        }catch (Exception e){
            Log.e("", Objects.requireNonNull(e.getLocalizedMessage()));
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.general_location_mapview);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        String apiKey = getString(R.string.google_maps_key);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }


        bottomSheetLayout = findViewById(R.id.location_bottom_sheet);
        bottomSheetAreaName = findViewById(R.id.bottom_sheet_location_areaname);
        ObjectAnimator animation = ObjectAnimator.ofFloat(bottomSheetLayout, "translationY", 700f);
        animation.setDuration(0);
        animation.start();


    }

    public void searchCalled(View view) {

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                areaName = place.getName();
                latLngSelected = place.getLatLng();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngSelected, cameraZoom));

                EditText searchBox = findViewById(R.id.general_location_searchbox);
                searchBox.setText(areaName);

                checkIfInside(latLngSelected.latitude, latLngSelected.longitude);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(GeneralAreaMap.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        final ImageView fadedMarker = findViewById(R.id.general_location_marker);

        map = googleMap;
        final Marker markerName = map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)).position(latLngSelected).title(getResources().getString(R.string.your_location)));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngSelected, cameraZoom));

        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (!markerName.isVisible())
                    return;

                markerName.setVisible(false);
                fadedMarker.setVisibility(View.VISIBLE);
            }
        });


        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                latLngSelected = new LatLng(map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude);
                markerName.setPosition(latLngSelected);
                markerName.setVisible(true);
                fadedMarker.setVisibility(View.INVISIBLE);

                checkIfInside(latLngSelected.latitude, latLngSelected.longitude);
            }
        });

        getPolygons();

    }



    private void getPolygons() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest stringRequest = new JsonArrayRequest(
                Request.Method.GET,
                getResources().getString(R.string.backendUrl) + "mobile-api/getAllowedLocations/",
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i=0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                JSONArray allowedLocation = jsonObject.getJSONArray("polygon");
                                drawPolygon(allowedLocation);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }


    private void drawPolygon(JSONArray allowedLocation) {
        final PolygonOptions polygonOptions = new PolygonOptions().clickable(false);

        for(int i = 0; i < allowedLocation.length(); i++){
            try {
                JSONArray latlng = allowedLocation.getJSONArray(i);
                double lat = latlng.getDouble(0);
                double lng = latlng.getDouble(1);

                polygonOptions.add(new LatLng(lat, lng));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Polygon polygon = map.addPolygon(polygonOptions);
        polygon.setTag("A");
        polygon.setStrokeColor(getResources().getColor(R.color.redColor));
        polygon.setStrokeWidth(8f);
        polygons.add(polygon);
    }

    public void checkIfInside(double lat, double lng){

        if (polygons.isEmpty())
            return;

        boolean contain = false;
        for(Polygon polygon: polygons){
            if(PolyUtil.containsLocation(new LatLng(lat, lng), polygon.getPoints(), true)){
                contain = true;
                selectedPoints = polygon.getPoints();
            }
        }

        Button confirmButton = findViewById(R.id.general_location_confirm_button);
        if (contain) {
            confirmButton.setText(R.string.confirm);
            confirmButton.setEnabled(true);
        } else {
            confirmButton.setText(R.string.sorry_we_dont_deliver);
            confirmButton.setEnabled(false);
        }



    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses.size() == 0)
                return;
            String locality = addresses.get(0).getLocality();
            String subLocality = addresses.get(0).getSubLocality();
            String adminArea = addresses.get(0).getAdminArea();
            areaName = "";
            if (locality != null)
                areaName += locality + " ";
            if (subLocality != null)
                areaName += subLocality + " ";
            if (adminArea != null)
                areaName += adminArea;
            if (areaName.isEmpty())
                return;
            areaName = areaName.replaceAll(" ", ", ");

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void getCurrentLocation(View view) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!isLocationEnabled())
            return;

        try {
            if (locationPermissionGranted) {

                LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            latLngSelected = new LatLng(location.getLatitude(), location.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngSelected, cameraZoom));
                            checkIfInside(latLngSelected.latitude, latLngSelected.longitude);
                        } else if (currentLatLng != null) {
                            latLngSelected = new LatLng(currentLatLng.latitude, currentLatLng.longitude);
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngSelected, cameraZoom));
                            checkIfInside(latLngSelected.latitude, latLngSelected.longitude);
                        }
                    }
                });

            } else {
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }

    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            assert lm != null;
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.location_not_enabled)).setNegativeButton(getResources().getString(R.string.cancel), null)
                    .setPositiveButton(getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .show();

            return false;

        } else {

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

    public void GoBack(View view) {
        onBackPressed();
    }

    public void confirmLocation(View view) {
        getAddress(latLngSelected.latitude, latLngSelected.longitude);

        if (areaName == null){
            confirmLocation(view);
            return;
        }

        if (areaName.length() >= 45) {
            areaName = areaName.substring(0, 40);
        }


        boolean isChain = getResources().getBoolean(R.bool.chain);
        Intent intent;
        if (isChain) {
            intent = new Intent(this, SelectRestaurant.class);
        } else {
            intent = new Intent(this, GeneralArea.class);
        }
        intent.putExtra("Area Name", areaName);
        intent.putExtra("Type", AreaType.map);
        intent.putExtra("Latitude", latLngSelected.latitude);
        intent.putExtra("Longitude", latLngSelected.longitude);
        intent.putParcelableArrayListExtra("Points", (ArrayList<LatLng>)selectedPoints);
        startActivity(intent);
        finish();

    }

    private void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void closeHint(View view) {
        CardView hintCard = findViewById(R.id.get_location_hint_box);
        hintCard.setVisibility(View.GONE);
    }

    private boolean getConnectivity() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
                        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                }
            }
        };
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }


}
