package com.zeappa.chickenonfire;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import com.zeappa.chickenonfire.tools.AppBarStateChangeListener;
import com.zeappa.chickenonfire.tools.MultiTypeCheckAddOnCategoryAdapter;
import com.zeappa.chickenonfire.tools.MyBounceInterpolator;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class ItemOrderActivity extends AppCompatActivity {

    MenuItem menuItem;

    ArrayList<AddOnItem> addOns;
    private MultiTypeCheckAddOnCategoryAdapter adapter;

    private LinkedHashMap<String, List<AddOnItem>> items;

    int itemCount = 1;
    boolean favoured = false;
    private ImageView heart;


    RelativeLayout addToBasketButton;
    RecyclerView recyclerView;
    NestedScrollView nestedScrollView;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_order);
        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","ItemOrderActivity");
            startActivity(intent);
            finish();
        }

        nestedScrollView = findViewById(R.id.item_order_scrollview);

        addToBasketButton = findViewById(R.id.add_to_basket_button);
        addToBasketButton.setClickable(false);
        addToBasketButton.setEnabled(false);


        heart = findViewById(R.id.item_heart_icon);
        heart.setClickable(false);
        heart.setEnabled(false);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setLightStatusBar(this);

        menuItem = getIntent().getParcelableExtra("Menu Item");
        setItemDetails();
        getAddOns();
        isFavored();

        Toolbar mToolbar =  findViewById(R.id.toolbar3);
        setSupportActionBar(mToolbar);
        TextView toolBarTitle = findViewById(R.id.toolbar_title);
        toolBarTitle.setText(getResources().getString(R.string.order_details));
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if(state.name().equals("EXPANDED") || state.name().equals("IDLE")){
                    View mToolBar = findViewById(R.id.ptoolbar);
                    mToolBar.setVisibility(View.GONE);
                }else{
                    View mToolBar = findViewById(R.id.ptoolbar);
                    mToolBar.setVisibility(View.VISIBLE);
                }
            }
        });


    }

    private void isFavored(){

        if(!isLoggedIn()){
            heart.setEnabled(true);
            heart.setClickable(true);
            return;

        }


        OkHttpClient client = new OkHttpClient();
        JSONObject item = new JSONObject();
        try {
            item.put("email",getCurrentAccount().getEmailAddress());
            item.put("id",menuItem.getId());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,item.toString());
        okhttp3.Request request = new okhttp3.Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(getResources().getString(R.string.backendUrl) + "mobile-api/mobile-is-favoured/")
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

                            JSONObject responseJSON = new JSONObject(response.body().string());
                            if(responseJSON.getInt("favoured") == 1){
                                favoured = true;
                                heart.setImageResource(R.drawable.heart_red_icon);
                            }else{
                                heart.setImageResource(R.drawable.heart_icon);
                                favoured = false;
                            }

                            heart.setEnabled(true);
                            heart.setClickable(true);

                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }

    private void getAddOns() {

        items = new LinkedHashMap<>();
        addOns = new ArrayList<>();
        final String backendUrl = getResources().getString(R.string.backendUrl);
        final String mJSONURLRequest = backendUrl + "mobile-api/getAddonOfID/" + menuItem.getId();

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

                                JSONObject models = response.getJSONObject(i);;

                                String header;
                                if(getCurrentLanguage().equals("en")){
                                    header = models.getString("hd");
                                }else{
                                    header = models.getString("hdL");
                                }
                                int chooseMin = models.getInt("cMn");
                                int chooseMax = models.getInt("cMx");
                                boolean free = models.getBoolean("fr");
                                boolean optional = models.getBoolean("opt");
                                JSONArray items = models.getJSONArray("ao");

                                int isOptional,isMultiple;
                                if(optional)
                                    isOptional = 1;
                                else
                                    isOptional = 0;

                                if(chooseMax == 1)
                                    isMultiple = 0;
                                else
                                    isMultiple = 1;

                                // Generate Instruction
                                String instruction;
                                if(chooseMin == chooseMax && chooseMax != -1){
                                    instruction = getResources().getString(R.string.choose) + " " + chooseMax + " " + getResources().getString(R.string.from_the_list);
                                }else if (chooseMin == -1 && chooseMax == -1){
                                    instruction = getResources().getString(R.string.choose_items_from_list);
                                }else if (chooseMin == 0){
                                    instruction = getString(R.string.choose_up_to) + " " + chooseMax + " " + getString(R.string.items_from_the_list);
                                }else if (chooseMin > 0 && chooseMax > chooseMin){
                                    instruction = getResources().getString(R.string.choose_between) + " " + chooseMin + " " + getResources().getString(R.string.and) + " " + chooseMax + " " + getResources().getString(R.string.items);
                                }else{
                                    instruction = getResources().getString(R.string.choose_items_from_list);
                                }

                                for(int j = 0; j < items.length();j++){
                                    JSONObject item = items.getJSONObject(j);
                                    int addOnAID = Integer.parseInt(item.getString("aid"));
                                    String addOnName = item.getString("nm");
                                    String addOnNameAr = item.getString("nmL");
                                    boolean overrideFree = item.getBoolean("ovf");

                                    // Get Price
                                    double addOnPrice;
                                    if(overrideFree)
                                        addOnPrice = Double.parseDouble(item.getString("pr"));
                                    else{
                                        if(free)
                                            addOnPrice = 0;
                                        else
                                            addOnPrice = Double.parseDouble(item.getString("pr"));
                                    }

                                    AddOnItem addOnItem = new AddOnItem(addOnAID,addOnName,addOnNameAr,addOnPrice,header,isOptional,isMultiple, instruction,chooseMin,chooseMax);
                                    addOns.add(addOnItem);

                                    addToList(header,addOnItem);

                                }

                            }

                            setAddOns(addOns);

                        }catch (JSONException e){
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),e + "",Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(getApplicationContext(),error + "",Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);


    }

    private void setAddOns(ArrayList<AddOnItem> addOnItems) {

        if(addOnItems.size() == 0){
            ImageView divider = findViewById(R.id.divider3);
            divider.setVisibility(View.GONE);
        }

        recyclerView = findViewById(R.id.addons_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new MultiTypeCheckAddOnCategoryAdapter(this,AddOnDataFactory.makeAddOns(this,addOnItems),(TextView)findViewById(R.id.total_price_text),(TextView)findViewById(R.id.item_count),recyclerView, nestedScrollView);
        recyclerView.setAdapter(adapter);
        
        ProgressBar mProgressBar = findViewById(R.id.addons_progressbar);
        mProgressBar.setVisibility(View.GONE);

        adapter.expandAll();

        addToBasketButton.setClickable(true);
        addToBasketButton.setEnabled(true);

    }



    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setItemDetails() {

        ImageView cover = findViewById(R.id.app_bar_image);
        Glide.with(this).load(menuItem.getImageUrl()).placeholder(getResources().getDrawable(R.drawable.ic_default_menu_item_hd, null)).into(cover);

        TextView title = findViewById(R.id.item_name);
        TextView description = findViewById(R.id.item_description);
        if(getCurrentLanguage().equals("en")){
            title.setText(menuItem.getTitle());
            description.setText(menuItem.getDescription());
        }else{
            title.setText(menuItem.getTitleAr());
            description.setText(menuItem.getDescriptionAr());
        }
        TextView price = findViewById(R.id.item_price);
        double lastPrice;
        if(menuItem.getDiscount() > 0){
            lastPrice = menuItem.getPrice() - ((menuItem.getPrice() * menuItem.getDiscount()) / 100);
            price.setText(getString(R.string.kd) + " " + String.format("%.3f", lastPrice));
        }else{
            lastPrice  = menuItem.getPrice();
            price.setText(getString(R.string.kd) + " " + String.format("%.3f", lastPrice));
        }
        TextView totalPrice = findViewById(R.id.total_price_text);
        totalPrice.setText(String.format("%.3f", lastPrice) + " " + getString(R.string.kd));
    }

    private void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void addToFavourites(View view){

        if(!isLoggedIn()){
            Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
            intent.putExtra("Home" , 1);
            startActivity(intent);
            return;
        }

        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        final MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);
        heart.startAnimation(myAnim);

        if(favoured){
            heart.setImageResource(R.drawable.heart_icon);
            favoured = false;
        }
        else{
            heart.setImageResource(R.drawable.heart_red_icon);
            favoured = true;
        }

        final OkHttpClient client = new OkHttpClient();
        JSONObject item = new JSONObject();
        try {
            item.put("email",getCurrentAccount().getEmailAddress());
            item.put("id",menuItem.getId());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,item.toString());
        final okhttp3.Request request = new okhttp3.Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(getResources().getString(R.string.backendUrl) + "mobile-api/mobile-add-to-favourites/")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) {

            }
        });

    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        adapter.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        adapter.onRestoreInstanceState(savedInstanceState);
    }


    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void decrementItemCount(View view) {
        TextView totalPrice = findViewById(R.id.total_price_text);
        TextView countText = findViewById(R.id.item_count);
        int count = Integer.parseInt(countText.getText().toString());
        double priceOfOne = Double.parseDouble(totalPrice.getText().toString().split(" ")[0]) / count;
        if(count == 1)
            return;
        count--;
        itemCount = count;
        countText.setText(String.valueOf(count));
        double newPrice = priceOfOne * count;
        totalPrice.setText(String.format("%.3f", newPrice) + " " + getString(R.string.kd));
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void incrementItemCount(View view) {
        TextView totalPrice = findViewById(R.id.total_price_text);
        TextView countText = findViewById(R.id.item_count);
        int count = Integer.parseInt(countText.getText().toString());
        double priceOfOne = Double.parseDouble(totalPrice.getText().toString().split(" ")[0]) / count;
        count++;
        itemCount = count;
        countText.setText(String.valueOf(count));
        double newPrice = priceOfOne * count;
        totalPrice.setText(String.format("%.3f", newPrice) + " " + getString(R.string.kd));
    }

    public void GoBack(View view){
        onBackPressed();
    }


    public void addToBasket(View view) {

        int missingAddOnID = isValid();
        if(missingAddOnID != -1){
            adapter.viewError(missingAddOnID);
            return;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        TextView quantityText = findViewById(R.id.item_count);
        int quantity = Integer.parseInt(quantityText.getText().toString());
        EditText specialRequestText = findViewById(R.id.special_requests_edittext);
        String specialRequest = specialRequestText.getText().toString();
        TextView totalPriceText = findViewById(R.id.total_price_text);
        double totalPrice = Double.parseDouble(totalPriceText.getText().toString().split(" ")[0]);

        BasketItem basketItem = new BasketItem(menuItem.getId(),getSelectedAddOnsAIDs(),menuItem.getTitle(),menuItem.getTitleAr(),getSelectedAddOnsNames(),getSelectedAddOnsNamesAr(),menuItem.getImageUrl(),quantity,specialRequest,totalPrice);

        SharedPreferences.Editor prefsEditor = preferences.edit();
        int count = preferences.getInt("Basket Items Count", 0);

        Gson gson = new Gson();
        String json = gson.toJson(basketItem);
        prefsEditor.putString("Basket Item" + count, json);
        prefsEditor.putInt("Basket Items Count", count + 1);
        prefsEditor.apply();

        onBackPressed();
        finish();

    }

    private ArrayList<Integer> getSelectedAddOnsAIDs(){

        ArrayList<Integer> list = new ArrayList<>();
        LinkedHashMap<String, List<AddOnItem>> selectedAddOns = adapter.getSelectedAddOns();

        for (String key : selectedAddOns.keySet()) {
            List<AddOnItem> clist = selectedAddOns.get(key);
            if (clist.size() > 0) {
                for (AddOnItem addOnItem : clist) {
                    list.add(addOnItem.getAid());
                }
            }
        }

        return list;
    }

    private ArrayList<String> getSelectedAddOnsNames(){

        ArrayList<String> list = new ArrayList<>();
        LinkedHashMap<String, List<AddOnItem>> selectedAddOns = adapter.getSelectedAddOns();

        for (String key : selectedAddOns.keySet()) {
            List<AddOnItem> clist = selectedAddOns.get(key);
            if (clist.size() > 0) {
                for (AddOnItem addOnItem : clist) {
                    list.add(addOnItem.getName());
                }
            }
        }

        return list;
    }

    private ArrayList<String> getSelectedAddOnsNamesAr(){

        ArrayList<String> list = new ArrayList<>();
        LinkedHashMap<String, List<AddOnItem>> selectedAddOns = adapter.getSelectedAddOns();

        for (String key : selectedAddOns.keySet()) {
            List<AddOnItem> clist = selectedAddOns.get(key);
            if (clist.size() > 0) {
                for (AddOnItem addOnItem : clist) {
                    list.add(addOnItem.getNameAr());
                }
            }
        }

        return list;
    }

    private int isValid(){

        for(String key : items.keySet()){
            List<AddOnItem> clist = items.get(key);
            assert clist != null;
            if(clist.get(0).isOptional() == 0){
                boolean found = false;
                int count = clist.get(0).getChooseMin();
                for(int i = 0; i < clist.size(); i++){
                    for(int j = 0; j < getSelectedAddOnsAIDs().size(); j++){
                        if(getSelectedAddOnsAIDs().get(j) == clist.get(i).getAid()){
                            count--;
                            if(count == 0){
                                found = true;
                                break;
                            }
                        }

                    }
                }
                if(!found)
                    return clist.get(0).getAid();
            }
        }
        return -1;
    }

    private void addToList(String mapKey, AddOnItem myItem) {
        List<AddOnItem> itemsList = items.get(mapKey);

        if(itemsList == null) {
            itemsList = new ArrayList<>();
            itemsList.add(myItem);
            items.put(mapKey, itemsList);
        } else {
            if(!itemsList.contains(myItem))
                itemsList.add(myItem);
        }
    }

    private boolean getConnectivity(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
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

    private Account getCurrentAccount(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("Account", "");
        return gson.fromJson(json, Account.class);

    }


    private String getCurrentLanguage(){

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);
        return sh.getString("language", "en");
    }



}
