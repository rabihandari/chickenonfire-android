package com.orderzzteam.chickenonfire;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.orderzzteam.chickenonfire.tools.KnetWebView;
import com.orderzzteam.chickenonfire.tools.VoucherBottomDialogFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckoutActivity extends AppCompatActivity {

    ImageView mapView;
    UserAddress userAddress;
    int userAddressIndex = -1;

    SwitchDateTimeDialogFragment dateTimeDialogFragment;

    Date schedulePicked = null;
    Boolean scheduleValid = true;
    String paymentMethod = "";
    String voucherID = "";
    String recevingMethod = "delivery";

    OkHttpClient httpClient = new OkHttpClient();

    double subtotalValue,totalAmountValue;
    double finalServiceFee = 1;
    double discountValue = 0;
    ArrayList<BasketItem> basketItems;

    Button placeOrder;
    boolean serviceFeeLoading = true;

    ProgressBar mProgressBar;
    CountDownTimer cdt;
    private CheckoutActivity checkoutActivity;
    String restaurnatStatus = "Open";
    BranchArea branchArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        setLightStatusBar(this);
        checkoutActivity = this;

        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","CheckoutActivity");
            startActivity(intent);
            finish();
        }

        mProgressBar = findViewById(R.id.checkout_progressbar);
        placeOrder = findViewById(R.id.place_order_button);
        placeOrder.setEnabled(false);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("General Area", null);
        try {
            branchArea = gson.fromJson(json, BranchArea.class);
        }catch (Exception e){
            Log.e("", Objects.requireNonNull(e.getLocalizedMessage()));
        }

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            int index = extras.getInt("User Address Index", -1);
            if(index == -1){
                userAddress = getLastAddress();
                setUserAddress(userAddress);
            }else{
                userAddress = getAddressOfIndex(index);
                setUserAddress(userAddress);
            }
            boolean reorder = extras.getBoolean("Reorder" , false);
            if(reorder){
                MyOrder myOrder = (MyOrder) extras.getParcelable("My Order");
                if (myOrder == null){
                    getLastOrderItems(userAddress.getLatLng().latitude, userAddress.getLatLng().longitude);
                } else {
                    getReorderedItems(myOrder.getBasketItems() ,userAddress.getLatLng().latitude, userAddress.getLatLng().longitude);
                }
            }else{
                getFinalOrderItems(userAddress.getLatLng().latitude, userAddress.getLatLng().longitude);
            }
        }else{
            userAddress = getLastAddress();
            setUserAddress(userAddress);
            getFinalOrderItems(userAddress.getLatLng().latitude, userAddress.getLatLng().longitude);

        }


        // Setting Map...
        mapView = findViewById(R.id.mapView3);
        if(branchArea.type == AreaType.map) {
            setMapImage();
        } else {
            mapView.setVisibility(View.GONE);
        }


        CheckBox cutleryBox = findViewById(R.id.no_cultery_checkBox);
        cutleryBox.setChecked(false);

        checkRestaurantStatus();
        setDateAndTimePickers();
        setPaymentOptions();
        setReceivingOptions();

    }


    private void setMapImage() {
        LatLng selectedLatLng = userAddress.getLatLng();
        String latitude = String.valueOf(selectedLatLng.latitude);
        String longitude = String.valueOf(selectedLatLng.longitude);
        String apiKey = getResources().getString(R.string.google_maps_key);
        String url = "http://maps.google.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=15&size=500x250&sensor=false&key=" + apiKey;
        Glide.with(this).load(url).into(mapView);

    }

    private void setReceivingOptions() {
        RadioGroup radioGroup = findViewById(R.id.receiving_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                RadioButton checkedRadioButton = findViewById(radioGroup.getCheckedRadioButtonId());
                if(checkedRadioButton.getId() == R.id.delivery_radio_button){
                    recevingMethod = "delivery";
                    getServiceFee(userAddress.getLatLng().latitude, userAddress.getLatLng().longitude);
                } else {
                    recevingMethod = "pickup";
                    setFinalOrderItems(0);
                }
            }
        });
    }


    private void setPaymentOptions() {

        RadioButton knet_radio = findViewById(R.id.knet_radioButton);
        RadioButton credit_radio = findViewById(R.id.creditcard_radioButton);
        RadioButton cash_radio = findViewById(R.id.cash_radioButton);
        ConstraintLayout knet_layout = findViewById(R.id.knet_layout);
        ConstraintLayout credit_layout = findViewById(R.id.credit_card_layout);
        ConstraintLayout cash_layout = findViewById(R.id.cash_layout);

        List<String> paymentMethods = ((RestaurantApplication) this.getApplication()).getPaymentMethods();
        if (!paymentMethods.contains("knet")){
            knet_layout.setVisibility(View.GONE);
            knet_radio.setVisibility(View.GONE);
        }
        if (!paymentMethods.contains("cash")){
            cash_layout.setVisibility(View.GONE);
            cash_radio.setVisibility(View.GONE);
        }
        if (!paymentMethods.contains("master") && !paymentMethods.contains("visa")){
            credit_layout.setVisibility(View.GONE);
            credit_radio.setVisibility(View.GONE);
        }

        RadioGroup radioGroup = findViewById(R.id.payment_radiogroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                RadioButton checkedRadioButton = findViewById(radioGroup.getCheckedRadioButtonId());

                if(checkedRadioButton.getId() == R.id.creditcard_radioButton){

                    if(checkedRadioButton.isChecked()){
                        paymentMethod = "Credit Card";
                    }else{
                        paymentMethod = "";
                    }

                }else if(checkedRadioButton.getId() == R.id.cash_radioButton){

                    if(checkedRadioButton.isChecked()){
                        paymentMethod = "Cash on delivery";
                    }
                    else{
                        paymentMethod = "";
                    }

                }else if(checkedRadioButton.getId() == R.id.knet_radioButton){

                    if(checkedRadioButton.isChecked()){
                        paymentMethod = "KNET";
                    }
                    else{
                        paymentMethod = "";
                    }

                }  else{
                    paymentMethod = "";
                }

                EnablePlaceOrder();

            }
        });

    }


    private void EnablePlaceOrder(){
        if (serviceFeeLoading) return;
        if (paymentMethod.isEmpty()) return;

        placeOrder.setEnabled(true);
    }


    private void setDateAndTimePickers() {
        final Calendar calendar = Calendar.getInstance();

        dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                "Schedule your order",
                "OK",
                "Cancel"
        );
        long now = System.currentTimeMillis() - 1000;

        dateTimeDialogFragment.startAtCalendarView();
        dateTimeDialogFragment.set24HoursMode(false);
        dateTimeDialogFragment.setMinimumDateTime(new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).getTime());
        dateTimeDialogFragment.setMaximumDateTime(new Date(now+(1000*60*60*24*2)));
        dateTimeDialogFragment.setDefaultDateTime(new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)).getTime());

        try {
            dateTimeDialogFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("dd MMMM", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            Log.e("CheckOut", Objects.requireNonNull(e.getMessage()));
        }

        dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(final Date date) {

                final TextView scheduleTextButton = findViewById(R.id.checkout_schedule_button);
                scheduleTextButton.setVisibility(View.GONE);
                final ProgressBar scheduleProgressBar = findViewById(R.id.schedule_progressbar);
                scheduleProgressBar.setVisibility(View.VISIBLE);

                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
                final String timePicked = simpleDateFormat.format(date);

                OkHttpClient client = new OkHttpClient();
                JSONObject jsonAccount = new JSONObject();
                try {
                    jsonAccount.put("schedule", timePicked);
                    jsonAccount.put("brID", branchArea.getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                MediaType mediaType = MediaType.get("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(mediaType,jsonAccount.toString());
                Request request = new Request.Builder()
                        .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                        .url(getResources().getString(R.string.backendUrl) + "mobile-api/schedule-validity")
                        .post(body)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                scheduleProgressBar.setVisibility(View.GONE);
                                scheduleTextButton.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NotNull final Call call, @NotNull final Response response) {

                        if (!response.isSuccessful()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                    scheduleProgressBar.setVisibility(View.GONE);
                                    scheduleTextButton.setVisibility(View.VISIBLE);
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView scheduleText = findViewById(R.id.schedule_value);
                                    scheduleText.setVisibility(View.VISIBLE);

                                    try {
                                        JSONObject responseJSON = new JSONObject(response.body().string());
                                        boolean available = responseJSON.getBoolean("available");
                                        if (response.code() == 200){
                                            schedulePicked = date;
                                            if (available) {
                                                scheduleText.setText(timePicked);
                                                scheduleText.setTextColor(Color.BLACK);
                                                scheduleValid = true;
                                            } else {
                                                scheduleText.setText(getResources().getString(R.string.the_restaurant_will_be_closed_at_this_time));
                                                scheduleText.setTextColor(Color.RED);
                                                scheduleValid = false;
                                            }

                                        }else {
                                            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException | IOException e) {
                                        e.printStackTrace();
                                    }

                                    scheduleProgressBar.setVisibility(View.GONE);
                                    scheduleTextButton.setVisibility(View.VISIBLE);

                                }
                            });
                        }

                    }
                });

            }

            @Override
            public void onNegativeButtonClick(Date date) {

            }
        });

    }

    private void getReorderedItems(ArrayList<BasketItem> _basketItems, double latitude, double longitude) {
        basketItems = new ArrayList<>(_basketItems);

        getServiceFee(latitude, longitude);
    }


    private void getLastOrderItems(double latitude, double longitude) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("Last Order", "");
        Order order = gson.fromJson(json, Order.class);

        basketItems = new ArrayList<>();
        basketItems.addAll(order.getBasketItems());

        getServiceFee(latitude, longitude);
    }

    private void getFinalOrderItems(double latitude, double longitude) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        int count = preferences.getInt("Basket Items Count", 0);


        basketItems = new ArrayList<>();
        for(int i = 0;i < count;i++){
            String json = preferences.getString("Basket Item" + i, "");
            BasketItem basketItem = gson.fromJson(json, BasketItem.class);
            basketItems.add(basketItem);
        }


        getServiceFee(latitude, longitude);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setFinalOrderItems(double serviceFee) {
        RecyclerView recyclerView = findViewById(R.id.checkout_order_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        FinalOrderAdapter basketItemAdapter = new FinalOrderAdapter(this,basketItems);
        recyclerView.setAdapter(basketItemAdapter);

        TextView subtotal = findViewById(R.id.subtotal_value);
        TextView serviceCharge = findViewById(R.id.service_charge_value);
        TextView totalAmount = findViewById(R.id.total_amount_value);

        subtotalValue = 0;
        for(BasketItem basketItem : basketItems){
            subtotalValue += basketItem.getTotalPrice();
        }
        finalServiceFee = serviceFee;
        totalAmountValue = subtotalValue + serviceFee;

        subtotal.setText(String.format("%.3f", subtotalValue) + " " + getString(R.string.kd));
        serviceCharge.setText(String.format("%.3f", serviceFee) + " " + getString(R.string.kd));
        totalAmount.setText(String.format("%.3f", totalAmountValue) + " " + getString(R.string.kd));
    }


    private void getServiceFee(double latitude, double longitude){
        final ProgressBar mProgressBar = findViewById(R.id.checkout_servicefee_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        if(branchArea.type == AreaType.list){
            setFinalOrderItems(branchArea.serviceCharge);
            mProgressBar.setVisibility(View.INVISIBLE);
            serviceFeeLoading = false;
            EnablePlaceOrder();
            return;
        }

        serviceFeeLoading = true;
        final String backendUrl = getResources().getString(R.string.backendUrl);
        final String urlRequest = backendUrl + "mobile-api/get-service-fee?latitude=" + latitude + "&longitude=" + longitude + "&brID=" + branchArea.getId();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                com.android.volley.Request.Method.GET,
                urlRequest,
                null,
                response -> {

                    try {

                        mProgressBar.setVisibility(View.INVISIBLE);
                        serviceFeeLoading = false;
                        EnablePlaceOrder();

                        double serviceFee = response.getDouble("newServiceFee");
                        setFinalOrderItems(serviceFee);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), error.getMessage() , Toast.LENGTH_SHORT).show();

            }
        });

        requestQueue.add(jsonObjectRequest);

    }




    @SuppressLint("SetTextI18n")
    private void setUserAddress(UserAddress userAddress) {

        TextView area = findViewById(R.id.checkout_area);
        TextView blocketc = findViewById(R.id.checkout_block_street_building_floor_apartmentno);
        TextView mobileNo = findViewById(R.id.checkout_mobile);

        String blocketcString = getResources().getString(R.string.block) + " " + userAddress.getBlock() + ", "
                + getResources().getString(R.string.street) + " " + userAddress.getStreet() + ", ";

        if(!userAddress.getHouse().isEmpty())
            blocketcString += getResources().getString(R.string.house) + " " + userAddress.getHouse() + ", ";
        if(!userAddress.getBuilding().isEmpty())
            blocketcString += getResources().getString(R.string.building) + " " + userAddress.getBuilding() + ", ";
        if(userAddress.getFloor() != -1)
            blocketcString += getResources().getString(R.string.floor) + " " + userAddress.getFloor() + ", ";
        if(userAddress.getApartmentNo() != -1)
            blocketcString += getResources().getString(R.string.apartment_no) + " " + userAddress.getApartmentNo() + ", ";
        if(!userAddress.getOffice().isEmpty())
            blocketcString += getResources().getString(R.string.office) + " " + userAddress.getOffice() + ", ";

        blocketcString = blocketcString.trim();
        blocketcString = blocketcString.substring(0, blocketcString.length() - 1);

        area.setText(userAddress.getArea());
        blocketc.setText(blocketcString);
        mobileNo.setText(getResources().getString(R.string.mobile)+ ": +" + userAddress.getPhoneCode() + " " + userAddress.getPhoneNumber());


    }

    private UserAddress getLastAddress() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        UserAddress lastUserAddress = null;
        Gson gson2 = new Gson();
        int count = preferences.getInt("User Addresses Count", 0);
        for (int i = 0; i < count; i++){
            String json2 = preferences.getString("User Address" + (i) , "");
            UserAddress userAddress = gson2.fromJson(json2, UserAddress.class);
            if (userAddress.getBranchArea().getId() == branchArea.getId()){
                userAddressIndex = i;
                lastUserAddress = userAddress;
            }
        }

        if (lastUserAddress == null){
            Gson gson = new Gson();
            String json = preferences.getString("User Address" + (count -1) , "");
            UserAddress userAddress = gson.fromJson(json, UserAddress.class);

            TextView locationError = findViewById(R.id.checkout_location_error);
            if (userAddress.getBranchArea().getId() != branchArea.getId()){
                locationError.setVisibility(View.VISIBLE);
            }else{
                locationError.setVisibility(View.GONE);
            }

            lastUserAddress = userAddress;
        }

        return lastUserAddress;
    }

    private UserAddress getAddressOfIndex(int index) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("User Address" + index , "");

        userAddressIndex = index;
        UserAddress userAddress = gson.fromJson(json, UserAddress.class);

        TextView locationError = findViewById(R.id.checkout_location_error);
        if (userAddress.getBranchArea().getId() != branchArea.getId()){
            locationError.setVisibility(View.VISIBLE);
        }else{
            locationError.setVisibility(View.GONE);
        }
        return userAddress;
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


    public void changeAddress(View view) {

        Intent intent = new Intent(this,SelectAddressActivity.class);
        startActivity(intent);

    }

    public void editAddress(View view) {

        if (userAddress.getBranchArea().getId() != branchArea.getId()){
            return;
        }

        Intent intent = new Intent(this,AddAddressActivity.class);
        intent.putExtra("Editing Index",userAddressIndex);
        startActivity(intent);

    }

    public void viewVoucherCard(View view) {
        VoucherBottomDialogFragment voucherBottomDialogFragment = new VoucherBottomDialogFragment(this, subtotalValue);
        voucherBottomDialogFragment.show(getSupportFragmentManager(), VoucherBottomDialogFragment.TAG);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void setVoucherDiscount(String voucher, double discount){
        ConstraintLayout voucherLayout = findViewById(R.id.voucher_layout);
        ImageView voucherIcon = findViewById(R.id.ic_voucher);
        TextView voucherText = findViewById(R.id.voucher_text);
        TextView voucherValue = findViewById(R.id.voucher_value);

        voucherLayout.setBackgroundColor(getResources().getColor(R.color.lightestGreen));
        voucherIcon.setColorFilter(ContextCompat.getColor(this, R.color.greenColor));
        voucherText.setTextColor(getResources().getColor(R.color.greenColor));
        voucherText.setText(voucher);
        voucherValue.setVisibility(View.VISIBLE);
        TextView totalAmount = findViewById(R.id.total_amount_value);
        voucherValue.setText("- " + String.format("%.3f", (subtotalValue*discount)/100) + " " + getString(R.string.kd));
        double newAmountValue = subtotalValue - ((subtotalValue*discount)/100);
        totalAmountValue = newAmountValue + finalServiceFee;
        totalAmount.setText(String.format("%.3f", totalAmountValue) + " " + getString(R.string.kd));
        voucherID = voucher;
        discountValue = (subtotalValue*discount)/100;

        Toast.makeText(this,"Voucher Added!", Toast.LENGTH_SHORT).show();

    }

    public void openSchedule(View view) {
        dateTimeDialogFragment.show(getSupportFragmentManager(), "dialog_time");
    }


    private boolean isValid(){
        // Restaurant Open Status...
        if(schedulePicked == null){
            if(restaurnatStatus.equalsIgnoreCase("Closed") || restaurnatStatus.equalsIgnoreCase("Busy")){
                showClosedDialog();
                return false;
            }
        }

        // Address Validity...
        if (userAddress.getBranchArea().getId() != branchArea.getId()){
            NestedScrollView scrollView = findViewById(R.id.checkout_scrollview);
            scrollView.fullScroll(NestedScrollView.FOCUS_UP);
            return false;
        }

        // Schedule Validity...
        if (schedulePicked != null && !scheduleValid){
            ShowErrorDialog(getResources().getString(R.string.invalid_schedule_picked), getResources().getString(R.string.the_restaurant_will_be_closed_at_the_time));
            return false;
        };

        // Cash and Schedule Validity...
        if (!((RestaurantApplication) this.getApplication()).getEnableCodAndSchedule()){
            if (paymentMethod.equals("Cash on delivery") && schedulePicked != null){
                ShowErrorDialog(getResources().getString(R.string.cash_on_delivery), getResources().getString(R.string.we_dont_allow_scheduling_with_cash));
                return false;
            }
        }

        return true;
    }

    public void placeOrder(View view) {
        // Check if demo
        boolean demo = getResources().getBoolean(R.bool.demo);
        if (demo){
            ShowErrorDialog(getResources().getString(R.string.demo), getResources().getString(R.string.the_application_is_used_for_demo));
            return;
        }

        if (!isValid()) return;

        // Phone Verification ...
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int phoneVerified = preferences.getInt(String.valueOf(userAddress.getPhoneNumber()),0);
        if(phoneVerified == 0 && ((RestaurantApplication) this.getApplication()).getEnablePhoneVerification()){
            showPhoneDialog();
        }else{
            switch (paymentMethod) {
                case "Cash on delivery":
                    mProgressBar.setVisibility(View.VISIBLE);
                    sendOrder();
                    break;
                case "Credit Card":
                    mProgressBar.setVisibility(View.VISIBLE);
                    InitializePayment("credit");
                    break;
                case "KNET":
                    mProgressBar.setVisibility(View.VISIBLE);
                    InitializePayment("knet");
                    break;
                default:
                    Toast.makeText(this, getResources().getString(R.string.please_select_payment_method), Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    }


    private void checkRestaurantStatus(){

        String status = ((RestaurantApplication) this.getApplication()).getStatus();

        if(status.equalsIgnoreCase("BUSY")){
            restaurnatStatus = "Busy";
        }else if(status.equalsIgnoreCase("CLOSED")){
            restaurnatStatus = "Closed";
        }else{
            restaurnatStatus = "Open";
        }
    }


    // ----------------------- Sending order to the server --------------------- //


    private void sendOrder() {
        placeOrder.setEnabled(false);

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, getOrderJsonObject().toString());
        Request request = new Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(getResources().getString(R.string.backendUrl) + "mobile-api/send-order/")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.INVISIBLE);
                        placeOrder.setEnabled(true);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull final Response response) {
                        if (!response.isSuccessful()) {
                            runOnUiThread(new Runnable() {
                                @SuppressLint("DefaultLocale")
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    placeOrder.setEnabled(true);

                                    try {
                                        JSONObject responseJSON = new JSONObject(response.body().string());
                                        HandleOrderError(responseJSON, false, null);
                                    } catch (JSONException | IOException e) {
                                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    try {

                                        JSONObject responseJSON = new JSONObject(response.body().string());
                                        int orderID = responseJSON.getInt("orderID");

                                        AddOrderToPrefs();
                                        AddToMyOrders(orderID);

                                        removeAllBasketItems();
                                        mProgressBar.setVisibility(View.INVISIBLE);

                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                        intent.putExtra("Order Success" , true);
                                        intent.putExtra("Pickup" , recevingMethod.equals("pickup"));
                                        startActivity(intent);
                                        finish();

                                    } catch (JSONException | IOException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "Failed to place order", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });

    }


    private JSONObject getOrderJsonObject(){
        @SuppressLint("HardwareIds") String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

        CheckBox cutleryBox = findViewById(R.id.no_cultery_checkBox);
        CheckBox leaveOrder = findViewById(R.id.leave_order_checkbox);
        EditText specialRequest = findViewById(R.id.special_request_edittext);
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("Account", "");
        final Account account = gson.fromJson(json, Account.class);
        String email = "";
        if (account != null) {
            if (account.getLoginStatus() == 1){
                email = account.getEmailAddress();
            }
        }

        JSONObject order = new JSONObject();
        JSONObject orderInfo = new JSONObject();
        JSONObject userInfo = new JSONObject();
        try {

            // Adding the User info
            userInfo.put("areaType", branchArea.getType() == AreaType.list ? "list" : "map");
            userInfo.put("subAreaID", branchArea.getSubAreaID());
            userInfo.put("firstName", userAddress.getFirstName());
            userInfo.put("lastName", userAddress.getLastName());
            userInfo.put("area", userAddress.getArea());
            userInfo.put("addressType", userAddress.getAddressType());
            userInfo.put("block", userAddress.getBlock());
            userInfo.put("street",userAddress.getStreet());
            userInfo.put("avenue",userAddress.getAvenue());
            userInfo.put("house",userAddress.getHouse());
            userInfo.put("building",userAddress.getBuilding());
            userInfo.put("office",userAddress.getOffice());
            userInfo.put("floor",userAddress.getFloor());
            userInfo.put("apartmentNo",userAddress.getApartmentNo());
            userInfo.put("phoneCode",userAddress.getPhoneCode());
            userInfo.put("phoneNumber",userAddress.getPhoneNumber());
            userInfo.put("additionalDirections",userAddress.getAdditionalDirections());
            userInfo.put("locationLat",String.valueOf(userAddress.getLatLng().latitude));
            userInfo.put("locationLong",String.valueOf(userAddress.getLatLng().longitude));
            userInfo.put("email", email);
            userInfo.put("deviceID", deviceID);
            userInfo.put("preferredLanguage", getCurrentLanguage());

            // Adding the Order info
            JSONArray menuItems = new JSONArray();
            for(BasketItem basketItem : basketItems){
                JSONArray customization = new JSONArray();
                for (AddOnItem addOnItem: basketItem.getAddOns()) {
                    JSONObject itemCustomization = new JSONObject();
                    itemCustomization.put("aid", addOnItem.getAid());
                    itemCustomization.put("nm", addOnItem.getName());
                    itemCustomization.put("nmL", addOnItem.getNameAr());
                    itemCustomization.put("pr", addOnItem.getPrice());
                    customization.put(itemCustomization);
                }

                JSONObject menuItem = new JSONObject();
                menuItem.put("menuItemID",basketItem.getItemID());
                menuItem.put("specialRequest",basketItem.getSpecialRequest());
                menuItem.put("quantity",basketItem.getQuantity());
                menuItem.put("customization", customization);
                menuItems.put(menuItem);
            }
            orderInfo.put("orderedItems", menuItems);
            orderInfo.put("schedule", schedulePicked == null ? "" : simpleDateFormat.format(schedulePicked));
            orderInfo.put("cutlery", cutleryBox.isChecked());
            orderInfo.put("Leave_order_at_the_door", leaveOrder.isChecked());
            orderInfo.put("subtotal", subtotalValue); // To be removed....
            orderInfo.put("specialRequest", specialRequest.getText().toString());

            // Adding the Order
            order.put("paymentMethod",paymentMethod);
            order.put("pickUp", recevingMethod.equals("pickup"));
            order.put("serviceFee", finalServiceFee);
            order.put("voucherID",voucherID);
            order.put("orderTime",currentTime);
            order.put("userInfo",userInfo);
            order.put("orderInfo",orderInfo);
            order.put("brID", branchArea.getId());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return order;
    }

    private void AddOrderToPrefs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = preferences.edit();

        Order order = new Order(basketItems,false,false);

        Gson gson = new Gson();
        String json = gson.toJson(order);
        prefsEditor.putString("Last Order", json);
        prefsEditor.apply();
    }


    private void AddToMyOrders(int orderID){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = preferences.edit();

        int count = preferences.getInt("My Orders Count", 0);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        final String date = simpleDateFormat.format(schedulePicked != null && scheduleValid ? schedulePicked : new Date());


        MyOrder myOrder = new MyOrder(orderID, "PENDING", date, basketItems, totalAmountValue, "", "");
        Gson gson = new Gson();
        String json = gson.toJson(myOrder);
        prefsEditor.putString("My Order" + count, json);
        prefsEditor.putInt("My Orders Count", count + 1);
        prefsEditor.apply();

    }

    public void removeAllBasketItems() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int count = preferences.getInt("Basket Items Count", 0);

        for(int i = 0;i < count;i++){
            String json = preferences.getString("Basket Item" + i, "");
            preferences.edit().remove(json).apply();
        }

        preferences.edit().putInt("Basket Items Count", 0).apply();



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 3){

            if(resultCode == RESULT_OK){

                if(data == null || data.getExtras() == null){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_wrong_please_try_again),Toast.LENGTH_SHORT).show();
                    return;
                }

                if (data.getExtras().getInt("Payment Status") == 1){
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.transaction_completed),Toast.LENGTH_SHORT).show();
                    sendOrder();
                }else{
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.transaction_failed),Toast.LENGTH_SHORT).show();
                }



            }

        }
    }



    private boolean getConnectivity(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private void ShowErrorDialog(String title, String description){
        AlertDialog.Builder alert = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton(getResources().getString(R.string.okay), null);
        alert.show();

    }

    private void ShowErrorDialog(String title, String description, DialogInterface.OnClickListener listener, String positiveButton){
        AlertDialog.Builder alert = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton(positiveButton, listener)
                .setNegativeButton(getResources().getString(R.string.cancel), null);
        alert.show();

    }

    @SuppressLint("SetTextI18n")
    private void showClosedDialog(){

        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View alertLayout = inflater.inflate(R.layout.custom_dialog, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(CheckoutActivity.this);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();

        final TextView title = alertLayout.findViewById(R.id.custom_dialog_title);
        final TextView message = alertLayout.findViewById(R.id.custom_dialog_message);
        final TextView viewOpeningHours = alertLayout.findViewById(R.id.custom_dialog_view_hours);
        final TextView cancel = alertLayout.findViewById(R.id.custom_dialog_cancel);

        if(restaurnatStatus.equalsIgnoreCase("Closed")){
            title.setText(getResources().getString(R.string.closed));
            message.setText(getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.is_not_available));
        }else if (restaurnatStatus.equalsIgnoreCase("Busy")){
            title.setText(getResources().getString(R.string.busy));
            message.setText(getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.is_current_busy));
            viewOpeningHours.setVisibility(View.INVISIBLE);
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        viewOpeningHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(),RestaurantInfoActivity.class);
                startActivity(intent);
                dialog.cancel();

            }
        });

        dialog.show();

    }



    @SuppressLint("SetTextI18n")
    private void showPhoneDialog(){

        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View alertLayout = inflater.inflate(R.layout.phone_verification_dialog, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(CheckoutActivity.this);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();

        final TextView phoneNumber = alertLayout.findViewById(R.id.phone_dialog_phonenumber);
        final TextView change = alertLayout.findViewById(R.id.phone_dialog_change_button);
        final TextView getCode = alertLayout.findViewById(R.id.phone_verification_getcode);
        final TextView cancel = alertLayout.findViewById(R.id.phone_verification_cancel);
        final ProgressBar mProgressBar = alertLayout.findViewById(R.id.phone_verification_progressbar);

        phoneNumber.setText("+" + userAddress.getPhoneCode() + " " + userAddress.getPhoneNumber());

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(),AddAddressActivity.class);
                intent.putExtra("Editing Index",userAddressIndex);
                startActivity(intent);
            }
        });

        getCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressBar.setVisibility(View.VISIBLE);
                startPhoneVerification("sms",dialog,mProgressBar);


            }
        });

        dialog.show();

    }

    @SuppressLint("SetTextI18n")
    private void showTokenDialog(){

        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View alertLayout = inflater.inflate(R.layout.phone_token_validation_dialog, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(CheckoutActivity.this);
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();

        final TextView phoneNumber = alertLayout.findViewById(R.id.phone_token_phonenumber);
        final EditText number1 = alertLayout.findViewById(R.id.phone_token_number_1);
        final EditText number2 = alertLayout.findViewById(R.id.phone_token_number_2);
        final EditText number3 = alertLayout.findViewById(R.id.phone_token_number_3);
        final EditText number4 = alertLayout.findViewById(R.id.phone_token_number_4);
        final Button verify = alertLayout.findViewById(R.id.phone_token_verify_button);
        final ProgressBar mProgressBar = alertLayout.findViewById(R.id.phone_token_progressbar);
        final TextView cancel = alertLayout.findViewById(R.id.phone_token_cancel);
        final TextView message = alertLayout.findViewById(R.id.phone_token_message);

        addTextListeners(number1,number2,number3,number4,verify);

        phoneNumber.setText("+" + userAddress.getPhoneCode() + " " + userAddress.getPhoneNumber());

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressBar.setVisibility(View.VISIBLE);
                startTokenVerification(dialog,number1.getText().toString(),number2.getText().toString(),number3.getText().toString(),number4.getText().toString(),message,mProgressBar);


            }
        });

        final TextView resendCodeText = alertLayout.findViewById(R.id.resend_code_text);
        final TextView resendCodeTimer = alertLayout.findViewById(R.id.resend_code_timer);
        final TextView callMeText= alertLayout.findViewById(R.id.call_me_text);
        final TextView callMeTimer = alertLayout.findViewById(R.id.call_me_timer);

        final ConstraintLayout resendCodeLayout = alertLayout.findViewById(R.id.constraintLayout4);
        final ConstraintLayout callMeLayout = alertLayout.findViewById(R.id.constraintLayout5);

        cdt = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                resendCodeTimer.setText(String.valueOf(millisUntilFinished / 1000));
                callMeTimer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {

                resendCodeText.setTextColor(Color.BLACK);
                callMeText.setTextColor(Color.BLACK);

                resendCodeTimer.setVisibility(View.INVISIBLE);
                callMeTimer.setVisibility(View.INVISIBLE);

                resendCodeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        resendCodeText.setTextColor(Color.GRAY);
                        callMeText.setTextColor(Color.GRAY);

                        resendCodeTimer.setVisibility(View.VISIBLE);
                        callMeTimer.setVisibility(View.VISIBLE);
                        startPhoneVerification("sms",null,mProgressBar);
                        resendCodeLayout.setOnClickListener(null);
                        cdt.start();


                    }
                });

                callMeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        resendCodeText.setTextColor(Color.GRAY);
                        callMeText.setTextColor(Color.GRAY);

                        resendCodeTimer.setVisibility(View.VISIBLE);
                        callMeTimer.setVisibility(View.VISIBLE);
                        startPhoneVerification("call",null,mProgressBar);
                        callMeLayout.setOnClickListener(null);
                        cdt.start();


                    }
                });
            }

        };

        cdt.start();

        dialog.show();

    }
    // ----------------------------- Phone Verification ------------------------- //

    private void startPhoneVerification(String via, final Dialog dialog, final ProgressBar mProgressBar){

        final String backendUrl = getResources().getString(R.string.backendUrl);
        OkHttpClient client = new OkHttpClient();
        JSONObject item = new JSONObject();
        try {
            item.put("phone_number", userAddress.getPhoneNumber());
            item.put("country_code", userAddress.getPhoneCode());
            item.put("via", via);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,item.toString());
        okhttp3.Request request = new okhttp3.Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(backendUrl + "phone/start-verification/")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final okhttp3.Response response) {

                if(!response.isSuccessful())
                    return;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            mProgressBar.setVisibility(View.INVISIBLE);

                            assert response.body() != null;
                            JSONObject responseJSON = new JSONObject(response.body().string());
                            if(responseJSON.getInt("statusCode") == 201){

                                if (dialog != null) {
                                    dialog.cancel();
                                    showTokenDialog();
                                }

                            }else if (responseJSON.getInt("statusCode") == 403){

                                Toast.makeText(getApplicationContext(), "Authentication Error",Toast.LENGTH_SHORT).show();

                            }else if (responseJSON.getInt("statusCode") == 405){

                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_wait_at_least_one_minute),Toast.LENGTH_SHORT).show();

                            }else{

                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_wrong_please_try_again),Toast.LENGTH_SHORT).show();

                            }

                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }



    private void startTokenVerification(final AlertDialog dialog, String num1, String num2, String num3, String num4, final TextView message, final ProgressBar mProgressBar) {

        final String backendUrl = getResources().getString(R.string.backendUrl);
        OkHttpClient client = new OkHttpClient();
        JSONObject item = new JSONObject();
        try {
            item.put("phone_number", userAddress.getPhoneNumber());
            item.put("country_code", userAddress.getPhoneCode());
            item.put("token", num1 + num2 + num3 + num4);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,item.toString());
        okhttp3.Request request = new okhttp3.Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(backendUrl + "phone/token-verification/")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final okhttp3.Response response) {

                if(!response.isSuccessful())
                    return;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            mProgressBar.setVisibility(View.INVISIBLE);

                            assert response.body() != null;
                            JSONObject responseJSON = new JSONObject(response.body().string());
                            if(responseJSON.getInt("statusCode") == 201){

                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                preferences.edit().putInt(String.valueOf(userAddress.getPhoneNumber()),1).apply();

                                Toast.makeText(getApplicationContext(), "Phone Successfully Verified",Toast.LENGTH_SHORT).show();
                                dialog.cancel();


                            }else if (responseJSON.getInt("statusCode") == 403){

                                Toast.makeText(getApplicationContext(), "Authentication Error",Toast.LENGTH_SHORT).show();

                            }else{

                                message.setText(getResources().getString(R.string.sorry_the_verification_code_you_have_entered_is_incorrect));
                                message.setTextColor(getResources().getColor(R.color.redColor));

                            }

                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }

    // ------------------------------ Text Change Listeners ---------------------- //

    private void addTextListeners(final EditText num1, final EditText num2, final EditText num3, final EditText num4, final Button verify){

        num1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.length() == 1)
                    num2.requestFocus();

            }
        });
        num2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.length() == 1)
                    num3.requestFocus();

            }
        });
        num3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.length() == 1)
                    num4.requestFocus();

            }
        });
        num4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {



            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.length() == 1){

                    if(num1.getText().length() == 1 && num2.getText().length() == 1 && num3.getText().length() == 1 && num4.getText().length() == 1){

                        num4.clearFocus();
                        hideKeyboard(checkoutActivity);
                        verify.setEnabled(true);

                    }

                }
            }
        });

    }



    //------------------------------- BOOKEY --------------------------- //

    public void InitializePayment(String paymentMethod) {
        final ProgressBar progressBar = findViewById(R.id.checkout_progressbar);
        progressBar.setVisibility(View.VISIBLE);

        // Validate Order then Initialize Payment...
        final String validationUrl = getResources().getString(R.string.backendUrl) + "mobile-api/validate-order";
        RequestQueue validationQueue = Volley.newRequestQueue(this);
        JsonObjectRequest validationRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, validationUrl, getOrderJsonObject(), response -> {

            // Initialize Payment...
            final String successSecretKey = getRandomString(20);
            final String requestUrl = "https://pg.bookeey.com/internalapi/api/payment/requestLink";
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, requestUrl, getPayload(paymentMethod, successSecretKey), response1 -> {

                try {
                    progressBar.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(getApplicationContext(), KnetWebView.class);
                    intent.putExtra("URL", response1.getString("PayUrl"));
                    intent.putExtra("Success Secret Key", successSecretKey);
                    startActivityForResult(intent, 3);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error2) {
                    Toast.makeText(getApplicationContext(), error2.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);

                }
            });

            requestQueue.add(jsonObjectRequest);

        }, error -> {

            String body = null;
            try {
                body = new String(error.networkResponse.data,"UTF-8");
                JSONObject errorResponse = new JSONObject(body);
                HandleOrderError(errorResponse, true, paymentMethod);
                progressBar.setVisibility(View.INVISIBLE);
            } catch (UnsupportedEncodingException | JSONException e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }

        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", getResources().getString(R.string.backend_API_Key));
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        validationQueue.add(validationRequest);

    }


    private void HandleOrderError(JSONObject errorResponse, boolean onlinePayment, String paymentMethod){
        try {
            int errorCode = errorResponse.getInt("error");
            switch (errorCode){
                case 1:
                    double newServiceFee = errorResponse.getDouble("newServiceFee");
                    ShowErrorDialog(
                            getResources().getString(R.string.service_fee_changed),
                            getResources().getString(R.string.the_service_fee_has_just_been_changed) + " " + (String.format("%.3f", newServiceFee) + " " + getString(R.string.kd))+ ". " + getResources().getString(R.string.do_you_wish_to_complete),
                            (dialogInterface, i) -> {
                                BranchArea newBranchArea = BranchArea.getSavedArea(getApplicationContext());
                                newBranchArea.serviceCharge = newServiceFee;
                                setFinalOrderItems(newServiceFee);
                                BranchArea.setBranchArea(getApplicationContext(), newBranchArea);
                                if (onlinePayment) {
                                    InitializePayment(paymentMethod);
                                } else {
                                    sendOrder();
                                }
                            },
                            "Continue");
                    return;
                default:
                    ShowErrorDialog("Something went wrong", "Something has gone wrong from our end. Please contact us to help you");
            }


        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private JSONObject getPayload(String paymentMethod, String successSecretKey){

        // Declaring Variables
        String merchantID = "mer20000379";
        String subMerchantID = "mer20000379";
        String transactionRefNumber = getRandomString(12);
        String transactionAmount = String.valueOf(totalAmountValue);
        String successUrl = "https://www.bookeey.com/portal/paymentSuccess?k=" + successSecretKey;
        String failureUrl = "https://www.bookeey.com/portal/paymentfailure";
        String secretKey = "4349231";

        // Create Payload
        JSONObject appInfo = new JSONObject();
        JSONObject merchantDetails = new JSONObject();
        JSONObject moreDetails = new JSONObject();
        JSONObject payerDetails = new JSONObject();
        JSONArray transactionDetails = new JSONArray();
        JSONObject transactionHeader = new JSONObject();

        try {

            // App Info Payload
            appInfo.put("APIVer", "");
            appInfo.put("APPID", "");
            appInfo.put("APPTyp", "");
            appInfo.put("AppVer", "");
            appInfo.put("Country", "");
            appInfo.put("DevcType", "5");
            appInfo.put("HsCode", "");
            appInfo.put("IPAddrs", "");
            appInfo.put("MdlID", "");
            appInfo.put("OS", "Android");
            appInfo.put("UsrSessID", "");

            // Merchant Details Payload
            merchantDetails.put("BKY_PRDENUM", "ECom");
            merchantDetails.put("FURL", failureUrl);
            merchantDetails.put("MerchUID", merchantID);
            merchantDetails.put("SURL", successUrl);


            // More Details Payload
            moreDetails.put("Cust_Data1", "");
            moreDetails.put("Cust_Data2", "");
            moreDetails.put("Cust_Data3", "");


            // Payer Details Payload
            payerDetails.put("Pyr_MPhone", "");
            payerDetails.put("Pyr_Name", "");


            // Transaction Details Payload
            JSONObject transactionDetailsItem = new JSONObject();
            transactionDetailsItem.put("SubMerchUID", subMerchantID);
            transactionDetailsItem.put("Txn_AMT", transactionAmount);
            transactionDetails.put(transactionDetailsItem);


            // Transaction Header Payload
            transactionHeader.put("BKY_Txn_UID", "");
            transactionHeader.put("Merch_Txn_UID", transactionRefNumber);
            transactionHeader.put("PayFor", "ECom");
            transactionHeader.put("PayMethod", paymentMethod);
            transactionHeader.put("Txn_HDR", getRandomString(16));
            transactionHeader.put("hashMac", getHashMac(merchantID, transactionRefNumber, transactionAmount, successUrl, secretKey));


            // Main Payload
            JSONObject payload = new JSONObject();
            payload.put("DBRqst", "PY_ECom");
            payload.put("Do_Appinfo", appInfo);
            payload.put("Do_MerchDtl", merchantDetails);
            payload.put("Do_MoreDtl", moreDetails);
            payload.put("Do_PyrDtl", payerDetails);
            payload.put("Do_TxnDtl", transactionDetails);
            payload.put("Do_TxnHdr", transactionHeader);

            return payload;

        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }

    }

    private static String getRandomString(final int sizeOfRandomString) {
        final String ALLOWED_CHARACTERS ="0123456789";

        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }


    private String getHashMac(String merchantID, String transactionRefNumber, String transactionAmount, String successUrl, String secretKey){

        String sequence = merchantID + "|" + transactionRefNumber + "|" + successUrl + "|" + successUrl + "|" + transactionAmount + "|GEN|" + secretKey + "|" + getRandomString(10);

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert messageDigest != null;
        byte[] digest = messageDigest.digest(sequence.getBytes());
        StringBuilder result = new StringBuilder();
        for (byte b : digest) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        return result.toString();
    }


    private String getCurrentLanguage(){
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);
        return sh.getString("language", "en");
    }

}
