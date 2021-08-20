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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zeappa.chickenonfire.tools.CustomViewPager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class AddReviewActivity extends AppCompatActivity {

    private CustomViewPager starsPager;

    private int orderPackaging = -1;
    private int valueForMoney = -1;
    private int deliveryTime = -1;
    private int qualityOfFood = -1;

    Button submitReview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);
        setLightStatusBar(this);

        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","AddReviewActivity");
            startActivity(intent);
            finish();
        }

        submitReview = findViewById(R.id.add_review_submit);
        submitReview.setEnabled(false);

        StarsReviewAdapter starsReviewAdapter = new StarsReviewAdapter(getSupportFragmentManager());
        starsPager = findViewById(R.id.add_review_pager);
        starsPager.setPagingEnabled(false);
        starsPager.setAdapter(starsReviewAdapter);


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

    public void SetPage(int page){

        starsPager.setCurrentItem(page,true);
    }

    public void SetDone() {
        submitReview.setEnabled(true);
    }

    public void setOrderPackaging(int orderPackaging) {
        this.orderPackaging = orderPackaging;
    }

    public void setValueForMoney(int valueForMoney) {
        this.valueForMoney = valueForMoney;
    }

    public void setDeliveryTime(int deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public void setQualityOfFood(int qualityOfFood) {
        this.qualityOfFood = qualityOfFood;
    }

    public void submitReview(View view) {

        final ProgressBar mProgressBar = findViewById(R.id.add_review_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        EditText comment = findViewById(R.id.add_review_comment);

        OkHttpClient client = new OkHttpClient();
        JSONObject review = new JSONObject();
        try {
            review.put("email",getCurrentAccout().getEmailAddress());
            review.put("orderPackaginRating",orderPackaging);
            review.put("valueForMoneyRating",valueForMoney);
            review.put("deliveryTimeRating",deliveryTime);
            review.put("qualityOfFoodRating",qualityOfFood);
            review.put("comment",comment.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,review.toString());
        okhttp3.Request request = new okhttp3.Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(getResources().getString(R.string.backendUrl) + "mobile-api/mobile-add-review/")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final okhttp3.Response response) throws IOException {

                if(!response.isSuccessful())
                    return;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            mProgressBar.setVisibility(View.INVISIBLE);

                            JSONObject responseJSON = new JSONObject(response.body().string());
                            if(responseJSON.getInt("statusCode") == 201){
                                onBackPressed();
                            }else{
                                Toast.makeText(getApplicationContext(),"Something went wrong. Please try again later",Toast.LENGTH_SHORT).show();
                            }


                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }


    private Account getCurrentAccout(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("Account", "");
        return gson.fromJson(json, Account.class);

    }
}
