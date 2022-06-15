package com.orderzzteam.chickenonfire;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyOrders extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_orders);
        setLightStatusBar(this);

        GetOrders();

    }

    public void GoBack(View view) {
        onBackPressed();
    }

    public void RefreshOrders(View view) {
        GetOrders();
    }


    private void GetOrders(){
        ProgressBar progressBar = findViewById(R.id.myorders_progressbar);
        progressBar.setVisibility(View.VISIBLE);

        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<MyOrder> myOrders = MyOrder.getMyOrders(this);
        for (MyOrder myOrder: myOrders) {
            ids.add(myOrder.getId());
        }

        JSONObject orderIDs = new JSONObject();
        try {
            orderIDs.put("orderIDs", ids);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, orderIDs.toString());
        Request request = new Request.Builder()
                .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                .url(getResources().getString(R.string.backendUrl) + "mobile-api/get-order-status")
                .post(body)
                .build();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull final Response response) {
                        if (!response.isSuccessful()) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Error: " + response.toString(), Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });

                        } else {

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    try {
                                        JSONObject responseJSON = new JSONObject(response.body().string());
                                        JSONArray orderStatuses = responseJSON.getJSONArray("ordersStatus");
                                        for (int i = 0; i < orderStatuses.length(); i++){
                                            int id = orderStatuses.getJSONObject(i).getInt("pk");
                                            String status = orderStatuses.getJSONObject(i).getString("orderStatus");
                                            String rejectionReason = orderStatuses.getJSONObject(i).getString("rejectionReason");
                                            String cancellationReason = orderStatuses.getJSONObject(i).getString("cancelationReason");
                                            MyOrder.UpdateOrder(getApplicationContext(), id, status, rejectionReason, cancellationReason);
                                        }

                                        ArrayList<MyOrder> myOrders = MyOrder.getMyOrders(getApplicationContext());

                                        RecyclerView recyclerView = findViewById(R.id.myorders_recyclerview);
                                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                        recyclerView.setHasFixedSize(true);
                                        OrdersAdapter ordersAdapter = new OrdersAdapter(getBaseContext(), myOrders);
                                        recyclerView.setAdapter(ordersAdapter);

                                        ConstraintLayout noOrdersLayout = findViewById(R.id.no_orders_layout);
                                        if (myOrders.isEmpty()){
                                            noOrdersLayout.setVisibility(View.VISIBLE);
                                        } else {
                                            noOrdersLayout.setVisibility(View.GONE);
                                        }

                                    } catch (JSONException | IOException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }
                });

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
