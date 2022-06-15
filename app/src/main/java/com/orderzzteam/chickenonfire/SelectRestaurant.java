package com.orderzzteam.chickenonfire;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.orderzzteam.chickenonfire.tools.CustomViewPager2;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SelectRestaurant extends FragmentActivity {

    TextView locationText;
    ConstraintLayout chooseLocation;
    Activity activity;

    private CustomViewPager2 mPager;
    private PagerAdapter pagerAdapter;

    // Temps...
    ArrayList<Restaurant> filteredRestaurants;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_restaurant);
        setLightStatusBar(this);

        requestAllPermissions();


        activity = this;
        chooseLocation = findViewById(R.id.select_restaurant_choose_location);
        locationText = findViewById(R.id.select_restaurant_location_text);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            AreaType areaType = (AreaType) extras.getSerializable("Type");
            if (areaType == AreaType.map) {
                String areaName = extras.getString("Area Name", null);
                double latitude = extras.getDouble("Latitude", 0);
                double longitude = extras.getDouble("Longitude", 0);
                List<LatLng> points = extras.getParcelableArrayList("Points");
                getBranchInfo(areaName, latitude, longitude, points);
            } else {
                int areaID = extras.getInt("Area ID");
                int subareaID = extras.getInt("SubArea ID");
                int branchID = extras.getInt("Branch ID");
                String areaName = extras.getString("Area Name", null);
                Double serviceCharge = extras.getDouble("Service Charge");
                getBranchInfo(areaID, subareaID, branchID, areaName, serviceCharge);
            }

        } else {
            BranchArea branchArea = BranchArea.getSavedArea(this);
            if(branchArea != null){
                locationText.setText(branchArea.areaName);
                if (branchArea.type == AreaType.list){
                    validateArea(branchArea);
                }else {
//                    restaurantsRecyclerView.setEnabled(true);
                }
            }
        }

        boolean isMapEnabled = ((RestaurantApplication) this.getApplication()).isMapEnabled();
        chooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if (isMapEnabled) {
                    intent = new Intent(getApplicationContext(), GeneralAreaMap.class);
                } else {
                    intent = new Intent(getApplicationContext(), GeneralAreaList.class);
                }
                startActivity(intent);

            }
        });


        // Initializing Fragments...
        mPager = findViewById(R.id.select_restaurant_viewpager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ImageView toolbarButton = findViewById(R.id.select_restaurant_toolbar_left_button);
                if (position == 0){
                    toolbarButton.setImageDrawable(getResources().getDrawable(R.drawable.burger_button));
                } else {
                    toolbarButton.setImageDrawable(getResources().getDrawable(R.drawable.back_arrow));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



    }

    public void ShakeArea(){
        final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake_anim2);
        chooseLocation.startAnimation(animShake);
    }


    public void GoToFilteredRestaurants(ArrayList<Restaurant> filteredRestaurants, String filter) {
        Fragment fragment = getSupportFragmentManager().getFragments().get(1);
        if (fragment != null){
            ((SelectRestaurantFilterFragment)fragment).SetRestaurants(filteredRestaurants, filter);
            mPager.setCurrentItem(1);
        }
    }


    private void validateArea(BranchArea branchArea) {
        ProgressBar progressBar = findViewById(R.id.select_restaurant_progressview);
        progressBar.setVisibility(View.VISIBLE);

        ImageView arrow = findViewById(R.id.select_restaurant_arrow);
        arrow.setVisibility(View.GONE);

        chooseLocation.setClickable(false);

        JSONObject body = new JSONObject();
        try {
            body.put("brID", branchArea.id);
            body.put("areaID", branchArea.areaID);
            body.put("subareaID", branchArea.subAreaID);
        }catch (JSONException e){
            ResetBranchArea();
        }

        final String backendUrl = getResources().getString(R.string.backendUrl);
        final String mJSONURLRequest = backendUrl + "mobile-api/validate-area";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(
                com.android.volley.Request.Method.POST,
                mJSONURLRequest,
                body,
                response -> {
                    try{
                        boolean valid = response.getBoolean("valid");
                        double serviceFee = response.getDouble("serviceFee");

                        // Updating service Fee...
                        if (valid){
                            BranchArea newBranchArea = BranchArea.getSavedArea(getApplicationContext());
                            newBranchArea.serviceCharge = serviceFee;
                            BranchArea.setBranchArea(getApplicationContext(), newBranchArea);
                        } else {
                            ResetBranchArea();
                        }

                        // Validation...
                        progressBar.setVisibility(View.GONE);
                        arrow.setVisibility(View.VISIBLE);

                        chooseLocation.setClickable(valid);


                    }catch (JSONException e){
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        arrow.setVisibility(View.VISIBLE);
                        ResetBranchArea();
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    ResetBranchArea();
                    progressBar.setVisibility(View.GONE);
                    arrow.setVisibility(View.VISIBLE);
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", getResources().getString(R.string.backend_API_Key));
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        requestQueue.add(jsonArrayRequest);
    }


    private void ResetBranchArea(){
        BranchArea.clearBranchArea(this);
        locationText.setText("Select your delivery location");
    }


    private void getBranchInfo(String areaName, double latitude, double longitude, List<LatLng> points){

        ProgressBar progressBar = findViewById(R.id.select_restaurant_progressview);
        progressBar.setVisibility(View.VISIBLE);

        ImageView arrow = findViewById(R.id.select_restaurant_arrow);
        arrow.setVisibility(View.GONE);

        chooseLocation.setClickable(false);

        OkHttpClient client = new OkHttpClient();
        JSONObject jsonAccount = new JSONObject();
        try {
            jsonAccount.put("latitude", latitude);
            jsonAccount.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,jsonAccount.toString());
        Request request = new Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(getResources().getString(R.string.backendUrl) + "mobile-api/get-branch-info")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show());

                } else {
                    runOnUiThread(() -> {
                        try {
                            JSONObject responseJSON = new JSONObject(response.body().string());
                            switch (response.code()){
                                case 201:
                                    BranchArea.setRestaunrantBranchInfo(activity, getApplicationContext(), responseJSON, areaName, latitude, longitude, points);
                                    locationText.setText(areaName);
                                    break;
                                case 403:
                                    Toast.makeText(getApplicationContext(), "Forbidden", Toast.LENGTH_LONG).show();
                                    break;
                                case 405:
                                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                                    break;
                            }



                        } catch (JSONException | IOException e) {
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }

                        chooseLocation.setClickable(true);
                        progressBar.setVisibility(View.GONE);
                        arrow.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }

    private void getBranchInfo(int areaID, int subareaID, int branchID, String areaName, Double serviceCharge){
        ProgressBar progressBar = findViewById(R.id.select_restaurant_progressview);
        progressBar.setVisibility(View.VISIBLE);

        ImageView arrow = findViewById(R.id.select_restaurant_arrow);
        arrow.setVisibility(View.GONE);

        chooseLocation.setClickable(false);

        OkHttpClient client = new OkHttpClient();
        JSONObject jsonAccount = new JSONObject();
        try {
            jsonAccount.put("brID", branchID);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,jsonAccount.toString());
        Request request = new Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(getResources().getString(R.string.backendUrl) + "mobile-api/get-branch-info")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show());

                } else {
                    runOnUiThread(() -> {
                        try {
                            JSONObject responseJSON = new JSONObject(response.body().string());
                            switch (response.code()){
                                case 201:
                                    BranchArea.setRestaunrantBranchInfo(activity, getApplicationContext(), branchID, areaID, subareaID, responseJSON, areaName, serviceCharge);
                                    locationText.setText(areaName);
                                    break;
                                case 403:
                                    Toast.makeText(getApplicationContext(), "Forbidden", Toast.LENGTH_LONG).show();
                                    break;
                                case 405:
                                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                                    break;
                            }



                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }

                        chooseLocation.setClickable(true);
                        progressBar.setVisibility(View.GONE);
                        arrow.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }

    public void ToobarButtonClicked(View view) {
        if (mPager.getCurrentItem() == 1){
            mPager.setCurrentItem(0);
        }
    }


    public  class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new SelectRestaurantHomeFragment();
                case 1:
                    return new SelectRestaurantFilterFragment();
                    default:
                        return new SelectRestaurantHomeFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

    private void requestAllPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CALL_PHONE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                1);

    }

    private void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void showMenu(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }

    @Override
    public void onBackPressed() {
        mPager.setCurrentItem(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BranchArea branchArea = BranchArea.getSavedArea(this);
        if(branchArea != null){
            locationText.setText(branchArea.areaName);
        }
    }
}
