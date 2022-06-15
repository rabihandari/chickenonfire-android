package com.orderzzteam.chickenonfire;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SelectRestaurantHomeFragment extends Fragment implements RestaurantsAdapterInterface, RestaurantsFilterAdapterInterface{

    private RecyclerView restaurantsRecyclerView;
    private RestaurantsAdapter restaurantsAdapter;
    private TabLayout tabLayout;
    private ArrayList<Restaurant> restaurants;

    // Temps...
    View view;
    private String keyword = "";
    private String selectedTag = "";

    public SelectRestaurantHomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_select_restaurant_home, container, false);


        // Todo get restaurants from API
        // Setting restaurants filters...
        ArrayList<FilterCategory> filterCategories1 = new ArrayList<>();
        filterCategories1.add(FilterCategory.freeDelivery);
        filterCategories1.add(FilterCategory.fastDelivery);
        ArrayList<FilterCategory> filterCategories2 = new ArrayList<>();
        filterCategories2.add(FilterCategory.deliveryOffers);
        filterCategories2.add(FilterCategory.fiftyPercentOff);
        ArrayList<FilterCategory> filterCategories3 = new ArrayList<>();
        filterCategories3.add(FilterCategory.specialOffers);
        filterCategories3.add(FilterCategory.happyHour);
        ArrayList<FilterCategory> filterCategories4 = new ArrayList<>();
        filterCategories4.add(FilterCategory.vegan);
        filterCategories4.add(FilterCategory.buyOneGetOneFree);
        ArrayList<FilterCategory> filterCategories5 = new ArrayList<>();

        // Setting Restaurants...
        restaurants = new ArrayList<>();
        restaurants.add(new Restaurant(2,
                "Go Doner",
                "جو دونر",
                "Grill Platters",
                "أطباق الشواء",
                "godoner.png",
                "godoner_cover.png",
                4.5,
                "15 - 20 minutes",
                "https://godoner.orderzz.com/",
                filterCategories1));
        restaurants.add(new Restaurant(3,
                "PLT Burgers",
                "بي إل تي برجر",
                "Full Vegan",
                "نباتي كامل",
                "plt_burgers.jpeg",
                "plt_burgers_cover.png",
                4.5,
                "25 - 30 minutes",
                "https://pltburger.orderzz.com/",
                filterCategories4));
        restaurants.add(new Restaurant(4,
                "Pizzylla",
                "بزيلا",
                "Pizza, Italian, American",
                "بيتزا ، إيطالي ، أمريكي",
                "pizzylla.jpg",
                "pizzylla_cover.png",
                4.5,
                "25 - 35 minutes",
                "https://demo.orderzz.com/",
                filterCategories2));
        restaurants.add(new Restaurant(5,
                "Sushi Guy",
                "سوشي جاي",
                "Japanese, Sushi, Seafood",
                "ياباني، سوشي، مأكولات بحرية",
                "sushi_guy.jpg",
                "sushi_guy_cover.png",
                4.5,
                "20 - 30 minutes",
                "https://godoner.orderzz.com/",
                filterCategories3));
        restaurants.add(new Restaurant(6,
                "The Chicken State",
                "دولة الدجاج",
                "Fried Chicken, Fast Food, Wings",
                "دجاج مقلي، وجبات سريعة، أجنحة",
                "the_chicken_state.jpg",
                "the_chicken_state_cover.png",
                4.5,
                "25 - 35 minutes",
                "https://pltburger.orderzz.com/",
                filterCategories5));
        restaurants.add(new Restaurant(7,
                "Glazy Doh",
                "جليزي دوه",
                "Donuts, Cronuts, Desserts",
                "دونات ، كرونات ، حلويات",
                "glazy_doh.jpg",
                "glazy_doh_cover.png",
                3.5,
                "20 - 25 minutes",
                "https://demo.orderzz.com/",
                filterCategories1));
        restaurants.add(new Restaurant(8,
                "Potato City",
                "مدينة البطاطس",
                "Fries, Fast Food, Streetfood",
                "بطاطس ، وجبات سريعة ، طعام الشارع",
                "potato_city.jpg",
                "potato_city_cover.png",
                4,
                "10 - 15 minutes",
                "https://godoner.orderzz.com/",
                filterCategories2));
        restaurants.add(new Restaurant(9,
                "Tawook Time",
                "وقت طاووق",
                "Sandwiches, Wraps, Grills",
                "سندويشات ، راب ، مشاوي",
                "tawook_time.jpg",
                "tawook_time_cover.png",
                4,
                "15 - 20 minutes",
                "https://pltburger.orderzz.com/",
                filterCategories3));
        restaurants.add(new Restaurant(10,
                "Fishermen Basket",
                "سلة الصيادين",
                "Fatayer, Pastries, Pizza",
                "فطاير ، معجنات ، بيتزا",
                "fishermen_basket.jpg",
                "fishermen_basket_cover.png",
                4,
                "25 - 30 minutes",
                "https://demo.orderzz.com/",
                filterCategories5));
        restaurants.add(new Restaurant(11,
                "Kushariz",
                "كشريز",
                "Egyptian, Arabic, Kushari",
                "مصري ، عربي ، كشري",
                "kushariz.jpg",
                "kushariz_cover.png",
                4,
                "25 - 30 minutes",
                "https://godoner.orderzz.com/",
                filterCategories3));
        restaurants.add(new Restaurant(12,
                "Mama's Crepe",
                "كريب ماما",
                "Crepe, Pancakes, Desserts",
                "كريب ، بان كيك ، حلويات",
                "mamas_crepe.jpg",
                "mamas_crepe_cover.png",
                4,
                "10 - 15 minutes",
                "https://pltburger.orderzz.com/",
                filterCategories1));
        restaurants.add(new Restaurant(13,
                "Noodles On Fire",
                "نودلس أون فاير ",
                "Noodles, Chinese, Asian",
                "نودلز ، صيني ، آسيوي",
                "noodles_on_fire.jpg",
                "noodles_on_fire_cover.png",
                4,
                "20 - 30 minutes",
                "https://demo.orderzz.com/",
                filterCategories2));
        restaurants.add(new Restaurant(14,
                "Papa Kumpir",
                "بابا كومبير",
                "Turkish, Streetfood, Potato",
                "تركي ، طعام الشارع ، بطاطا",
                "papa_kumpir.jpg",
                "papa_kumpir_cover.png",
                4,
                "25 - 30 minutes",
                "https://godoner.orderzz.com/",
                filterCategories3));
        restaurants.add(new Restaurant(15,
                "Pasta Call",
                "دعوة باستا",
                "Italian, Pasta",
                "الباستا الايطالية",
                "pasta_call.jpg",
                "pasta_call_cover.png",
                4,
                "10 - 15 minutes",
                "https://pltburger.orderzz.com/",
                filterCategories5));
        restaurants.add(new Restaurant(16,
                "Sandwich Express",
                "ساندوتش اكسبريس",
                "Sandwiches, Fast Food, Wraps",
                "سندويشات، وجبات سريعة، راب",
                "sandwich_express.jpg",
                "sandwich_express_cover.png",
                4,
                "20 - 30 minutes",
                "https://demo.orderzz.com/",
                filterCategories2));
        restaurants.add(new Restaurant(17,
                "Shawarma Wnos",
                "شاورما ونص",
                "Shawarma, Sandwiches, Lebanese",
                "شاورما ، سندويشات ، لبناني",
                "shawarma_wnos.jpg",
                "shawarma_wnos_cover.png",
                4,
                "25 - 30 minutes",
                "https://godoner.orderzz.com/",
                filterCategories5));
        restaurants.add(new Restaurant(18,
                "Ve Bistro",
                "في بيسترو",
                "Healthy, Vegan, Vegetarian",
                "صحي ، نباتي",
                "ve_bistro.jpg",
                "ve_bistro_cover.png",
                4,
                "10 - 15 minutes",
                "https://pltburger.orderzz.com/",
                filterCategories4));
        restaurants.add(new Restaurant(19,
                "Willi's Burgers",
                "ويلي برجر",
                "Burger, American, Fast Food",
                "برجر ، أمريكي ، وجبات سريعة",
                "willis_burgers.jpg",
                "willis_burgers_cover.png",
                4,
                "20 - 30 minutes",
                "https://demo.orderzz.com/",
                filterCategories1));
        restaurants.add(new Restaurant(20,
                "Bo Soltan",
                "بو سلطان",
                "Kuwaiti, Sea Food, Gatherings",
                "كويتي، مأكولات بحرية، تجمعات",
                "bo_soltan.jpg",
                "bo_soltan_cover.png",
                4,
                "25 - 30 minutes",
                "https://godoner.orderzz.com/",
                filterCategories2));
        restaurants.add(new Restaurant(21,
                "Sixties Cake",
                "كعكة الستين",
                "Cakes, Desserts, Bakeries",
                "كعك ، حلويات ، مخابز",
                "sixties_cake.jpg",
                "sixties_cake_cover.png",
                4,
                "10 - 15 minutes",
                "https://pltburger.orderzz.com/",
                filterCategories3));


        restaurantsRecyclerView = view.findViewById(R.id.restaurants_recyclerview);
        restaurantsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        restaurantsRecyclerView.setHasFixedSize(true);
        restaurantsRecyclerView.setNestedScrollingEnabled(false);
        restaurantsAdapter = new RestaurantsAdapter(getActivity().getApplication(), getContext(), restaurants, null);
        restaurantsRecyclerView.setAdapter(restaurantsAdapter);
        restaurantsAdapter.adapterInterface = this;


        // Setting filters...
        EditText searchBar = view.findViewById(R.id.select_restaurant_searchbox2);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                keyword = editable.toString();
                restaurantsAdapter.setFilter(editable.toString(), selectedTag);
            }
        });
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    searchBar.clearFocus();
                    InputMethodManager in = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });


        // Setting tab layout...
        ArrayList<Restaurant.Tag> tags = new ArrayList<>();
        tags.add(new Restaurant.Tag("Pizza", R.drawable.ic_pizza));
        tags.add(new Restaurant.Tag("Vegan", R.drawable.ic_vegan));
        tags.add(new Restaurant.Tag("Fast Food", R.drawable.ic_fastfood));
        tags.add(new Restaurant.Tag("Sandwiches", R.drawable.ic_sandwiches));
        tags.add(new Restaurant.Tag("Sea Food", R.drawable.ic_seafood));
        tags.add(new Restaurant.Tag("Coffee", R.drawable.ic_coffee));

        tabLayout = view.findViewById(R.id.select_restaurant_tablayout);
        tabLayout.setInlineLabel(true);

        for (int i = 0; i < tags.size(); i++){
            Restaurant.Tag tag = tags.get(i);
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(tag.getTagName());
            tab.setIcon(tag.getIconID());
            tabLayout.addTab(tab,i + 1,false);
        }


        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            View tabView = tabLayout.getTabAt(i).view;
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tabView.getLayoutParams();
            layoutParams.weight = 0;
            layoutParams.setMarginEnd(15);
            layoutParams.setMarginEnd(15);
            tabView.setLayoutParams(layoutParams);
            tabLayout.requestLayout();
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tag = tab.getText().toString();
                selectedTag = tag;
                restaurantsAdapter.setFilter(keyword, tag);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        // Setting restaurants filter...
        ArrayList<RestaurantFilter> restaurantFilters = new ArrayList<>();
        restaurantFilters.add(new RestaurantFilter("free_delivery_filter.jpg", FilterCategory.freeDelivery));
        restaurantFilters.add(new RestaurantFilter("buy_one_get_one_free_filter.jpg", FilterCategory.buyOneGetOneFree));
        restaurantFilters.add(new RestaurantFilter("happy_hour_filter.jpg", FilterCategory.happyHour));
        restaurantFilters.add(new RestaurantFilter("vegan_filter.jpg", FilterCategory.vegan));
        restaurantFilters.add(new RestaurantFilter("special_offers_filter.jpg", FilterCategory.specialOffers));
        restaurantFilters.add(new RestaurantFilter("fifty_percent_offer_filter.jpg", FilterCategory.fiftyPercentOff));
        restaurantFilters.add(new RestaurantFilter("fast_delivery_filter.jpg", FilterCategory.fastDelivery));

        RecyclerView filtersRecyclerView = view.findViewById(R.id.select_restaurant_filter_recyclerview);
        filtersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        filtersRecyclerView.setHasFixedSize(true);
        RestaurantsFilterAdapter restaurantsFilterAdapter = new RestaurantsFilterAdapter(getContext(), restaurantFilters);
        filtersRecyclerView.setAdapter(restaurantsFilterAdapter);
        restaurantsFilterAdapter.adapterInterface = this;


        // Setting reset button...
        Button reset = view.findViewById(R.id.select_restaurants_resetfilter);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                didClearFilter();
            }
        });

        return view;
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
        ConstraintLayout notFound = view.findViewById(R.id.select_restaurants_not_found);
        notFound.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }


    @Override
    public void didFilter(FilterCategory filterCategory) {
        ArrayList<Restaurant> filteredRestaurants = new ArrayList<>();
        for (Restaurant restaurant: restaurants){
            if (restaurant.getFilterCategories().contains(filterCategory)){
                filteredRestaurants.add(restaurant);
            }
        }
        ((SelectRestaurant)getActivity()).GoToFilteredRestaurants(filteredRestaurants, convertFilterToString(filterCategory));
    }

    @Override
    public void didClearFilter() {
        // Reset Tags Tab
        tabLayout.selectTab(tabLayout.getTabAt(0));

        // Reset Search box
        EditText searchBar = view.findViewById(R.id.select_restaurant_searchbox2);
        searchBar.setText("");
        searchBar.clearFocus();
        InputMethodManager in = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);

        // Reset restaurants list
        restaurantsAdapter.resetFilters();
    }


    private String convertFilterToString(FilterCategory filterCategory){
        String[] stringArray = filterCategory.toString().split("(?=\\p{Upper})");

        StringBuilder sb = new StringBuilder();
        for (String s : stringArray) {
            s =  s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
            sb.append(s).append(" ");
        }
        return sb.toString().trim();
    }


}
