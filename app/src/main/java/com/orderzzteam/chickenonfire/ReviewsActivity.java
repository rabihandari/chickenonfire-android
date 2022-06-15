package com.orderzzteam.chickenonfire;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ReviewsActivity extends AppCompatActivity {

    private static final int REVIEWS_LIMIT = 3;

    ArrayList<Review> reviews;
    private ArrayList<Review> visibleReviews = new ArrayList<>();
    private int visibleReviewsNum = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        setLightStatusBar(this);

        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","ReviewsActivity");
            startActivity(intent);
            finish();
        }

    }

    private void getReviews() {

        reviews = new ArrayList<>();

        final String backendUrl = getResources().getString(R.string.backendUrl);
        final String mJSONURLRequest = backendUrl + "mobile-api/get-reviews/";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                mJSONURLRequest,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try{
                            for(int i=0;i<response.length();i++){

                                JSONObject fields = response.getJSONObject(i);

                                String userName = fields.getString("userName");
                                String date = fields.getString("date");
                                double orderPackagingRating = Double.parseDouble(fields.getString("orderPackaginRating"));
                                double valueForMoneyRating = Double.parseDouble(fields.getString("valueForMoneyRating"));
                                double deliveryTimeRating = Double.parseDouble(fields.getString("deliveryTimeRating"));
                                double qualityOfFoodRating = Double.parseDouble(fields.getString("qualityOfFoodRating"));
                                String comment = fields.getString("comment");

                                Review review = new Review(userName, date, orderPackagingRating, valueForMoneyRating, deliveryTimeRating, qualityOfFoodRating, comment);
                                reviews.add(review);

                            }

                            setReviewsContents(reviews);
                            setReviewsRecyclerView();

                            ProgressBar mProgressBar = findViewById(R.id.reviews_progressbar);
                            mProgressBar.setVisibility(View.INVISIBLE);


                        }catch (JSONException e){
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);

    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setReviewsContents(ArrayList<Review> reviews) {

        float i = 0;
        float j = 0;
        float k = 0;
        float l = 0;

        for(Review review : reviews){
            i += review.getOrderPackaginRating();
            j += review.getValueForMoneyRating();
            k += review.getDeliveryTimeRating();
            l += review.getQualityOfFoodRating();
        }

        i /= reviews.size();
        j /= reviews.size();
        k /= reviews.size();
        l /= reviews.size();

        RatingBar orderPackagingBar = findViewById(R.id.order_pacakaging_rating);
        RatingBar valueOfMoneyBar = findViewById(R.id.value_formoney_rating);
        RatingBar deliveryTimeBar = findViewById(R.id.delivery_time_rating);
        RatingBar qualityOfFoodBar = findViewById(R.id.quality_offood_rating);

        orderPackagingBar.setRating(i);
        valueOfMoneyBar.setRating(j);
        deliveryTimeBar.setRating(k);
        qualityOfFoodBar.setRating(l);

        TextView orderPackagingValue = findViewById(R.id.order_packaging_value);
        TextView valueForMoneyValue = findViewById(R.id.value_formoney_value);
        TextView deliveryTimeValue = findViewById(R.id.delivery_time_value);
        TextView qualityOfFoodValue = findViewById(R.id.quality_offood_value);

        orderPackagingValue.setText(String.format("%.1f", i));
        valueForMoneyValue.setText(String.format("%.1f", j));
        deliveryTimeValue.setText(String.format("%.1f", k));
        qualityOfFoodValue.setText(String.format("%.1f", l));

        TextView ratingText = findViewById(R.id.reviews_rating_text_value);
        ratingText.setText(getRatingText(i,j,k,l));

        TextView basedOnText = findViewById(R.id.reviews_based_on);
        basedOnText.setText(getResources().getString(R.string.based_on) + " " + reviews.size() + " " + getResources().getString(R.string.reviews_smallS));

        ImageView ratingFace = findViewById(R.id.reviews_face_icon);
        Glide.with(this).load(getRatingFace(i, j, k, l)).into(ratingFace);
    }


    private void setReviewsRecyclerView() {

        RecyclerView recyclerView = findViewById(R.id.reviews_recyclerview);
        recyclerView.setAdapter(new ReviewsAdapter(this,this,new ArrayList<Review>()));


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);
        ReviewsAdapter reviewsAdapter = new ReviewsAdapter(this,this,visibleReviews);
        recyclerView.setAdapter(reviewsAdapter);

        addMoreItems(reviewsAdapter);

    }

    public void addMoreItems(ReviewsAdapter reviewsAdapter){

        if(visibleReviewsNum != 0){
            visibleReviews.remove(visibleReviews.size()-1);
        }

        int newLimit = visibleReviewsNum + REVIEWS_LIMIT;
        while (visibleReviewsNum < newLimit){
            if(visibleReviewsNum > (reviews.size()-1)){
                reviewsAdapter.notifyDataSetChanged();
                return;
            }
            visibleReviews.add(reviews.get(visibleReviewsNum));
            visibleReviewsNum++;
        }
        visibleReviews.add(null);

        reviewsAdapter.notifyDataSetChanged();
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

    private String getRatingText(double i, double j, double k, double l){

        double average = (i + j + k + l)/4;

        switch ((int) average){

            case 1:
                return getResources().getString(R.string.very_bad);
            case 2:
                return getResources().getString(R.string.bad);
            case 3:
                return getResources().getString(R.string.good);
            case 5:
                return getResources().getString(R.string.excellent);
            default:
                return getResources().getString(R.string.amazing);
        }
    }

    private Drawable getRatingFace(double i, double j, double k, double l){

        double average = (i + j + k + l)/4;

        switch ((int) average){

            case 1:
                return getResources().getDrawable(R.drawable.very_bad_face_icon);
            case 2:
                return getResources().getDrawable(R.drawable.bad_face_icon);
            case 3:
                return getResources().getDrawable(R.drawable.good_face_icon);
            case 5:
                return getResources().getDrawable(R.drawable.excellent_face_icon);
            default:
                return getResources().getDrawable(R.drawable.amazing_face_icon);
        }
    }

    public void addReview(View view) {

        if(!isLoggedIn()) {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            intent.putExtra("Home", 1);
            startActivity(intent);
            return;
        }

        Intent intent = new Intent(this,AddReviewActivity.class);
        startActivity(intent);

    }



    private boolean isLoggedIn(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("Account", "");
        Account account = gson.fromJson(json, Account.class);
        if(account == null)
            return false;
        else{
            return account.getLoginStatus() == 1;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        visibleReviews = new ArrayList<>();
        visibleReviewsNum = 0;
        getReviews();
    }
}
