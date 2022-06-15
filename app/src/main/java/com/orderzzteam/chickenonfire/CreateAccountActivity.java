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

import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

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

public class CreateAccountActivity extends AppCompatActivity {

    boolean passwordHidden = true;
    CountryCodePicker ccp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        setLightStatusBar(this);

        ccp = findViewById(R.id.ccp);

        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","CreateAccountActivity");
            startActivity(intent);
            finish();
        }

    }

    public void togglePassword(View view) {

        EditText passwordField = findViewById(R.id.create_account_password);
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

    public void createAccount(View view) {

        final ProgressBar mProgressBar = findViewById(R.id.create_account_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        EditText firstName = findViewById(R.id.create_account_fname);
        EditText lastName = findViewById(R.id.create_account_lname);
        final EditText email = findViewById(R.id.create_account_email);
        EditText mobileNo = findViewById(R.id.create_account_mobileno);
        EditText password = findViewById(R.id.create_account_password);
        EditText password2 = findViewById(R.id.create_account_password2);

        if(!isValid(firstName,lastName,email,password, password2)){
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }

        OkHttpClient client = new OkHttpClient();

        JSONObject account = new JSONObject();
        try {
            account.put("fname",firstName.getText().toString());
            account.put("lname",lastName.getText().toString());
            account.put("email",email.getText().toString());
            account.put("phonenumber", "+" + ccp.getSelectedCountryCodeAsInt() + " " +  mobileNo.getText().toString());
            account.put("password",password.getText().toString());
            account.put("Cpassword",password.getText().toString());
            account.put("method","email");
            account.put("language", getCurrentLanguage());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,account.toString());
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
                            try {
                                JSONObject responseJSON = new JSONObject(response.body().string());
                                if(responseJSON.getInt("statusCode") == 400)
                                    Toast.makeText(getApplicationContext(), "Account already exists", Toast.LENGTH_SHORT).show();
                                else{

                                    Toast.makeText(getApplicationContext(),"Account successfully created",Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(getApplicationContext(),SignInWithEmailActivity.class);
                                    intent.putExtra("Email" , email.getText().toString());
                                    startActivity(intent);
                                    finish();
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

    private boolean isValid(EditText firstName, EditText lastName, EditText email, EditText password, EditText password2) {

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if(firstName.getText().toString().isEmpty()){
            firstName.setError(getResources().getString(R.string.please_enter_this_field));
            return false;
        }else if(lastName.getText().toString().isEmpty()){
            lastName.setError(getResources().getString(R.string.please_enter_this_field));
            return false;
        }else if(email.getText().toString().isEmpty()){
            email.setError(getResources().getString(R.string.please_enter_this_field));
            return false;
        }else if(password.getText().toString().isEmpty()){
            password.setError(getResources().getString(R.string.please_enter_this_field));
            return false;
        }else if(!email.getText().toString().matches(emailPattern)){
            email.setError(getResources().getString(R.string.please_enter_a_valid_email));
            return false;
        }else if(password.getText().toString().length() < 6){
            password.setError(getResources().getString(R.string.your_password_must_be_at_least_6_characters));
            return false;
        }else if(!password2.getText().toString().equals(password.getText().toString())){
            password2.setError(getResources().getString(R.string.passwords_do_not_match));
            return false;
        }else
            return true;

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

    private String getCurrentLanguage(){
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);
        return sh.getString("language", "en");
    }

    public void GoBack(View view) {
        finish();
    }
}
