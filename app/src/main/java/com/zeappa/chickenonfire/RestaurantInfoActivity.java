package com.zeappa.chickenonfire;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RestaurantInfoActivity extends AppCompatActivity implements OnMapReadyCallback{

    LatLng restaurantLatLng;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_info);
        setLightStatusBar(this);


        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","RestaurantInfoActivity");
            startActivity(intent);
            finish();
        }

        restaurantLatLng = new LatLng(((RestaurantApplication) getApplication()).getLatitude(), ((RestaurantApplication) getApplication()).getLongitude());

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        setUI();

    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void setUI() {
        TextView deliveryTime = findViewById(R.id.restaurant_info_deliverytime_value);
        deliveryTime.setText(((RestaurantApplication) this.getApplication()).getDeliveryTime() + " " + getResources().getString(R.string.min));

        TextView minimumOrder = findViewById(R.id.restaurant_info_minimumorder_value);
        minimumOrder.setText(String.format("%.3f", ((RestaurantApplication) this.getApplication()).getMinimumOrder()) + " " + getResources().getString(R.string.kd));

        TextView serviceCharge = findViewById(R.id.restaurant_info_servicecharge_value);
        serviceCharge.setText(String.format("%.3f", ((RestaurantApplication) this.getApplication()).getDeliveryPrice()) + " " + getResources().getString(R.string.kd));

        TextView preOrder = findViewById(R.id.restaurant_info_preorder_value);
        preOrder.setText(((RestaurantApplication) this.getApplication()).isPreOrder() ?  getResources().getString(R.string.yes) : getResources().getString(R.string.no));

        TextView monday = findViewById(R.id.moday_value);
        WorkDay mondayWorkingDay = ((RestaurantApplication) this.getApplication()).getWorkingDays().get("Monday");
        if (mondayWorkingDay != null){
            monday.setText(openingClosingTime(mondayWorkingDay));
        }

        TextView tuesday = findViewById(R.id.tuesday_value);
        WorkDay tuesdayWorkingDay = ((RestaurantApplication) this.getApplication()).getWorkingDays().get("Tuesday");
        if (tuesdayWorkingDay != null){
            tuesday.setText(openingClosingTime(tuesdayWorkingDay));
        }

        TextView wednesday = findViewById(R.id.wednesday_value);
        WorkDay wednesdayWorkingDay = ((RestaurantApplication) this.getApplication()).getWorkingDays().get("Wednesday");
        if (wednesdayWorkingDay != null){
            wednesday.setText(openingClosingTime(wednesdayWorkingDay));
        }

        TextView thursday = findViewById(R.id.thursday_value);
        WorkDay thursdayWorkingDay = ((RestaurantApplication) this.getApplication()).getWorkingDays().get("Thursday");
        if (thursdayWorkingDay != null){
            thursday.setText(openingClosingTime(thursdayWorkingDay));
        }

        TextView friday = findViewById(R.id.friday_value);
        WorkDay fridayWorkingDay = ((RestaurantApplication) this.getApplication()).getWorkingDays().get("Friday");
        if (fridayWorkingDay != null){
            friday.setText(openingClosingTime(fridayWorkingDay));
        }

        TextView saturday = findViewById(R.id.saturday_value);
        WorkDay saturdayWorkingDay = ((RestaurantApplication) this.getApplication()).getWorkingDays().get("Saturday");
        if (saturdayWorkingDay != null){
            saturday.setText(openingClosingTime(saturdayWorkingDay));
        }

        TextView sunday = findViewById(R.id.sunday_value);
        WorkDay sundayWorkingDay = ((RestaurantApplication) this.getApplication()).getWorkingDays().get("Sunday");
        if (sundayWorkingDay != null){
            sunday.setText(openingClosingTime(sundayWorkingDay));
        }

        TextView knet_text = findViewById(R.id.knet_text);
        TextView credit_text = findViewById(R.id.credit_card_text);
        TextView cash_text = findViewById(R.id.cash_on_delivery_text);
        ImageView knet_image = findViewById(R.id.ic_knet);
        ImageView credit_image = findViewById(R.id.ic_creditcard);
        ImageView cash_image = findViewById(R.id.ic_cashondelivery);

        List<String> paymentMethods = ((RestaurantApplication) this.getApplication()).getPaymentMethods();
        if (!paymentMethods.contains("knet")){
            knet_text.setVisibility(View.GONE);
            knet_image.setVisibility(View.GONE);
        }
        if (!paymentMethods.contains("cash")){
            cash_text.setVisibility(View.GONE);
            cash_image.setVisibility(View.GONE);
        }
        if (!paymentMethods.contains("master") && !paymentMethods.contains("visa")){
            credit_text.setVisibility(View.GONE);
            credit_image.setVisibility(View.GONE);
        }


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

    public void getDirections(View view) {

        String uri;

        if(restaurantLatLng == null)
            return;

        uri = "http://maps.google.com/maps?daddr=" + restaurantLatLng.latitude + ","+ restaurantLatLng.longitude;

        Uri gmmIntentUri = Uri.parse(uri);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.addMarker(new MarkerOptions().position(restaurantLatLng).title("Marker to " + getResources().getString(R.string.app_name)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantLatLng,16.0f));
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    private boolean getConnectivity(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }

    @SuppressLint("DefaultLocale")
    private String openingClosingTime(WorkDay workDay){
        String openingAmPm = workDay.getOpeningAmPm();
        String closingAmPm = workDay.getClosingAmPm();
        openingAmPm = openingAmPm.equalsIgnoreCase("AM") ? getResources().getString(R.string.am) : getResources().getString(R.string.pm);
        closingAmPm = closingAmPm.equalsIgnoreCase("AM") ? getResources().getString(R.string.am) : getResources().getString(R.string.pm);
        return workDay.getOpeningHour() + ":" + String.format("%02d", workDay.getOpeningMinute()) + " " + openingAmPm + " - " + workDay.getClosingHour() + ":" + String.format("%02d", workDay.getClosingMinute()) + " " + closingAmPm;
    }


    private String getCurrentLanguage(){
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);
        return sh.getString("language", "en");
    }
}
