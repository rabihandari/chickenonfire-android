package com.orderzzteam.chickenonfire;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class ForgottenPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotten_password);
        setLightStatusBar(this);


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

    public void resetPassword(View view) {

        final ProgressBar mProgressBar = findViewById(R.id.forget_password_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        EditText email = findViewById(R.id.forgot_password_email);

        final String backendUrl = getResources().getString(R.string.backendUrl);
        OkHttpClient client = new OkHttpClient();
        JSONObject item = new JSONObject();
        try {
            item.put("email", email.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,item.toString());
        okhttp3.Request request = new okhttp3.Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(backendUrl + "users/mobile-password-reset/")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final okhttp3.Response response) {

                if(!response.isSuccessful())
                    return;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            mProgressBar.setVisibility(View.INVISIBLE);

                            assert response.body() != null;
                            JSONObject responseJSON = new JSONObject(response.body().string());
                            if(responseJSON.getInt("statusCode") == 201){

                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.a_message_has_been_sent_to_your_email),Toast.LENGTH_SHORT).show();
                                onBackPressed();
                                finish();

                            }else if (responseJSON.getInt("statusCode") == 403){

                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_wait_a_while_before),Toast.LENGTH_SHORT).show();

                            }else if (responseJSON.getInt("statusCode") == 404){

                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.account_not_found),Toast.LENGTH_SHORT).show();

                            }else{

                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_wrong_please_try_again),Toast.LENGTH_SHORT).show();

                            }



                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }
}
