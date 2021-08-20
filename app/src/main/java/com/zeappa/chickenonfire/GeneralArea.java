package com.zeappa.chickenonfire;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class GeneralArea extends AppCompatActivity {

    Button showMenu;
    EditText locationEditText;
    private String areaName = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.general_area);
        setLightStatusBar(this);

        requestAllPermissions();

        showMenu = findViewById(R.id.general_location_showmenu_button);
        locationEditText = findViewById(R.id.general_location_setlocation_edittext);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String savedAreaName = preferences.getString("General Area" , "");
        if(!savedAreaName.isEmpty()){
            areaName = savedAreaName;
            locationEditText.setText(savedAreaName);
        }


        Bundle extras = getIntent().getExtras();
        if(extras != null){
            areaName = extras.getString("Area Name", null);
            locationEditText.setText(areaName);
        }

        if(areaName != null)
            showMenu.setEnabled(true);

        locationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(),GeneralAreaMap.class);
                startActivity(intent);

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


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().putString("General Area",areaName).apply();

        Intent intent = new Intent(this,HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }
}
