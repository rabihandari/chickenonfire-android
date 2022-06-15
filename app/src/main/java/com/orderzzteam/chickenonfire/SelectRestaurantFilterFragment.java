package com.orderzzteam.chickenonfire;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SelectRestaurantFilterFragment extends Fragment implements RestaurantsAdapterInterface {

    View view;
    RecyclerView recyclerView;
    TextView title, desc;

    public SelectRestaurantFilterFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_restaurant_filter, container, false);
        recyclerView = view.findViewById(R.id.select_restraunt_filter_recyclerview);
        title = view.findViewById(R.id.select_restaurant_filter_title);
        desc = view.findViewById(R.id.select_restaurant_filter_desc);

        return view;
    }


    @SuppressLint("SetTextI18n")
    public void SetRestaurants(ArrayList<Restaurant> restaurants, String filter){
        title.setText(filter);
        desc.setText("Restaurants with" + " " + filter.toLowerCase());


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        RestaurantsAdapter restaurantsAdapter = new RestaurantsAdapter(getActivity().getApplication(), getContext(), restaurants, filter);
        recyclerView.setAdapter(restaurantsAdapter);
        restaurantsAdapter.adapterInterface = this;
    }


    @Override
    public void onRestaurantSelected(Restaurant restaurant) {
        BranchArea branchArea = BranchArea.getSavedArea(getContext());

        if (branchArea == null) {
            ((SelectRestaurant)getActivity()).ShakeArea();
            return;
        }

        final Application application = getActivity().getApplication();
        ((RestaurantApplication) application).setBackendUrl(restaurant.getBackendUrl());

        ProgressBar progressBar = getActivity().findViewById(R.id.restaurants_progressview);
        progressBar.setVisibility(View.VISIBLE);

        JSONObject body = new JSONObject();
        try{
            body.put("email", "");
            body.put("password", "");
        }catch (JSONException e){
            Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
        }

        final String url = restaurant.getBackendUrl() + "mobile-api/general-info";
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, url, body, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    ((RestaurantApplication) application).setAppName(restaurant.getName());
                    ((RestaurantApplication) application).setAppNameAr(restaurant.getNameAr());
                    ((RestaurantApplication) application).setLogo(response.getString("Logo"));
                    ((RestaurantApplication) application).setAppDescription(response.getString("Cuisine"));
                    ((RestaurantApplication) application).setAppDescriptionAr(response.getString("Cuisine_Second_Language"));
                    ((RestaurantApplication) application).setMinimumOrder(response.getDouble("Minimum_Order"));
                    ((RestaurantApplication) application).setDeliveryPrice(response.getDouble("AVG_Service_Fee"));
                    ((RestaurantApplication) application).setPreOrder(response.getBoolean("Pre_Order"));
                    ((RestaurantApplication) application).setDeliveryTime(response.getInt("Delivery_Time"));
                    ((RestaurantApplication) application).setHeaderImage(response.has("Mobile_Header_Image") ? response.getString("Mobile_Header_Image") : "");
                    ((RestaurantApplication) application).setCoverImage(response.has("Mobile_Cover_Image") ? response.getString("Mobile_Cover_Image") : "");

                    // Setting Social Media
                    JSONArray socialMediaArray = response.getJSONObject("Social_Media").getJSONArray("Vendor_Accounts");
                    for (int i = 0; i < socialMediaArray.length(); i++) {
                        if (socialMediaArray.getJSONObject(i).getString("name").equals("facebook")) {
                            ((RestaurantApplication) application).setFacebook(((JSONObject) socialMediaArray.get(i)).getString("Link"));
                        }
                        if (socialMediaArray.getJSONObject(i).getString("name").equals("instagram")) {
                            ((RestaurantApplication) application).setInstagram(((JSONObject) socialMediaArray.get(i)).getString("Link"));
                        }
                        if (socialMediaArray.getJSONObject(i).getString("name").equals("twitter")) {
                            ((RestaurantApplication) application).setTwitter(((JSONObject) socialMediaArray.get(i)).getString("Link"));
                        }

                    }

                    // Setting featuredItems
                    JSONArray featuredItemsResponse = response.getJSONArray("featuredItems");
                    List<String> featuredItems = new ArrayList<>();
                    for (int i = 0; i < featuredItemsResponse.length(); i++) {
                        featuredItems.add(featuredItemsResponse.getString(i));
                    }
                    ((RestaurantApplication) application).setFeaturedItems(featuredItems);

                    progressBar.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(getContext(), HomeActivity.class);
                    getActivity().startActivity(intent);

                }catch (JSONException e){
                    Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization",  getActivity().getApplication().getString(R.string.backend_API_Key));
                return map;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void listChanged(boolean isEmpty) {

    }
}
