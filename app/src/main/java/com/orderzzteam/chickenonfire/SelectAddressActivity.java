package com.orderzzteam.chickenonfire;

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

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SelectAddressActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);
        setLightStatusBar(this);

        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","SearchActivity");
            startActivity(intent);
            finish();
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        int count = preferences.getInt("User Addresses Count", 0);

        ArrayList<UserAddress> userAddresses = new ArrayList<>();
        for(int i  = 0; i < count; i++){
            String json = preferences.getString("User Address" + i , "");
            userAddresses.add(gson.fromJson(json, UserAddress.class));
        }

        RecyclerView recyclerView = findViewById(R.id.select_address_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        UserAddressesAdapter userAddressesAdapter = new UserAddressesAdapter(userAddresses, this,this);
        recyclerView.setAdapter(userAddressesAdapter);

    }

    private void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }


    public void addAddress(View view) {
        Intent intent;
        if(Objects.requireNonNull(BranchArea.getSavedArea(this)).type == AreaType.map) {
            intent = new Intent(this, GetLocationActivity.class);
        } else {
            intent = new Intent(this, AddAddressActivity.class);
        }
        intent.putExtra("Editing Index" , -1);
        startActivity(intent);

    }

    public void SelectAddress(int userAddressIndex){
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("User Address Index" , userAddressIndex);
        startActivity(intent);
        finish();
    }

    public void GoBack(View view) {
        onBackPressed();
    }

    private boolean getConnectivity(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }
}
