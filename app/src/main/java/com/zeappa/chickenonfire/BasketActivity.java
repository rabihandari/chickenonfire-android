package com.zeappa.chickenonfire;

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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ProgressBar;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);
        setLightStatusBar(this);

        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","BasketActivity");
            startActivity(intent);
            finish();
        }

        basketItems = new ArrayList<>();

        getBasketItems();
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
        basketItemAdapter = new BasketItemAdapter(this,this,basketItems,subtotalValue);
        recyclerView.setAdapter(basketItemAdapter);

        CheckMinimumOrder(grandTotal);

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

    @SuppressLint("SetTextI18n")
    public void CheckMinimumOrder(double total){

        TextView minumumOrderText = findViewById(R.id.basket_minimum_order_amount);
        minumumOrderText.setText(getResources().getString(R.string.minimum_order_amount) + " " + ((RestaurantApplication) this.getApplication()).getMinimumOrder());

        Button checkoutButton = findViewById(R.id.checkout_button);
        double minimumOrder = ((RestaurantApplication) this.getApplication()).getMinimumOrder();
        if(total < minimumOrder){
            checkoutButton.setEnabled(false);
            minumumOrderText.setVisibility(View.VISIBLE);
        }
        else{
            checkoutButton.setEnabled(true);
            minumumOrderText.setVisibility(View.GONE);
        }
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
                if(count == 0)
                    intent = new Intent(this,GetLocationActivity.class);
                else
                    intent = new Intent(this,CheckoutActivity.class);
            }
        }


        startActivity(intent);

    }

    private boolean getConnectivity(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }
}
