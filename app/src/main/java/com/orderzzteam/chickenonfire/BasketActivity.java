package com.orderzzteam.chickenonfire;

import android.annotation.SuppressLint;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BasketActivity extends AppCompatActivity {

    ArrayList<BasketItem> basketItems;
    RecyclerView recyclerView;
    BasketItemAdapter basketItemAdapter;
    ArrayList<Integer> unavailableIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);
        setLightStatusBar(this);

        basketItems = new ArrayList<>();
        unavailableIDs = new ArrayList<>();

        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","BasketActivity");
            startActivity(intent);
            finish();
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            unavailableIDs = extras.getIntegerArrayList("Unavailable IDs");
        }

        getBasketItems();
    }


    public void ValidateBasket(double total) {
        boolean isChain = getResources().getBoolean(R.bool.chain);
        boolean minimumOrderValid, branchItemsValid = true;

        // Minimum Order...
        TextView minumumOrderText = findViewById(R.id.basket_minimum_order_amount);
        minumumOrderText.setText(getResources().getString(R.string.minimum_order_amount) + " " + ((RestaurantApplication) this.getApplication()).getMinimumOrder());

        Button checkoutButton = findViewById(R.id.checkout_button);
        double minimumOrder = ((RestaurantApplication) this.getApplication()).getMinimumOrder();
        if(total < minimumOrder){
            minumumOrderText.setVisibility(View.VISIBLE);
            minimumOrderValid = false;
        }
        else{
            minumumOrderText.setVisibility(View.GONE);
            minimumOrderValid = true;
        }


        // Branch Menu Items...
        for(BasketItem basketItem: basketItems){
            if(unavailableIDs.contains(basketItem.getItemID())){
                branchItemsValid = false;
                break;
            }
        }

        checkoutButton.setEnabled(minimumOrderValid && (branchItemsValid || isChain));
    }


    private void getBasketItems() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        int count = preferences.getInt("Basket Items Count", 0);

        double grandTotal = 0;

        for(int i = 0;i < count;i++){
            String json = preferences.getString("Basket Item" + i, "");
            BasketItem basketItem = gson.fromJson(json, BasketItem.class);
            basketItems.add(basketItem);
            grandTotal += basketItem.getTotalPrice();
        }
        
        setBasketItems(grandTotal);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setBasketItems(double grandTotal) {

        TextView subtotalValue = findViewById(R.id.subtotal_value);
        subtotalValue.setText(String.format("%.3f", grandTotal) + " " + getString(R.string.kd));

        recyclerView = findViewById(R.id.basket_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        basketItemAdapter = new BasketItemAdapter(this,this,basketItems, subtotalValue, unavailableIDs);
        recyclerView.setAdapter(basketItemAdapter);

        ValidateBasket(grandTotal);

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



    public void addItems(View view) {
        onBackPressed();
        finish();
    }


    public void checkOut(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int count = preferences.getInt("User Addresses Count", 0);

        Gson gson = new Gson();
        String json = preferences.getString("Account", "");
        Account account = gson.fromJson(json, Account.class);

        Intent intent;
        if(account == null)
            intent = new Intent(this,SignInActivity.class);
        else{
            if(account.getLoginStatus() != 1)
                intent = new Intent(this,SignInActivity.class);
            else{
                if(count == 0 || !hasLocationSaved()){
                    if(Objects.requireNonNull(BranchArea.getSavedArea(this)).type == AreaType.map) {
                        intent = new Intent(this, GetLocationActivity.class);
                    } else {
                        intent = new Intent(this, AddAddressActivity.class);
                    }
                }
                else{
                    intent = new Intent(this,CheckoutActivity.class);
                }
            }
        }

        startActivity(intent);

    }


    private boolean hasLocationSaved(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Gson gson = new Gson();
        String json = preferences.getString("General Area", null);
        BranchArea branchArea = null;
        try {
            branchArea = gson.fromJson(json, BranchArea.class);
        }catch (Exception e){
            Log.e("", Objects.requireNonNull(e.getLocalizedMessage()));
        }

        Gson gson2 = new Gson();
        int count = preferences.getInt("User Addresses Count", 0);
        for (int i = 0; i < count; i++){
            String json2 = preferences.getString("User Address" + (i) , "");
            UserAddress userAddress = gson2.fromJson(json2, UserAddress.class);
            if (userAddress.getBranchArea().getId() == branchArea.getId()){
                return true;
            }
        }
        return false;
    }

    private boolean getConnectivity(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }
}
