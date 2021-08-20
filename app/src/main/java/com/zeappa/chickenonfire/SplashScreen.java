package com.zeappa.chickenonfire;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.ThreeBounce;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SplashScreen extends AppCompatActivity {

    int connectionCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        ProgressBar progressBar = findViewById(R.id.spin_kit);
        Sprite threeBounce = new ThreeBounce();
        progressBar.setIndeterminateDrawable(threeBounce);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if(!getConnectivity()){
            Intent intent = new Intent(this, NoInternetActivity.class);
            intent.putExtra("Activity","SplashScreen");
            startActivity(intent);
            finish();
        } else {
            GetGeneralInfo();
        }

    }

    private Runnable task = new Runnable() {
        public void run() {

            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String langauge = sh.getString("language", "");

            Intent i;
            if(langauge.isEmpty()){
                i = new Intent(getApplicationContext(), LanguageActivity.class);
                setLocale("en");
            }else{
                i = new Intent(getApplicationContext(), GeneralArea.class);
                setLocale(langauge);
            }

            startActivity(i);
            finish();
        }
    };

    private void GetGeneralInfo(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("Account", "");
        final Account account = gson.fromJson(json, Account.class);

        String email, password;
        if(account == null){
            email = "";
            password = "";
        }else{
            if(account.getLoginStatus() == 0){
                email = "";
                password = "";
            } else {
                email = account.getEmailAddress();
                password = account.getPassword();
            }
        }


        OkHttpClient client = new OkHttpClient();
        JSONObject jsonAccount = new JSONObject();
        try {
            jsonAccount.put("email", email);
            jsonAccount.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,jsonAccount.toString());
        Request request = new Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(getResources().getString(R.string.backendUrl) + "mobile-api/general-info")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                setRestaurantInfo(new JSONObject());
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) {

                if (!response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setRestaurantInfo(new JSONObject());
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject responseJSON = new JSONObject(response.body().string());
                                switch (response.code()){
                                    case 200:
                                        setRestaurantInfo(responseJSON);
                                        break;
                                    case 403:
                                        Toast.makeText(getApplicationContext(), "Forbidden", Toast.LENGTH_LONG).show();
                                        setRestaurantInfo(new JSONObject());
                                        break;
                                    case 400:
                                        Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                                        setRestaurantInfo(new JSONObject());
                                        break;
                                }


                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            }
        });
    }

    private void setRestaurantInfo(JSONObject response){

        boolean authenticated;
        try {
            authenticated = response.getBoolean("authenticated");

            ((RestaurantApplication) this.getApplication()).setAppName(response.getString("Vendor_Name"));
            ((RestaurantApplication) this.getApplication()).setAppNameAr(response.getString("Vendor_Name_Second_Language"));
            ((RestaurantApplication) this.getApplication()).setAppDescription(response.getString("Cuisine"));
            ((RestaurantApplication) this.getApplication()).setAppDescriptionAr(response.getString("Cuisine_Second_Language"));
            ((RestaurantApplication) this.getApplication()).setPhoneNumber(response.getString("Vendor_Phone_Number"));
            ((RestaurantApplication) this.getApplication()).setWebsite(response.getString("Website_Url"));
            ((RestaurantApplication) this.getApplication()).setDigitalExperts(response.getString("Digital_Experts"));
            ((RestaurantApplication) this.getApplication()).setPrivacyPolicy(response.getString("Privacy_Policy"));
            ((RestaurantApplication) this.getApplication()).setMinimumOrder(response.getDouble("Minimum_Order"));
            ((RestaurantApplication) this.getApplication()).setDeliveryPrice(response.getDouble("AVG_Service_Fee"));
            ((RestaurantApplication) this.getApplication()).setPreOrder(response.getBoolean("Pre_Order"));
            ((RestaurantApplication) this.getApplication()).setGmailEmail(response.getString("Vendor_Email"));
            ((RestaurantApplication) this.getApplication()).setGmailPassword(response.getString("Vendor_Password"));
            ((RestaurantApplication) this.getApplication()).setTwilioSecurityApiKey(response.getString("TWILIO_SECURITY_API_KEY"));
            ((RestaurantApplication) this.getApplication()).setDeliveryTime(response.getInt("Delivery_Time"));
            ((RestaurantApplication) this.getApplication()).setBookeyMerchantID(response.getString("BOOKEY_MERCHANT_ID"));
            ((RestaurantApplication) this.getApplication()).setBookeySubMerchantID(response.getString("BOOKEY_SUB_MERCHANT_ID"));
            ((RestaurantApplication) this.getApplication()).setBookeySecretKey(response.getString("BOOKEY_SECRET_KEY"));
            ((RestaurantApplication) this.getApplication()).setGoogleApiKey(response.getString("GOOGLE_API_KEY"));
            ((RestaurantApplication) this.getApplication()).setLatitude(response.getDouble("latitude"));
            ((RestaurantApplication) this.getApplication()).setLongitude(response.getDouble("longitude"));
            ((RestaurantApplication) this.getApplication()).setStatus(response.getString("status"));

            // Setting Social Media
            JSONArray socialMediaArray = response.getJSONObject("Social_Media").getJSONArray("Vendor_Accounts");
            ((RestaurantApplication) this.getApplication()).setFacebook(((JSONObject)socialMediaArray.get(0)).getString("Link"));
            ((RestaurantApplication) this.getApplication()).setInstagram(((JSONObject)socialMediaArray.get(1)).getString("Link"));
            ((RestaurantApplication) this.getApplication()).setTwitter(((JSONObject)socialMediaArray.get(2)).getString("Link"));

            // Setting payment methods
            JSONArray paymentMethodsResponse = response.getJSONArray("Payment_Methods");
            List<String> paymentMethods = new ArrayList<>();
            for (int i=0; i < paymentMethodsResponse.length(); i++){
                paymentMethods.add(paymentMethodsResponse.getString(i));
            }
            ((RestaurantApplication) this.getApplication()).setPaymentMethods(paymentMethods);

            // Setting featuredItems
            JSONArray featuredItemsResponse = response.getJSONArray("featuredItems");
            List<String> featuredItems = new ArrayList<>();
            for (int i=0; i < featuredItemsResponse.length(); i++){
                featuredItems.add(featuredItemsResponse.getString(i));
            }
            ((RestaurantApplication) this.getApplication()).setFeaturedItems(featuredItems);

            // Setting working days
            HashMap<String, WorkDay> workingDays = new HashMap<>();
            Iterator<String> iter = response.getJSONObject("openingTimes").keys();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    JSONObject value = response.getJSONObject("openingTimes").getJSONObject(key);

                    int openingMinute = value.getInt("opiningMin");
                    int openingHour = value.getInt("opiningHour");
                    int closingMinute = value.getInt("closingMin");
                    int closingHour = value.getInt("closingHour");

                    workingDays.put(key, new WorkDay(openingMinute, openingHour, closingMinute, closingHour, getApplicationContext()));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
            ((RestaurantApplication) this.getApplication()).setWorkingDays(workingDays);


        } catch (JSONException e) {
            authenticated = false;
            e.printStackTrace();
            connectionCount++;
            if (connectionCount >= 3){
                Intent intent = new Intent(this, NoInternetActivity.class);
                intent.putExtra("Activity","SplashScreen");
                startActivity(intent);
                finish();
            }else{
                setRestaurantInfo(response);
            }
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("Account", "");
        Account account = gson.fromJson(json, Account.class);
        if (account != null){
            account.setLoginStatus(authenticated ? 1 : 0);
            String json2 = gson.toJson(account);
            preferences.edit().putString("Account", json2).apply();
        }

        runOnUiThread(new Runnable() {
            public void run() {
                Handler handler = new Handler();
                handler.postDelayed(task, 1000);
            }
        });

    }

    private void setLocale(String lang) {

        Resources resources = this.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(lang.toLowerCase()));
        resources.updateConfiguration(configuration, displayMetrics);
        configuration.locale = new Locale(lang.toLowerCase());
        resources.updateConfiguration(configuration, displayMetrics);

    }

    private boolean getConnectivity(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }


}

