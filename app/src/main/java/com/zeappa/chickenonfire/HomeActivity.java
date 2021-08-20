package com.zeappa.chickenonfire;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.Json;
import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.internal.NavigationMenuView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.zeappa.chickenonfire.tools.ActionBottomDialogFragment;
import com.zeappa.chickenonfire.tools.AppBarStateChangeListener;
import com.zeappa.chickenonfire.tools.HomeSliderAdapter;
import com.zeappa.chickenonfire.tools.RestaurantOpenStatus;
import com.zeappa.chickenonfire.tools.FeedbackBottomDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout mDrawer;
    NavigationView nvDrawer;

    private TabLayout tabLayout;
    private Activity activity;
    private SliderView sliderView;

    RecyclerView menuRecyclerView;
    ArrayList<MenuCategory> menuCategories;
    RecyclerView.SmoothScroller smoothScroller;
    int selectedDrawerItem = -1;
    boolean tabScroll = true;
    ConstraintLayout lastOrderLayout;

    LinearLayout reorder,rate;


    private static LinkedHashMap<String, ArrayList<com.zeappa.chickenonfire.MenuItem>> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","HomeActivity");
            startActivity(intent);
            finish();
        }


        activity = this;
        getMenuCategories();

        lastOrderLayout = findViewById(R.id.last_order_layout);
        mDrawer = findViewById(R.id.drawer_layout);
        nvDrawer = findViewById(R.id.nav_view);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            boolean orderSuccess = extras.getBoolean("Order Success" , false);
            if(orderSuccess)
                showOrderDialog();
        }

        sliderView = findViewById(R.id.homeImageSlider);
        smoothScroller = new LinearSmoothScroller(this) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        Toolbar mToolbar =  findViewById(R.id.toolbar2);
        setSupportActionBar(mToolbar);
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if(state.name().equals("EXPANDED") || state.name().equals("IDLE")){
                    View mToolBar = findViewById(R.id.ptoolbar);
                    mToolBar.setVisibility(View.GONE);
                    clearLightStatusBar(activity);
                }else{
                    View mToolBar = findViewById(R.id.ptoolbar);
                    mToolBar.setVisibility(View.VISIBLE);
                    setLightStatusBar(activity);
                }
            }
        });


        setUI();
        getLastOrder();
        getRestaurantStatus();
        getBasket();
        setNavigationDrawer();
        CreateHomeSlider();
        getReviewStatus();

    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setUI() {
        TextView restaurantDesc = findViewById(R.id.restaurant_desc);
        restaurantDesc.setText(getCurrentLanguage().equals("ar") ? ((RestaurantApplication) this.getApplication()).getAppDescriptionAr() : ((RestaurantApplication) this.getApplication()).getAppDescription());

        TextView deliveryTime = findViewById(R.id.delivery_time);
        deliveryTime.setText(String.valueOf(((RestaurantApplication) this.getApplication()).getDeliveryTime()));

        TextView deliveryPrice = findViewById(R.id.delivery_cost);
        deliveryPrice.setText("(" + getResources().getString(R.string.kd) + " " + String.format("%.3f", ((RestaurantApplication) this.getApplication()).getDeliveryPrice()) + " " + getResources().getString(R.string.delivery) + ")");

    }


    private void getLastOrder() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("Last Order", "");
        Order order = gson.fromJson(json, Order.class);

        if(order == null){
            lastOrderLayout.setVisibility(View.GONE);
            return;
        }

        if(order.isDismissed()){
            lastOrderLayout.setVisibility(View.GONE);
            return;
        }

        setLastOrderLayout(order);

    }

    private void setLastOrderLayout(final Order order) {

        RecyclerView recyclerView = findViewById(R.id.last_order_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        FinalOrderAdapter basketItemAdapter = new FinalOrderAdapter(this,order.getBasketItems());
        recyclerView.setAdapter(basketItemAdapter);


        TextView dismiss = findViewById(R.id.dismiss_button);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                order.setDismissed(true);
                AddOrderToPrefs(order);
                lastOrderLayout.setVisibility(View.GONE);

            }
        });

        reorder = findViewById(R.id.reorder_button);
        reorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Reorder(order);

            }
        });

        rate = findViewById(R.id.rate_button);
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FeedbackBottomDialogFragment actionBottomDialogFragment = new FeedbackBottomDialogFragment(getApplicationContext(),HomeActivity.this);
                actionBottomDialogFragment.show(getSupportFragmentManager(), ActionBottomDialogFragment.TAG);

            }
        });


        if(order.isRated())
            rate.setVisibility(View.GONE);
    }

    public void setLastOrderAsRated(){

        rate.setVisibility(View.GONE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("Last Order", "");
        Order order = gson.fromJson(json, Order.class);
        order.setRated(true);
        AddOrderToPrefs(order);

    }


    private void AddOrderToPrefs(Order order) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = preferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(order);
        prefsEditor.putString("Last Order", json);
        prefsEditor.apply();
    }

    private void Reorder(Order order){

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("Reorder", true);
        intent.putExtra("User Address Index",-1);
        startActivity(intent);
    }

    public void restaurantInfo(View view) {

        Intent intent = new Intent(this,RestaurantInfoActivity.class);
        startActivity(intent);

    }

    public void openBasket(View view) {

        Intent intent = new Intent(this,BasketActivity.class);
        startActivity(intent);

    }

    public void openReviews(View view){

        Intent intent = new Intent(this,ReviewsActivity.class);
        startActivity(intent);

    }

    public void searchMenu(View view){

        Intent intent = new Intent(this,SearchActivity.class);
        startActivity(intent);
    }

    public void openCategoryDrawer(View view){
        ActionBottomDialogFragment actionBottomDialogFragment = new ActionBottomDialogFragment(menuCategories,menuRecyclerView,tabLayout);
        actionBottomDialogFragment.show(getSupportFragmentManager(), ActionBottomDialogFragment.TAG);
    }

    private void getMenuCategories() {

        final ProgressBar mProgressBar = findViewById(R.id.menu_progressbar);

        final double bestDiscount = 0;

        items = new LinkedHashMap<>();
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

                                JSONObject model = response.getJSONObject(i);
                                String categoryTitle = model.getString("nm");
                                String categoryTitleAr = model.getString("nmL");


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
                                    double price = item.getDouble("pr");
                                    double discount = item.getDouble("dis");
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

                                    if(menuItem.getDiscount() > bestDiscount){
                                        setPromotedItem(menuItem);
                                    }

                                    if(getCurrentLanguage().equals("en"))
                                        addToList(categoryTitle,menuItem);
                                    else
                                        addToList(categoryTitleAr,menuItem);
                                }

                            }

                            setMenuCategories();

                            mProgressBar.setVisibility(View.INVISIBLE);

                        }catch (JSONException e){

                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            Toast.makeText(getApplicationContext(), e + "", Toast.LENGTH_SHORT).show();

                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){

                        Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), error + "", Toast.LENGTH_SHORT).show();

                    }
                }
        );
        requestQueue.add(jsonArrayRequest);


    }


    @SuppressLint("SetTextI18n")
    private void setPromotedItem(com.zeappa.chickenonfire.MenuItem menuItem){

        TextView promotionText = findViewById(R.id.promotion_value);
        promotionText.setText(menuItem.getDiscount() + "% " + getResources().getString(R.string.discount_on_the) + " " + (getCurrentLanguage().equals("en") ? menuItem.getTitle() : menuItem.getTitleAr()));

    }

    private void setMenuCategories(){

        menuCategories = new ArrayList<>();

        for (String key : items.keySet()) {
            menuCategories.add(new MenuCategory(key,items.get(key).size(), items.get(key)));
        }

        menuRecyclerView = findViewById(R.id.menu_recyclerview);
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuRecyclerView.setHasFixedSize(true);
        MenuCategoryAdapter menuCategoryAdapter = new MenuCategoryAdapter(this, menuCategories);
        menuRecyclerView.setAdapter(menuCategoryAdapter);


        final RelativeLayout viewBasketButton = findViewById(R.id.view_basket_button);
        final boolean[] isHidden = {false};
        final int[] newPosition = {-1};
        menuRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);


                int currentPos = ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).findFirstVisibleItemPosition();
                if(currentPos != newPosition[0]){
                    newPosition[0] = currentPos;
                    tabScroll = false;
                    tabLayout.selectTab(tabLayout.getTabAt(newPosition[0]));
                }

                if(newState == RecyclerView.SCROLL_STATE_IDLE)
                    tabScroll = true;

                if (!recyclerView.canScrollVertically(1)) {
                    ObjectAnimator animation = ObjectAnimator.ofFloat(viewBasketButton, "translationY", 500f);
                    animation.setDuration(300);
                    animation.start();
                    isHidden[0] = true;

                }else{
                    if(isHidden[0]){
                        ObjectAnimator animation = ObjectAnimator.ofFloat(viewBasketButton, "translationY", 0f);
                        animation.setDuration(300);
                        animation.start();
                        isHidden[0] = false;
                    }
                }
            }
        });

        setTabLayout(menuCategories);
    }

    private void getReviewStatus() {


        final ArrayList<Review> reviews = new ArrayList<>();

        final String backendUrl = getResources().getString(R.string.backendUrl);
        final String mJSONURLRequest = backendUrl + "mobile-api/get-reviews/";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                mJSONURLRequest,
                null,
                new Response.Listener<JSONArray>() {
                    @SuppressLint("SetTextI18n")
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


                            TextView ratingText = findViewById(R.id.rating);
                            ratingText.setText(getRatingText(i,j,k,l));

                            TextView basedOnText = findViewById(R.id.rating_count);
                            basedOnText.setText(getResources().getString(R.string.based_on) + " " + reviews.size() + " " + getResources().getString(R.string.reviews_smallS));

                            ImageView ratingFace = findViewById(R.id.ic_good_face);
                            Glide.with(getApplicationContext()).load(getRatingFace(i,j,k,l)).into(ratingFace);

                            ProgressBar mProgressBar = findViewById(R.id.hone_rating_progressbar);
                            mProgressBar.setVisibility(View.GONE);

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        getReviewStatus();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);

    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void getBasket() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        int count = preferences.getInt("Basket Items Count", 0);
        double grandTotalPrice = 0;

        for(int i = 0;i < count;i++){
            String json = preferences.getString("Basket Item"+i, "");
            BasketItem basketItem = gson.fromJson(json, BasketItem.class);
            grandTotalPrice += basketItem.getTotalPrice();
        }

        TextView totalPriceText = findViewById(R.id.total_price_text);
        totalPriceText.setText(getResources().getString(R.string.total) + " " + getResources().getString(R.string.kd) + " " + String.format("%.3f", grandTotalPrice));

        RelativeLayout viewBasketButton = findViewById(R.id.view_basket_button);
        if (count != 0) {
            viewBasketButton = findViewById(R.id.view_basket_button);
            viewBasketButton.setVisibility(View.VISIBLE);
        }
    }

    private void getRestaurantStatus() {

        String status = ((RestaurantApplication) this.getApplication()).getStatus();

        final ImageView closingScrim = findViewById(R.id.closingScrim);
        final TextView closingText = findViewById(R.id.status_text);

        if(status.equalsIgnoreCase("BUSY")){
            closingText.setText(getResources().getString(R.string.busy));
            closingScrim.setVisibility(View.VISIBLE);
            closingText.setVisibility(View.VISIBLE);
        }else if(status.equalsIgnoreCase("CLOSED")){
            closingText.setText(getResources().getString(R.string.closed));
            closingScrim.setVisibility(View.VISIBLE);
            closingText.setVisibility(View.VISIBLE);
        }else{
            closingScrim.setVisibility(View.INVISIBLE);
            closingText.setVisibility(View.INVISIBLE);
        }

    }

    private void setNavigationDrawer() {

        setupDrawerContent(nvDrawer);

        updateDrawerMenu();

        mDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

                if(selectedDrawerItem == -1)
                    return;

                if(selectedDrawerItem == 0){
                    if(!isLoggedIn()){
                        Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
                        intent.putExtra("Home" , 1);
                        startActivity(intent);
                    }else{
                        logOut();
                        restartActivity();
                    }
                }else if(selectedDrawerItem == 1){
                    Intent intent = new Intent(getApplicationContext(),FavouritesActivity.class);
                    startActivity(intent);
                }else if(selectedDrawerItem == 2){
                    try {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                        String shareMessage= "\nLet me recommend you this application\n\n";
                        shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                        startActivity(Intent.createChooser(shareIntent, "choose one"));
                    } catch(Exception e) {
                    }
                }else if(selectedDrawerItem == 3){
                    Uri uriUrl = Uri.parse(((RestaurantApplication) getApplication()).getWebsite());
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                }else if(selectedDrawerItem == 4){
                    showLanguageDialog();
                }else if(selectedDrawerItem == 5){
                    Uri uri = Uri.parse(((RestaurantApplication) getApplication()).getFacebook());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }else if(selectedDrawerItem == 6){
                    Uri uri = Uri.parse(((RestaurantApplication) getApplication()).getInstagram());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }else if(selectedDrawerItem == 7){
                    Uri uri = Uri.parse(((RestaurantApplication) getApplication()).getTwitter());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } else if(selectedDrawerItem == 8){
                    Uri number = Uri.parse("tel:" + ((RestaurantApplication) getApplication()).getPhoneNumber());
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                    startActivity(callIntent);
                } else if(selectedDrawerItem == 9){
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", ((RestaurantApplication) getApplication()).getGmailEmail(), null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                } else if(selectedDrawerItem == 10){
                    Uri uri = Uri.parse(((RestaurantApplication) getApplication()).getDigitalExperts());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } else if(selectedDrawerItem == 11){
                    //Todo Privacy Policy
                    Uri uri = Uri.parse(((RestaurantApplication) getApplication()).getPrivacyPolicy());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
                selectedDrawerItem = -1;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void restartActivity() {
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupDrawerContent(NavigationView navigationView) {

        navigationView.setItemIconTintList(null);
        NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
        if (navigationMenuView != null) {
            navigationMenuView.setVerticalScrollBarEnabled(false);
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });
    }

    public void selectDrawerItem(MenuItem menuItem){


        switch(menuItem.getItemId()) {

            case R.id.sign_in:
                selectedDrawerItem = 0;
                break;
            case R.id.favourites:
                selectedDrawerItem = 1;
                break;
            case R.id.share_app:
                selectedDrawerItem = 2;
                break;
            case R.id.website:
                selectedDrawerItem = 3;
                break;
            case R.id.language:
                selectedDrawerItem = 4;
                break;
            case R.id.facebook:
                selectedDrawerItem = 5;
                break;
            case R.id.instagram:
                selectedDrawerItem = 6;
                break;
            case R.id.twitter:
                selectedDrawerItem = 7;
                break;
            case R.id.dial:
                selectedDrawerItem = 8;
                break;
            case R.id.message_us:
                selectedDrawerItem = 9;
                break;
            case R.id.our_digital_experts:
                selectedDrawerItem = 10;
                break;
            case R.id.privacy_policy:
                selectedDrawerItem = 11;
                break;
            default:
                selectedDrawerItem = -1;


        }


        mDrawer.closeDrawer(GravityCompat.START);

    }

    public void openDrawer(View view){
        mDrawer.openDrawer(GravityCompat.START);
    }

    private void setTabLayout(ArrayList<MenuCategory> menuCategories){
        tabLayout = findViewById(R.id.tabLayout);
        for(int i = 0; i < menuCategories.size(); i++){
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(menuCategories.get(i).getTitle());
            tabLayout.addTab(tab,i,false);

        }

        Objects.requireNonNull(tabLayout.getTabAt(0)).select();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(!tabScroll){
                    tabScroll = true;
                    return;
                }
                smoothScroller.setTargetPosition(tab.getPosition());
                Objects.requireNonNull(menuRecyclerView.getLayoutManager()).startSmoothScroll(smoothScroller);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }


    private void CreateHomeSlider(){

        // Get slider images from assets/slider_images folder
        List<String> featuredItems = ((RestaurantApplication) this.getApplication()).getFeaturedItems();

        HomeSliderAdapter adapter = new HomeSliderAdapter(this, featuredItems);
        sliderView.setSliderAdapter(adapter);

        sliderView.setIndicatorAnimation(IndicatorAnimations.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        sliderView.setScrollTimeInSec(8);
        sliderView.setIndicatorVisibility(true);
        sliderView.startAutoCycle();

    }

    private void showOrderDialog(){
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.heappy_eating))
                .setMessage(getResources().getString(R.string.your_order_has_been_successfully)).setNegativeButton(getResources().getString(R.string.okay),null)
                .show();
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

    private void clearLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(Color.GREEN);
        }
    }

    private static synchronized void addToList(String mapKey, com.zeappa.chickenonfire.MenuItem myItem) {
        ArrayList<com.zeappa.chickenonfire.MenuItem> itemsList = items.get(mapKey);

        if(itemsList == null) {
            itemsList = new ArrayList<>();
            itemsList.add(myItem);
            items.put(mapKey, itemsList);
        } else {
            if(!itemsList.contains(myItem))
                itemsList.add(myItem);
        }
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

    private void logOut(){

        LoginManager.getInstance().logOut();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("Account", "");
        Account account = gson.fromJson(json, Account.class);
        if(account != null){
            account.setLoginStatus(0);
            String json2 = gson.toJson(account);
            preferences.edit().putString("Account", json2).apply();
        }

    }

    private void updateDrawerMenu(){

        Menu menu = nvDrawer.getMenu();
        MenuItem signIn = menu.findItem(R.id.sign_in);
        MenuItem favourites = menu.findItem(R.id.favourites);

        if(isLoggedIn()){
            signIn.setTitle(getResources().getString(R.string.sign_out));
            favourites.setEnabled(true);
            favourites.setVisible(true);
        }else{
            signIn.setTitle(getResources().getString(R.string.sign_in));
            favourites.setEnabled(false);
            favourites.setVisible(false);
        }
    }

    private void showLanguageDialog() {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.language_dialog, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();

        final TextView cancel = alertLayout.findViewById(R.id.language_cancel_button);
        RecyclerView recyclerView = alertLayout.findViewById(R.id.language_dialog_recyclerview);

        ArrayList<String> list = new ArrayList<>();
        list.add("English");
        list.add("Arabic");

        LanguageAdapter adapter = new LanguageAdapter(HomeActivity.this,getApplicationContext(), list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(HomeActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private String getCurrentLanguage(){

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);
        return sh.getString("language", "en");
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        getBasket();
        updateDrawerMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBasket();
        updateDrawerMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getBasket();
        updateDrawerMenu();
    }

    @Override
    public void onBackPressed() {
        finish();
        System.exit(0);
    }
}


