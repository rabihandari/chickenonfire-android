package com.zeappa.chickenonfire;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SearchActivity extends AppCompatActivity {

    MenuItemAdapter menuItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","SearchActivity");
            startActivity(intent);
            finish();
        }

        final ConstraintLayout noResultLayout = findViewById(R.id.no_result_layout);
        EditText searchBox = findViewById(R.id.search_box);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                menuItemAdapter.filter(editable.toString());
                if(menuItemAdapter.getMenuItems().size() == 0){
                    noResultLayout.setVisibility(View.VISIBLE);
                }else{
                    noResultLayout.setVisibility(View.GONE);
                }

            }
        });

        setLightStatusBar(this);
        getMenuItems();

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

    private void getMenuItems() {

        final ArrayList<MenuItem> menuItems = new ArrayList<>();
        final ArrayList<String> addedItems = new ArrayList<>();

        final String backendUrl = getResources().getString(R.string.backendUrl);
        final String mJSONURLRequest = backendUrl + "mobile-api/getMenu/";

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


                                JSONArray items = response.getJSONObject(i).getJSONArray("sci");
                                for(int j = 0; j < items.length(); j++){
                                    JSONObject item = items.getJSONObject(j);

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

                                    if (!addedItems.contains(menuItem.getTitle())){
                                        menuItems.add(menuItem);
                                        addedItems.add(menuItem.getTitle());
                                    }


                                }

                            }

                            ProgressBar mProgressBar = findViewById(R.id.search_progressbar);
                            mProgressBar.setVisibility(View.INVISIBLE);

                            RecyclerView menuRecyclerView = findViewById(R.id.search_recycleview);
                            menuRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            menuRecyclerView.setHasFixedSize(true);
                            menuRecyclerView.setNestedScrollingEnabled(false);
                            menuItemAdapter = new MenuItemAdapter(getApplicationContext(),menuItems);
                            menuRecyclerView.setAdapter(menuItemAdapter);

                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getApplicationContext(), R.anim.layout_animation_fall_down);
                            menuRecyclerView.setLayoutAnimation(animation);


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

    private boolean getConnectivity(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }
}
