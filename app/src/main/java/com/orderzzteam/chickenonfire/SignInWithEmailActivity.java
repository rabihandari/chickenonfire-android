package com.orderzzteam.chickenonfire;

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
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

public class SignInWithEmailActivity extends AppCompatActivity {

    boolean passwordHidden = true;
    private int home = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_with_email_activity);
        setLightStatusBar(this);

        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","SignInWithEmailActivity");
            startActivity(intent);
            finish();
        }
        EditText emailField = findViewById(R.id.signin_we_email);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            String email = extras.getString("Email");
            emailField.setText(email);
            home = extras.getInt("Home" , 0);

        }

        if(getCurrentAccount() != null){
            emailField.setText(getCurrentAccount().getEmailAddress());
        }

    }

    private boolean getConnectivity(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }

    private void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void GoBack(View view) {
        onBackPressed();
    }

    public void continueAsGuest(View view) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int count = preferences.getInt("User Addresses Count", 0);

        Intent intent;
        if(count == 0)
            intent = new Intent(this,GetLocationActivity.class);
        else
            intent = new Intent(this,CheckoutActivity.class);

        startActivity(intent);
        finish();

    }

    public void togglePassword(View view) {

        EditText passwordField = findViewById(R.id.signin_we_password);
        TextView showPassword = (TextView) view;

        if (passwordHidden) {
            passwordField.setTransformationMethod(null);
            showPassword.setText(getResources().getString(R.string.hide));
            passwordHidden = false;
        }else {
            passwordField.setTransformationMethod(new PasswordTransformationMethod());
            showPassword.setText(getResources().getString(R.string.show));
            passwordHidden = true;
        }

    }

    public void SignIn(View view) {

        final ProgressBar mProgressBar = findViewById(R.id.signin_we_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        EditText emailField = findViewById(R.id.signin_we_email);
        final EditText passwordField = findViewById(R.id.signin_we_password);

        OkHttpClient client = new OkHttpClient();

        JSONObject account = new JSONObject();
        try {
            account.put("email",emailField.getText().toString());
            account.put("password",passwordField.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,account.toString());
        Request request = new Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(getResources().getString(R.string.backendUrl) + "users/mobile-signin/")
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
                            try {
                                JSONObject responseJSON = new JSONObject(response.body().string());
                                if(responseJSON.getInt("statusCode") == 404)
                                    Toast.makeText(getApplicationContext(), "Account not found", Toast.LENGTH_SHORT).show();
                                else if (responseJSON.getInt("statusCode") == 200){
                                    Toast.makeText(getApplicationContext(), "Already logged in", Toast.LENGTH_SHORT).show();
                                }else if (responseJSON.getInt("statusCode") == 201){

                                    Toast.makeText(getApplicationContext(),"Successfully logged in ",Toast.LENGTH_SHORT).show();

                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    int count = preferences.getInt("User Addresses Count", 0);

//                                    String phoneCode = "961";
//                                    String phoneNumber = "";
//                                    if(!responseJSON.getString("phonenumber").isEmpty()){
//
//                                        phoneCode = responseJSON.getString("phonenumber").split(" ")[0];
//                                        phoneCode = phoneCode.replace("+","");
//                                        phoneNumber = responseJSON.getString("phonenumber").split(" ")[1].trim();
//                                    }

                                    Account account = new Account(responseJSON.getString("firstName")
                                            ,responseJSON.getString("lastName")
                                            ,responseJSON.getString("email")
                                            ,passwordField.getText().toString()
                                            ,1
                                            ,"email");


                                    Gson gson = new Gson();
                                    String json = gson.toJson(account);
                                    preferences.edit().putString("Account", json).apply();

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

                                }else{
                                    Toast.makeText(getApplicationContext(),"Something went wrong. Please try again later",Toast.LENGTH_SHORT).show();
                                }

                                mProgressBar.setVisibility(View.INVISIBLE);

                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            }
});

        }

public void forgotPassword(View view) {

        Intent intent = new Intent(this,ForgottenPasswordActivity.class);
        startActivity(intent);

        }

public void createAccount(View view) {

        Intent intent = new Intent(this,CreateAccountActivity.class);
        startActivity(intent);
        }



    private Account getCurrentAccount(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("Account", "");
        return gson.fromJson(json, Account.class);

    }
}
