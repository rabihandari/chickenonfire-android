package com.orderzzteam.chickenonfire;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeneralArea extends AppCompatActivity {

    Button showMenu;
    EditText locationEditText;
    Activity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.general_area);
        setLightStatusBar(this);

        requestAllPermissions();

        activity = this;
        showMenu = findViewById(R.id.general_location_showmenu_button);
        locationEditText = findViewById(R.id.general_location_setlocation_edittext);

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
                locationEditText.setText(branchArea.areaName);
                if (branchArea.type == AreaType.list){
                    validateArea(branchArea);
                }else {
                    showMenu.setEnabled(true);
                }
            }
        }

        boolean isMapEnabled = ((RestaurantApplication) this.getApplication()).isMapEnabled();
        locationEditText.setOnClickListener(new View.OnClickListener() {
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

    }

    private void validateArea(BranchArea branchArea) {
        ProgressBar progressBar = findViewById(R.id.general_area_progressbar);
        progressBar.setVisibility(View.VISIBLE);

        ImageView arrow = findViewById(R.id.general_area_arrow);
        arrow.setVisibility(View.GONE);

        locationEditText.setClickable(false);
        showMenu.setEnabled(false);

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

                        locationEditText.setClickable(valid);
                        showMenu.setEnabled(valid);


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
        showMenu.setEnabled(false);
        BranchArea.clearBranchArea(this);
        locationEditText.setText("Select your delivery location");
    }


    private void getBranchInfo(String areaName, double latitude, double longitude, List<LatLng> points){

        ProgressBar progressBar = findViewById(R.id.general_area_progressbar);
        progressBar.setVisibility(View.VISIBLE);

        ImageView arrow = findViewById(R.id.general_area_arrow);
        arrow.setVisibility(View.GONE);

        locationEditText.setClickable(false);
        showMenu.setEnabled(false);

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
                                    locationEditText.setText(areaName);
                                    showMenu.setEnabled(true);
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

                        locationEditText.setClickable(true);
                        progressBar.setVisibility(View.GONE);
                        arrow.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }

    private void getBranchInfo(int areaID, int subareaID, int branchID, String areaName, Double serviceCharge){
        ProgressBar progressBar = findViewById(R.id.general_area_progressbar);
        progressBar.setVisibility(View.VISIBLE);

        ImageView arrow = findViewById(R.id.general_area_arrow);
        arrow.setVisibility(View.GONE);

        locationEditText.setClickable(false);
        showMenu.setEnabled(false);

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
                                    locationEditText.setText(areaName);
                                    showMenu.setEnabled(true);
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

                        locationEditText.setClickable(true);
                        progressBar.setVisibility(View.GONE);
                        arrow.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
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
        ((RestaurantApplication) this.getApplication()).setBackendUrl(getResources().getString(R.string.backendUrl));

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }

    @Override
    protected void onResume() {
        super.onResume();
        BranchArea branchArea = BranchArea.getSavedArea(this);
        if(branchArea != null){
            locationEditText.setText(branchArea.areaName);
        }
    }
}
