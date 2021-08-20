package com.zeappa.chickenonfire;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.platform.Platform;

public class SignInActivity extends AppCompatActivity {

    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    private int home = 0;

    private LoginButton facebookLoginButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        setLightStatusBar(this);


        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","SignInActivity");
            startActivity(intent);
            finish();
        }

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            home = extras.getInt("Home" , 0);

        }


        facebookLoginButton = findViewById(R.id.facebook_login_button);
        facebookLoginButton.setLoginBehavior(LoginBehavior.WEB_ONLY);
        facebookLoginButton.setReadPermissions(Arrays.asList("email"));
        facebookLoginButton.setSoundEffectsEnabled(false);
        callbackManager = CallbackManager.Factory.create();

        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Toast.makeText(getApplicationContext(),"Successfully logged in ",Toast.LENGTH_SHORT).show();
                getUserInfo(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

                Toast.makeText(getApplicationContext(), error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void continueWithEmail(View view) {

        Intent intent = new Intent(this,SignInWithEmailActivity.class);
        intent.putExtra("Home", home);
        startActivity(intent);
        finish();

    }

    public void continueAsGuest(View view) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int count = preferences.getInt("User Addresses Count", 0);

        Intent intent;
        if(home == 1){
            intent = new Intent(this,HomeActivity.class);
        }else{
            if(count == 0)
                intent = new Intent(this,GetLocationActivity.class);
            else
                intent = new Intent(this,CheckoutActivity.class);
        }

        startActivity(intent);
        finish();

    }

    public void GoBack(View view) {
        onBackPressed();
    }

    private void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void continueWithFacebook(View view) {

        facebookLoginButton.performClick();

    }


    private void getUserInfo(AccessToken newAccessToken){

        GraphRequest graphRequest = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                try {

                    String firstName = object.getString("first_name");
                    String lastName = object.getString("last_name");
                    String email = object.getString("email");

                    createFacebookAccount(firstName,lastName,email);


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                }


            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields","first_name,last_name,email");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();

    }

    private boolean getConnectivity(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createFacebookAccount(final String firstName, final String lastName, final String email){

        ProgressBar mProgressBar = findViewById(R.id.sign_in_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        final String phoneCode = "+961";
        final String phoneNumber = "123456";
        final String password = getRandomString(20);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final int count = preferences.getInt("User Addresses Count", 0);

        Account account = new Account(firstName
                ,lastName
                ,email
                ,password
                ,Integer.parseInt(phoneCode)
                ,Integer.parseInt(phoneNumber)
                ,1
                ,"facebook");

        Gson gson = new Gson();
        String json = gson.toJson(account);
        preferences.edit().putString("Account", json).apply();

        OkHttpClient client = new OkHttpClient();
        JSONObject fbaccount = new JSONObject();
        try {
            fbaccount.put("fname",firstName);
            fbaccount.put("lname",lastName);
            fbaccount.put("email",email);
            fbaccount.put("phonenumber",phoneCode + " " +  phoneNumber);
            fbaccount.put("password",password);
            fbaccount.put("Cpassword",password);
            fbaccount.put("method","facebook");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,fbaccount.toString());
        Request request = new Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(getResources().getString(R.string.backendUrl) + "users/mobile-register/")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) {

                if (!response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error: " + response.body(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(getApplicationContext(),"Successfully logged in ",Toast.LENGTH_SHORT).show();

                            Intent intent;
                            if(home == 1){
                                intent = new Intent(getApplicationContext(),HomeActivity.class);
                            }
                            else{
                                if(count == 0)
                                    intent = new Intent(getApplicationContext(),GetLocationActivity.class);
                                else
                                    intent = new Intent(getApplicationContext(),CheckoutActivity.class);
                            }

                            startActivity(intent);
                            finish();

                        }
                    });
                }

            }
        });

    }

    private static String getRandomString(final int sizeOfRandomString) {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}
