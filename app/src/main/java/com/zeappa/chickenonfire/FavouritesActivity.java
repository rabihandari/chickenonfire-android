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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class FavouritesActivity extends AppCompatActivity {

    TextView noItemsYet;
    RecyclerView menuRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        setLightStatusBar(this);

        noItemsYet = findViewById(R.id.favourites_no_items_yet);
        menuRecyclerView = findViewById(R.id.favourites_recycleview);

        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","FavouritesActivity");
            startActivity(intent);
            finish();
        }
        
        getFavourites();
    }

    private void getFavourites() {

        final String backendUrl = getResources().getString(R.string.backendUrl);
        final ArrayList<MenuItem> menuItems = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();
        JSONObject item = new JSONObject();
        try {
            item.put("email", getCurrentAccout().getEmailAddress());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,item.toString());
        okhttp3.Request request = new okhttp3.Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(backendUrl + "mobile-api/mobile-get-favourites/")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final okhttp3.Response response) throws IOException {

                if(!response.isSuccessful())
                    return;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            JSONArray responseJSONArray = new JSONArray(response.body().string());

                            if(responseJSONArray.length() == 0){
                                ProgressBar mProgressBar = findViewById(R.id.favourites_progressbar);
                                mProgressBar.setVisibility(View.INVISIBLE);
                                noItemsYet.setVisibility(View.VISIBLE);
                                menuItems.clear();
                                return;
                            }

                            for(int i=0;i<responseJSONArray.length();i++){

                                try {

                                    JSONObject item = responseJSONArray.getJSONObject(i);

                                    int id = item.getInt("pk");
                                    String name = item.getString("nm");
                                    String nameAr = item.getString("nmL");
                                    String description = item.getString("ds");
                                    String descriptionAr = item.getString("dsL");
                                    String imageUrl = item.getString("img");
                                    String url = backendUrl.substring(0, backendUrl.length()-1);
                                    imageUrl = url  + imageUrl;
                                    double price = Double.parseDouble(item.getString("pr"));
                                    int discount = item.getInt("dis");
                                    String flavours = item.get("fl").toString();

                                    com.zeappa.chickenonfire.MenuItem menuItem = new com.zeappa.chickenonfire.MenuItem(
                                            id,
                                            name,
                                            nameAr,
                                            description,
                                            descriptionAr,
                                            imageUrl,
                                            price,
                                            discount,
                                            flavours);

                                    menuItems.add(menuItem);
                                }
                                catch (Exception e){
                                    Toast.makeText(getApplicationContext(), "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            }

                            ProgressBar mProgressBar = findViewById(R.id.favourites_progressbar);
                            mProgressBar.setVisibility(View.INVISIBLE);

                            menuRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            menuRecyclerView.setHasFixedSize(true);
                            menuRecyclerView.setNestedScrollingEnabled(false);
                            MenuItemAdapter menuItemAdapter = new MenuItemAdapter(getApplicationContext(),menuItems);
                            menuRecyclerView.setAdapter(menuItemAdapter);

                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getApplicationContext(), R.anim.layout_animation_fall_down);
                            menuRecyclerView.setLayoutAnimation(animation);



                        } catch (JSONException | IOException e) {
                            Toast.makeText(getApplicationContext(), "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

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

    private boolean getConnectivity(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }

    private Account getCurrentAccout(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("Account", "");
        return gson.fromJson(json, Account.class);

    }

    @Override
    protected void onResume() {
        super.onResume();
        noItemsYet.setVisibility(View.INVISIBLE);
        menuRecyclerView.setAdapter(new MenuItemAdapter(getApplicationContext(),new ArrayList<MenuItem>()));
        getFavourites();
    }
}
