package com.orderzzteam.chickenonfire;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.ThreeBounce;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.orderzzteam.chickenonfire.tools.MyFirebaseMessagingService.NotificationType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    int connectionCount = 0;

    // Notification temps...
    NotificationType notificationType = NotificationType.none;
    int orderID, menuItemID = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

//        showKeyHash();


        // Notification Check...
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String type = extras.getString("Type", "");
            if (type.equals("status")){
                int orderID = Integer.parseInt(extras.getString("Order ID", "-1"));
                String status = extras.getString("Status", "PENDING");
                String rejectionReason = extras.getString("Rejection Reason", "");
                String cancelationReason = extras.getString("Cancelation Reason", "");
                MyOrder.UpdateOrder(getApplicationContext(), orderID, status, rejectionReason, cancelationReason);
                notificationType = NotificationType.status;
                this.orderID = orderID;
            }else if (type.equals("offer")) {
                int menuItemID = Integer.parseInt(extras.getString("MenuItem ID", "-1"));
                this.menuItemID = menuItemID;
                notificationType = NotificationType.offer;
            }
        }


        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        ProgressBar progressBar = findViewById(R.id.spin_kit);
        Sprite threeBounce = new ThreeBounce();
        progressBar.setIndeterminateDrawable(threeBounce);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (!getConnectivity()) {
            Intent intent = new Intent(this, NoInternetActivity.class);
            intent.putExtra("Activity", "SplashScreen");
            startActivity(intent);
            finish();
        } else {
            GetGeneralInfo();
        }


        RegisterNotificationToken();

    }

    private void GoToOrderDetails(int orderID){
        MyOrder myOrder = MyOrder.getMyOrder(getApplicationContext(), orderID);
        if (myOrder == null) return;

        try {
            Intent resultIntent = new Intent(getApplicationContext(), MyOrderDetails.class);
            resultIntent.putExtra("My Order", myOrder);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);
            resultPendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    private void GoToMenuItem(int menuItemID){
        if (menuItemID == -1) return;


        final String backendUrl = getResources().getString(R.string.backendUrl);
        final String mJSONURLRequest = backendUrl + "mobile-api/getMenuItem?id=" + menuItemID;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(
                com.android.volley.Request.Method.GET,
                mJSONURLRequest,
                null,
                response -> {

                    try {
                        String name = response.getString("nm");
                        String nameAr = response.getString("nmL");
                        String desc = response.getString("ds");
                        String descAr = response.getString("dsL");
                        double price = response.getDouble("pr");
                        double discount = response.getDouble("dis");
                        String imageUrl = response.getString("img");
                        String url = backendUrl.substring(0, backendUrl.length()-1);
                        imageUrl = url  + imageUrl;

                        // Flavours...
                        JSONArray flavoursResponse = response.getJSONArray("fl");
                        ArrayList<Flavour> flavours = new ArrayList<>();
                        for (int k = 0; k < flavoursResponse.length(); k++){
                            JSONObject flavourResponse = flavoursResponse.getJSONObject(k);
                            String flavourName = flavourResponse.getString("nm");
                            String flavourNameAr = flavourResponse.getString("nmL");
                            String flavourImage = flavourResponse.getString("img");

                            Flavour flavour = new Flavour(flavourName, flavourNameAr, url + flavourImage);
                            flavours.add(flavour);
                        }

                        MenuItem menuItem = new MenuItem(menuItemID, name, nameAr, desc, descAr, imageUrl, price, discount, flavours);
                        Intent resultIntent = new Intent(getApplicationContext(), ItemOrderActivity.class);
                        resultIntent.putExtra("Menu Item", menuItem);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                        stackBuilder.addNextIntentWithParentStack(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);
                        resultPendingIntent.send();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(jsonArrayRequest);


    }


    private void RegisterNotificationToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Fetching FCM registration token failed", Toast.LENGTH_SHORT).show();
                    if (task.getException() != null){
                        Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }


                // Get new FCM registration token
                String token = task.getResult();
                String deviceName = Build.MODEL + "~" + getCurrentLanguage();
                @SuppressLint("HardwareIds") String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                JSONObject jsonData = new JSONObject();
                try {
                    jsonData.put("name", deviceName);
                    jsonData.put("device_id", deviceID);
                    jsonData.put("registration_id", token);
                    jsonData.put("type", "android");
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = getResources().getString(R.string.backendUrl) + "mobile-api/register-device";
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                JsonObjectRequest request = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, jsonData, null, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("Authorization", getResources().getString(R.string.backend_API_Key));
                        return map;
                    }
                };
                queue.add(request);



            }
        });
    }

    private Runnable task = new Runnable() {
        public void run() {
            BranchArea branchArea = BranchArea.getSavedArea(getBaseContext());

            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String langauge = sh.getString("language", "");

            if (branchArea != null && notificationType == NotificationType.status) {
                GoToOrderDetails(orderID);
            } else if (branchArea != null && notificationType == NotificationType.offer){
                GoToMenuItem(menuItemID);
            }else {

                Intent i;
                if (langauge.isEmpty()) {
                    i = new Intent(getApplicationContext(), LanguageActivity.class);
                    setLocale("en");
                } else {
                    boolean isChain = getResources().getBoolean(R.bool.chain);
                    if (isChain){
                        i = new Intent(getApplicationContext(), SelectRestaurant.class);
                    } else {
                        i = new Intent(getApplicationContext(), GeneralArea.class);
                    }
                    setLocale(langauge);
                }

                startActivity(i);
                finish();
            }
        }
    };

    private void GetGeneralInfo() {
        // Getting branch area if saved...
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("Account", "");
        final Account account = gson.fromJson(json, Account.class);

        Gson gson2 = new Gson();
        String json2 = preferences.getString("General Area", null);
        BranchArea branchArea = null;
        try {
            branchArea = gson2.fromJson(json2, BranchArea.class);
        } catch (Exception e) {
            BranchArea.clearBranchArea(this);
        }

        // Getting Username and Password...
        String email, password;
        if (account == null) {
            email = "";
            password = "";
        } else {
            if (account.getLoginStatus() == 0) {
                email = "";
                password = "";
            } else {
                email = account.getEmailAddress();
                password = account.getPassword();
            }
        }


        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("email", email);
            jsonData.put("password", password);

            if (branchArea != null) {
                jsonData.put("brID", branchArea.id);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }


        // General Info request...
        String url = getResources().getString(R.string.backendUrl) + "mobile-api/general-info";
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, jsonData, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                setRestaurantInfo(response);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", getResources().getString(R.string.backend_API_Key));
                return map;
            }
        };
        queue.add(request);

    }

    private void setRestaurantInfo(JSONObject response) {
        String versionName = BuildConfig.VERSION_NAME;
        int versionCode = BuildConfig.VERSION_CODE;

        BranchArea branchArea = BranchArea.getSavedArea(this);
        boolean authenticated;
        try {
            authenticated = response.getBoolean("authenticated");

            // Application Version
            if (response.has("Mobile_Version_Name") && response.has("Mobile_Version_Code")){
                versionName = response.getString("Mobile_Version_Name");
                versionCode = response.getInt("Mobile_Version_Code");
            }

            ((RestaurantApplication) this.getApplication()).setAppName(response.getString("Vendor_Name"));
            ((RestaurantApplication) this.getApplication()).setAppNameAr(response.getString("Vendor_Name_Second_Language"));
            ((RestaurantApplication) this.getApplication()).setLogo(response.getString("Logo"));
            ((RestaurantApplication) this.getApplication()).setAppDescription(response.getString("Cuisine"));
            ((RestaurantApplication) this.getApplication()).setAppDescriptionAr(response.getString("Cuisine_Second_Language"));
            ((RestaurantApplication) this.getApplication()).setDigitalExperts(response.getString("Digital_Experts"));
            ((RestaurantApplication) this.getApplication()).setPrivacyPolicy(response.getString("Privacy_Policy"));
            ((RestaurantApplication) this.getApplication()).setMinimumOrder(response.getDouble("Minimum_Order"));
            ((RestaurantApplication) this.getApplication()).setDeliveryPrice(response.getDouble("AVG_Service_Fee"));
            ((RestaurantApplication) this.getApplication()).setPreOrder(response.getBoolean("Pre_Order"));
            ((RestaurantApplication) this.getApplication()).setEnablePhoneVerification(response.getBoolean("Enable_Phone_Verification"));
            ((RestaurantApplication) this.getApplication()).setEnableCodAndSchedule(response.getBoolean("Enable_CodAndSchedule"));
            ((RestaurantApplication) this.getApplication()).setGmailEmail(response.getString("Vendor_Email"));
            ((RestaurantApplication) this.getApplication()).setGmailPassword(response.getString("Vendor_Password"));
            ((RestaurantApplication) this.getApplication()).setTwilioSecurityApiKey(response.getString("TWILIO_SECURITY_API_KEY"));
            ((RestaurantApplication) this.getApplication()).setDeliveryTime(response.getInt("Delivery_Time"));
            ((RestaurantApplication) this.getApplication()).setBookeyMerchantID(response.getString("BOOKEY_MERCHANT_ID"));
            ((RestaurantApplication) this.getApplication()).setBookeySubMerchantID(response.getString("BOOKEY_SUB_MERCHANT_ID"));
            ((RestaurantApplication) this.getApplication()).setBookeySecretKey(response.getString("BOOKEY_SECRET_KEY"));
            ((RestaurantApplication) this.getApplication()).setMapEnabled(response.getBoolean("Use_Maps"));
            checkIsMapChanged(response.getBoolean("Use_Maps"), branchArea);

            // Additional Fields...
            ((RestaurantApplication) this.getApplication()).setHeaderImage(response.has("Mobile_Header_Image") ? response.getString("Mobile_Header_Image") : "");
            ((RestaurantApplication) this.getApplication()).setCoverImage(response.has("Mobile_Cover_Image") ? response.getString("Mobile_Cover_Image") : "");

            // Setting Social Media
            JSONArray socialMediaArray = response.getJSONObject("Social_Media").getJSONArray("Vendor_Accounts");
            for (int i = 0; i < socialMediaArray.length(); i++) {
                if (socialMediaArray.getJSONObject(i).getString("name").equals("facebook")) {
                    ((RestaurantApplication) this.getApplication()).setFacebook(((JSONObject) socialMediaArray.get(i)).getString("Link"));
                }
                if (socialMediaArray.getJSONObject(i).getString("name").equals("instagram")) {
                    ((RestaurantApplication) this.getApplication()).setInstagram(((JSONObject) socialMediaArray.get(i)).getString("Link"));
                }
                if (socialMediaArray.getJSONObject(i).getString("name").equals("twitter")) {
                    ((RestaurantApplication) this.getApplication()).setTwitter(((JSONObject) socialMediaArray.get(i)).getString("Link"));
                }

            }

            // Setting payment methods
            JSONArray paymentMethodsResponse = response.getJSONArray("Payment_Methods");
            List<String> paymentMethods = new ArrayList<>();
            for (int i = 0; i < paymentMethodsResponse.length(); i++) {
                paymentMethods.add(paymentMethodsResponse.getString(i));
            }
            ((RestaurantApplication) this.getApplication()).setPaymentMethods(paymentMethods);

            // Setting featuredItems
            JSONArray featuredItemsResponse = response.getJSONArray("featuredItems");
            List<String> featuredItems = new ArrayList<>();
            for (int i = 0; i < featuredItemsResponse.length(); i++) {
                featuredItems.add(featuredItemsResponse.getString(i));
            }
            ((RestaurantApplication) this.getApplication()).setFeaturedItems(featuredItems);

            // If branch already selected...
            branchArea = BranchArea.getSavedArea(this);
            if (branchArea != null) {
                ((RestaurantApplication) this.getApplication()).setLatitude(response.getDouble("latitude"));
                ((RestaurantApplication) this.getApplication()).setLongitude(response.getDouble("longitude"));
                ((RestaurantApplication) this.getApplication()).setStatus(response.getString("status"));
                ((RestaurantApplication) this.getApplication()).setPhoneNumber(response.getString("phoneNumber"));

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
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                ((RestaurantApplication) this.getApplication()).setWorkingDays(workingDays);
            }


        } catch (JSONException e) {
            authenticated = false;
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            if (connectionCount >= 3) {

                Intent intent = new Intent(this, NoInternetActivity.class);
                intent.putExtra("Activity", "SplashScreen");
                startActivity(intent);
                finish();
            } else {
                GetGeneralInfo();
                return;
            }
        }

        Account account = Account.getSavedAccount(this);
        if (account != null) {
            account.setLoginStatus(authenticated ? 1 : 0);
            Account.setAccount(this, account);
        }


        if (requiresUpdate(versionName, versionCode)){
            AlertDialog.Builder alert = new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.update_required))
                    .setMessage(getResources().getString(R.string.please_update_your_app))
                    .setPositiveButton(getResources().getString(R.string.update), (dialog, which) -> {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> {
                        finish();
                        System.exit(0);
                    });
            alert.show();
        } else {
            runOnUiThread(new Runnable() {
                public void run() {
                    Handler handler = new Handler();
                    handler.postDelayed(task, 1000);
                }
            });
        }


    }

    private boolean requiresUpdate(String versionName, int versionCode){
        String currentVersionName = BuildConfig.VERSION_NAME;
        int currentVersionCode = BuildConfig.VERSION_CODE;
        return !currentVersionName.equals(versionName) || currentVersionCode != versionCode;
    }

    private void checkIsMapChanged(boolean isMapEnabled, BranchArea branchArea) {
        if (branchArea == null) return;

        if ((isMapEnabled && branchArea.type == AreaType.list) || (!isMapEnabled && branchArea.type == AreaType.map)) {
            BranchArea.clearBranchArea(this);
            UserAddress.clearAddresses(this);
            Order.clearLastOrder(this);
        }
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

    private boolean getConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }


    private String getCurrentLanguage(){
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);
        return sh.getString("language", "en");
    }

    private void showKeyHash(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Toast.makeText(this, hashKey + "" , Toast.LENGTH_LONG).show();
                Toast.makeText(this, hashKey + "" , Toast.LENGTH_LONG).show();
                Toast.makeText(this, hashKey + "" , Toast.LENGTH_LONG).show();
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e("", "printHashKey()", e);
        } catch (Exception e) {
            Log.e("", "printHashKey()", e);
        }
    }
}

