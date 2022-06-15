package com.orderzzteam.chickenonfire.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.orderzzteam.chickenonfire.Flavour;
import com.orderzzteam.chickenonfire.ItemOrderActivity;
import com.orderzzteam.chickenonfire.MenuItem;
import com.orderzzteam.chickenonfire.MyOrder;
import com.orderzzteam.chickenonfire.MyOrderDetails;
import com.orderzzteam.chickenonfire.R;
import com.orderzzteam.chickenonfire.RestaurantApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.firebase.messaging.Constants.MessageNotificationKeys.TAG;



public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public enum NotificationType {
        none,
        status,
        offer

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            try{
                String type = remoteMessage.getData().get("Type");

                assert type != null;
                if (type.equals("status")){
                    int orderID = Integer.parseInt(Objects.requireNonNull(remoteMessage.getData().get("Order ID")));
                    String status = remoteMessage.getData().get("Status");
                    String rejectionReason = remoteMessage.getData().get("Rejection Reason");
                    String cancelationReason = remoteMessage.getData().get("Cancelation Reason");
                    MyOrder.UpdateOrder(getApplicationContext(), orderID, status, rejectionReason, cancelationReason);
                    showOrderDialog(orderID);
                } else if (type.equals("offer")){
                    int menuItemID = Integer.parseInt(Objects.requireNonNull(remoteMessage.getData().get("MenuItem ID")));
                    String notificationTitle = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : getResources().getString(R.string.new_offer);
                    String notificationBody = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : getResources().getString(R.string.a_new_offer_has_been_released);
                    showOfferDiloag(notificationTitle, notificationBody, menuItemID);
                }
            }
            catch (Exception e){
                Log.d(TAG, "Exception: " + e.getLocalizedMessage());
            }

        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }




    }



    private void showOrderDialog(int orderID) {
        Activity activity = ((RestaurantApplication) this.getApplication()).getActiveActivity();
        activity.runOnUiThread(() -> {

            MyOrder myOrder = MyOrder.getMyOrder(getApplicationContext(), orderID);
            if (myOrder == null) return;

            AlertDialog.Builder alert = new AlertDialog.Builder(activity)
                    .setTitle(getResources().getString(R.string.offer_status_updated))
                    .setMessage(getResources().getString(R.string.your_order_status_has_been_updated))
                    .setPositiveButton(getResources().getString(R.string.view_order), (dialog, which) -> {
                        try {
                            Intent resultIntent = new Intent(getApplicationContext(), MyOrderDetails.class);
                            resultIntent.putExtra("My Order", myOrder);
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                            stackBuilder.addNextIntentWithParentStack(resultIntent);
                            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);
                            resultPendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), null);
            alert.show();
        });
    }


    private void showOfferDiloag(String notificationTitle, String notificationBody, int menuItemID) {
        Activity activity = ((RestaurantApplication) this.getApplication()).getActiveActivity();
        activity.runOnUiThread(() -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(activity)
                    .setTitle(notificationTitle)
                    .setMessage(notificationBody)
                    .setPositiveButton(getResources().getString(R.string.view_offer), (dialog, which) -> {
                        try {
                            if (menuItemID == -1) return;

                            final String backendUrl = getResources().getString(R.string.backendUrl);
                            final String mJSONURLRequest = backendUrl + "mobile-api/getMenuItem?id=" + menuItemID;
                            RequestQueue requestQueue = Volley.newRequestQueue(activity);
                            JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(
                                    com.android.volley.Request.Method.GET,
                                    mJSONURLRequest,
                                    null,
                                    response -> {

                                        try {
                                            String name = response.getString("nm");
                                            String nameAr = response.getString("nmL");
                                            String desc = response.getString("ds");
                                            String descAr = response.getString("dsL");
                                            double price = response.getDouble("pr");
                                            double discount = response.getDouble("dis");
                                            String imageUrl = response.getString("img");
                                            String url = backendUrl.substring(0, backendUrl.length()-1);
                                            imageUrl = url  + imageUrl;

                                            // Flavours...
                                            JSONArray flavoursResponse = response.getJSONArray("fl");
                                            ArrayList<Flavour> flavours = new ArrayList<>();
                                            for (int k = 0; k < flavoursResponse.length(); k++){
                                                JSONObject flavourResponse = flavoursResponse.getJSONObject(k);
                                                String flavourName = flavourResponse.getString("nm");
                                                String flavourNameAr = flavourResponse.getString("nmL");
                                                String flavourImage = flavourResponse.getString("img");

                                                Flavour flavour = new Flavour(flavourName, flavourNameAr, url + flavourImage);
                                                flavours.add(flavour);
                                            }

                                            MenuItem menuItem = new MenuItem(menuItemID, name, nameAr, desc, descAr, imageUrl, price, discount, flavours);
                                            Intent resultIntent = new Intent(getApplicationContext(), ItemOrderActivity.class);
                                            resultIntent.putExtra("Menu Item", menuItem);
                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                                            stackBuilder.addNextIntentWithParentStack(resultIntent);
                                            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);
                                            resultPendingIntent.send();
                                        } catch (Exception e) {
                                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    },
                                    error -> {
                                        Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                            );
                            requestQueue.add(jsonArrayRequest);
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), null);
            alert.show();
        });
    }



    @Override
    public void onNewToken(String token) {
        String deviceName = Build.MODEL;
        @SuppressLint("HardwareIds") String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("name", deviceName);
            jsonData.put("device_id", deviceID);
            jsonData.put("registration_id", token);
            jsonData.put("type", "android");
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getResources().getString(R.string.backendUrl) + "mobile-api/register-device";
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, jsonData, null, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", getResources().getString(R.string.backend_API_Key));
                return map;
            }
        };
        queue.add(request);

    }



}
