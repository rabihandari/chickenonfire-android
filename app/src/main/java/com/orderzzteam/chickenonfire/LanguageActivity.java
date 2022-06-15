package com.orderzzteam.chickenonfire;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LanguageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        setLightStatusBar(this);
    }

    public void SelectArabic(View view) {
        setLocale("ar");
    }

    public void SelectEnglish(View view) {
        setLocale("en");
    }

    private void setLocale(String lang) {

        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(lang.toLowerCase()));
        resources.updateConfiguration(configuration, displayMetrics);
        configuration.locale = new Locale(lang.toLowerCase());
        resources.updateConfiguration(configuration, displayMetrics);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("language",lang);
        myEdit.apply();


        Intent intent;
        boolean isChain = getResources().getBoolean(R.bool.chain);
        if (isChain){
            intent = new Intent(getApplicationContext(), SelectRestaurant.class);
        } else {
            intent = new Intent(getApplicationContext(), GeneralArea.class);
        }
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
}
