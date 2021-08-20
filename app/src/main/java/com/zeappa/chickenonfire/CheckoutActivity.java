package com.zeappa.chickenonfire;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
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

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.zeappa.chickenonfire.tools.KnetWebView;
import com.zeappa.chickenonfire.tools.RestaurantOpenStatus;
import com.zeappa.chickenonfire.tools.VoucherBottomDialogFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckoutActivity extends AppCompatActivity implements OnMapReadyCallback {

    MapView mapView;
    UserAddress userAddress;
    int userAddressIndex = -1;

    SwitchDateTimeDialogFragment dateTimeDialogFragment;

    Date schedulePicked = null;
    String paymentMethod = "";
    String voucherID = "";

    OkHttpClient httpClient = new OkHttpClient();

    double subtotalValue,totalAmountValue;
    double finalServiceFee = 1;
    double discountValue = 0;
    ArrayList<BasketItem> basketItems;

    Button placeOrder;

    ProgressBar mProgressBar;
    CountDownTimer cdt;
    private CheckoutActivity checkoutActivity;
    String restaurnatStatus = "Open";

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
                getLastOrderItems(userAddress.getLatLng().latitude, userAddress.getLatLng().longitude);
            }else{
                getFinalOrderItems(userAddress.getLatLng().latitude, userAddress.getLatLng().longitude);
            }
        }else{
            userAddress = getLastAddress();
            setUserAddress(userAddress);
            getFinalOrderItems(userAddress.getLatLng().latitude, userAddress.getLatLng().longitude);

        }



        mapView = findViewById(R.id.mapView3);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        CheckBox cutleryBox = findViewById(R.id.no_cultery_checkBox);
        cutleryBox.setChecked(true);

        checkRestaurantStatus();
        setDateAndTimePickers();
        setPaymentOptions();

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
                        placeOrder.setEnabled(true);
                    }else{
                        paymentMethod = "";
                        placeOrder.setEnabled(false);
                    }

                }else if(checkedRadioButton.getId() == R.id.cash_radioButton){

                    if(checkedRadioButton.isChecked()){
                        paymentMethod = "Cash on delivery";
                        placeOrder.setEnabled(true);}
                    else{
                        paymentMethod = "";
                        placeOrder.setEnabled(false);
                    }

                }else if(checkedRadioButton.getId() == R.id.knet_radioButton){

                    if(checkedRadioButton.isChecked()){
                        paymentMethod = "KNET";
                        placeOrder.setEnabled(true);}
                    else{
                        paymentMethod = "";
                        placeOrder.setEnabled(false);
                    }

                }  else{
                    paymentMethod = "";
                    placeOrder.setEnabled(false);
                }

            }
        });

    }

    private void setDateAndTimePickers() {

        final Calendar calendar = Calendar.getInstance();

        dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                "Schedule your order",
                "OK",
                "Cancel"
        );

        dateTimeDialogFragment.startAtCalendarView();
        dateTimeDialogFragment.set24HoursMode(false);
