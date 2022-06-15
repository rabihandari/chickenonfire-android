package com.orderzzteam.chickenonfire;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyOrderDetails extends Activity {

    MyOrder myOrder;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_order_details);
        setLightStatusBar(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            myOrder = extras.getParcelable("My Order");
        }

        TextView toolbarTitle = findViewById(R.id.my_order_details_id);
        toolbarTitle.setText(getResources().getString(R.string.order_id) + "" + myOrder.getId());

        TextView status = findViewById(R.id.my_order_details_status);
        status.setText(getResources().getString(R.string.your_order_status_is) + " " + getStatusLocalized(myOrder.getStatus()).toLowerCase());

        TextView message = findViewById(R.id.my_order_details_message);
        int deliveryTime = ((RestaurantApplication) this.getApplication()).getDeliveryTime();
        message.setText(getStatusDescription(myOrder.getStatus()));

        TextView total = findViewById(R.id.my_order_details_total);
        total.setText(String.format("%.3f", myOrder.getTotal()) + " " + getResources().getString(R.string.kd));

        TextView time = findViewById(R.id.my_order_details_time);
        try {
            String fullTime = myOrder.getDate().split(" ")[1];
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            Date d = df.parse(fullTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.add(Calendar.MINUTE, deliveryTime);
            String newTime = df.format(cal.getTime());
            time.setText(newTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        ImageView statusImage = findViewById(R.id.my_order_details_status_image);
        statusImage.setImageDrawable(getStatusImage(myOrder.getStatus()));

        RecyclerView recyclerView = findViewById(R.id.my_order_details_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        MyOrderDetailsAdapter myOrderDetailsAdapter = new MyOrderDetailsAdapter(this, myOrder.getBasketItems());
        recyclerView.setAdapter(myOrderDetailsAdapter);

    }


    private Drawable getStatusImage(String status) {
        switch (status){
            case "PENDING":
                return getResources().getDrawable(R.drawable.ic_time);
            case "INDELIVERY":
                return getResources().getDrawable(R.drawable.ic_shipping);
            case "REJECTED":
                return getResources().getDrawable(R.drawable.ic_close2);
            case "CANCELLED":
                return getResources().getDrawable(R.drawable.ic_close2);
            case "DELIVERED":
                return getResources().getDrawable(R.drawable.ic_doneall);
                default:
                    return getResources().getDrawable(R.drawable.ic_check);
        }
    }



    private String getStatusLocalized(String status) {
        switch (status){
            case "PENDING":
                return getResources().getString(R.string.pending);
            case "INDELIVERY":
                return getResources().getString(R.string.indelivery);
            case "REJECTED":
                return getResources().getString(R.string.rejected);
            case "CANCELLED":
                return getResources().getString(R.string.cancelled);
            case "DELIVERED":
                return getResources().getString(R.string.delivered);
            default:
                return getResources().getString(R.string.accepted);
        }
    }


    private String getStatusDescription(String status){
        int deliveryTime = ((RestaurantApplication) this.getApplication()).getDeliveryTime();
        switch (status){
            case "PENDING":
                return getResources().getString(R.string.pending_description) + " " + deliveryTime + " " + getResources().getString(R.string.min);
            case "INDELIVERY":
                return getResources().getString(R.string.indelivery_description);
            case "REJECTED":
                return getResources().getString(R.string.rejected_description) + " " + myOrder.getRejectionReason();
            case "CANCELLED":
                return getResources().getString(R.string.cancelled_description) + " " + myOrder.getCancellationReason();
            case "DELIVERED":
                return getResources().getString(R.string.delivered_description);
            default:
                return getResources().getString(R.string.it_could_take_upto) + " " + deliveryTime + " " + getResources().getString(R.string.from_the_order_time);
        }
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
}