//        dateTimeDialogFragment.setMinimumDateTime(new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.DAY_OF_MONTH)).getTime());
//        dateTimeDialogFragment.setMaximumDateTime(new GregorianCalendar(2025, Calendar.MONDAY, 15).getTime());
//        dateTimeDialogFragment.setDefaultDateTime(new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)).getTime());

        dateTimeDialogFragment.setMinimumDateTime(new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).getTime());
        dateTimeDialogFragment.setMaximumDateTime(new GregorianCalendar(2025, Calendar.MONDAY, 15).getTime());
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
                                            if (available) {
                                                scheduleText.setText(timePicked);
                                                scheduleText.setTextColor(Color.BLACK);
                                                schedulePicked = date;
                                            } else {
                                                scheduleText.setText(getResources().getString(R.string.the_restaurant_will_be_closed_at_this_time));
                                                scheduleText.setTextColor(Color.RED);
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
        totalAmountValue = subtotalValue + serviceFee;

        subtotal.setText(String.format("%.3f", subtotalValue) + " " + getString(R.string.kd));
        serviceCharge.setText(String.format("%.3f", serviceFee) + " " + getString(R.string.kd));
        totalAmount.setText(String.format("%.3f", totalAmountValue) + " " + getString(R.string.kd));
    }


    private void getServiceFee(double latitude, double longitude){

        final ProgressBar mProgressBar = findViewById(R.id.checkout_servicefee_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        placeOrder.setEnabled(false);

        final String backendUrl = getResources().getString(R.string.backendUrl);
        final String urlRequest = backendUrl + "main/api/get-service-Fee/?latitude=" + latitude + "&longitude=" + longitude;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                com.android.volley.Request.Method.GET,
                urlRequest,
                null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            mProgressBar.setVisibility(View.INVISIBLE);
                            placeOrder.setEnabled(false);

                            double serviceFee = response.getDouble("newServiceFee");
                            finalServiceFee = serviceFee;

                            setFinalOrderItems(serviceFee);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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
        Gson gson = new Gson();
        int count = preferences.getInt("User Addresses Count", 0);
        String json = preferences.getString("User Address" + (count -1) , "");

        userAddressIndex = count - 1;
        return gson.fromJson(json, UserAddress.class);
    }

    private UserAddress getAddressOfIndex(int index) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("User Address" + index , "");

        userAddressIndex = index;
        return gson.fromJson(json, UserAddress.class);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)).position(userAddress.getLatLng()).title(getResources().getString(R.string.your_location)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userAddress.getLatLng(),16.0f));

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

    public void changeAddress(View view) {

        Intent intent = new Intent(this,SelectAddressActivity.class);
        startActivity(intent);

    }

    public void editAddress(View view) {

        Intent intent = new Intent(this,AddAddressActivity.class);
        intent.putExtra("Editing Index",userAddressIndex);
        startActivity(intent);

    }

    public void viewVoucherCard(View view) {
        VoucherBottomDialogFragment voucherBottomDialogFragment = new VoucherBottomDialogFragment(this);
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
        totalAmount.setText(String.format("%.3f", newAmountValue + finalServiceFee) + " " + getString(R.string.kd));
        voucherID = voucher;
        discountValue = (subtotalValue*discount)/100;

        Toast.makeText(this,"Voucher Added!", Toast.LENGTH_SHORT).show();

    }

    public void openSchedule(View view) {

        dateTimeDialogFragment.show(getSupportFragmentManager(), "dialog_time");
    }



    public void placeOrder(View view) {

        RestaurantOpenStatus restaurantOpenStatus = new RestaurantOpenStatus(this);

        if(schedulePicked == null){
            if(restaurnatStatus.equalsIgnoreCase("Closed") || restaurnatStatus.equalsIgnoreCase("Busy")){
                showClosedDialog();
                return;
            }
        }else{
            if(!restaurantOpenStatus.isOpen(schedulePicked)){
                showClosedDialog();
                return;
            }
        }


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int phoneVerified = preferences.getInt(String.valueOf(userAddress.getPhoneNumber()),0);
        if(phoneVerified == 0){
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

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

        final Button placeOrderButton = findViewById(R.id.place_order_button);
        placeOrderButton.setEnabled(false);

        CheckBox cutleryBox = findViewById(R.id.no_cultery_checkBox);
        CheckBox leaveOrder = findViewById(R.id.leave_order_checkbox);
        EditText specialRequest = findViewById(R.id.special_request_edittext);
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        JSONObject order = new JSONObject();
        JSONObject orderInfo = new JSONObject();
        JSONObject userInfo = new JSONObject();
        try {

            // Adding the User info
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

            // Adding the Order info
            JSONArray menuItems = new JSONArray();
            for(BasketItem basketItem : basketItems){
                JSONObject menuItem = new JSONObject();
                menuItem.put("menuItemID",basketItem.getItemID());
                menuItem.put("specialRequest",basketItem.getSpecialRequest());
                menuItem.put("quantity",basketItem.getQuantity());
                menuItem.put("addOnsIDs",basketItem.getAddOnsIDs());
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
            order.put("serviceFee", finalServiceFee);
            order.put("discountValue", discountValue);
            order.put("voucherID",voucherID);
            order.put("orderTime",currentTime);
            order.put("userInfo",userInfo);
            order.put("orderInfo",orderInfo);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType,order.toString());
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
                        placeOrderButton.setEnabled(true);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull final Response response) {
                        if (!response.isSuccessful()) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Error: " + response.toString(), Toast.LENGTH_LONG).show();
                                    placeOrderButton.setEnabled(true);
                                }
                            });

                        } else {

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    AddOrderToPrefs();

                                    removeAllBasketItems();
                                    mProgressBar.setVisibility(View.INVISIBLE);

                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    intent.putExtra("Order Success" , true);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    }
                });

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

        final String successSecretKey = getRandomString(20);

        final String requestUrl = "https://pg.bookeey.com/internalapi/api/payment/requestLink";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(requestUrl, getPayload(paymentMethod, successSecretKey), new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    progressBar.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(getApplicationContext(), KnetWebView.class);
                    intent.putExtra("URL", response.getString("PayUrl"));
                    intent.putExtra("Success Secret Key", successSecretKey);
                    startActivityForResult(intent, 3);


                } catch (JSONException e) {

                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                }



            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

        requestQueue.add(jsonObjectRequest);

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



}
