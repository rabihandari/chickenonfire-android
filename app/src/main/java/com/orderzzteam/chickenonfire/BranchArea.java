package com.orderzzteam.chickenonfire;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

enum AreaType {
    map,
    list
}

public class BranchArea{
    int id;
    int areaID;
    int subAreaID;
    AreaType type;
    String areaName;
    double latitude;
    double longitude;
    List<LatLng> points;
    double serviceCharge;


    public BranchArea(int id, int areaID, int subareaID, String areaName, double serviceCharge, Context context){
        this.id = id;
        this.areaID = areaID;
        this.subAreaID = subareaID;
        this.areaName = areaName;
        this.serviceCharge = serviceCharge;
        this.type = AreaType.list;
        this.latitude = Double.parseDouble(context.getResources().getString(R.string.default_restaurant_latitude));
        this.longitude = Double.parseDouble(context.getResources().getString(R.string.default_restaurant_longitude));
    }

    public BranchArea(int id, String areaName, double latitude, double longitude, List<LatLng> points) {
        this.id = id;
        this.areaName = areaName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.points = points;
        this.type = AreaType.map;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }

    public AreaType getType() {
        return type;
    }

    public void setType(AreaType type) {
        this.type = type;
    }

    public int getAreaID() {
        return areaID;
    }

    public void setAreaID(int areaID) {
        this.areaID = areaID;
    }

    public int getSubAreaID() {
        return subAreaID;
    }

    public void setSubAreaID(int subAreaID) {
        this.subAreaID = subAreaID;
    }

    public double getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(double serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    static void clearBranchArea(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().remove("General Area").apply();

    }

    static void setBranchArea(Context context, BranchArea branchArea) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = gson.toJson(branchArea);
        preferences.edit().putString("General Area", json).apply();
    }

    static BranchArea getSavedArea(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = preferences.getString("General Area", null);
        try {
            return gson.fromJson(json, BranchArea.class);
        }catch (Exception e){
            Log.e("", Objects.requireNonNull(e.getLocalizedMessage()));
            return null;
        }
    }



    @SuppressLint("CommitPrefEdits")
    static void setRestaunrantBranchInfo(Activity activity, Context context, JSONObject response, String areaName, double latitude, double longitude, List<LatLng> points){
        try {
            int branchID = response.getInt("brID");

            ((RestaurantApplication) activity.getApplication()).setLatitude(response.getDouble("latitude"));
            ((RestaurantApplication) activity.getApplication()).setLongitude(response.getDouble("longitude"));
            ((RestaurantApplication) activity.getApplication()).setStatus(response.getString("status"));
            ((RestaurantApplication) activity.getApplication()).setPhoneNumber(response.getString("phoneNumber"));

            // Setting working days
            HashMap<String, WorkDay> workingDays = new HashMap<>();
            Iterator<String> iter = response.getJSONObject("openingTimes").keys();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    JSONObject value = response.getJSONObject("openingTimes").getJSONObject(key);

                    int openingMinute = value.getInt("opiningMin");
                    int openingHour = value.getInt("opiningHour");
                    int closingMinute = value.getInt("closingMin");
                    int closingHour = value.getInt("closingHour");

                    workingDays.put(key, new WorkDay(openingMinute, openingHour, closingMinute, closingHour, context));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
            ((RestaurantApplication) activity.getApplication()).setWorkingDays(workingDays);

            // Saving branch area...
            BranchArea branchArea = new BranchArea(branchID, areaName, latitude, longitude, points);
            BranchArea.setBranchArea(context, branchArea);

        } catch (JSONException e) {
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    @SuppressLint("CommitPrefEdits")
    static void setRestaunrantBranchInfo(Activity activity, Context context, int branchID, int areaID, int subareaID, JSONObject response, String areaName, double serviceCharge){
        try {
            ((RestaurantApplication) activity.getApplication()).setLatitude(response.getDouble("latitude"));
            ((RestaurantApplication) activity.getApplication()).setLongitude(response.getDouble("longitude"));
            ((RestaurantApplication) activity.getApplication()).setStatus(response.getString("status"));
            ((RestaurantApplication) activity.getApplication()).setPhoneNumber(response.getString("phoneNumber"));

            // Setting working days
            HashMap<String, WorkDay> workingDays = new HashMap<>();
            Iterator<String> iter = response.getJSONObject("openingTimes").keys();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    JSONObject value = response.getJSONObject("openingTimes").getJSONObject(key);

                    int openingMinute = value.getInt("opiningMin");
                    int openingHour = value.getInt("opiningHour");
                    int closingMinute = value.getInt("closingMin");
                    int closingHour = value.getInt("closingHour");

                    workingDays.put(key, new WorkDay(openingMinute, openingHour, closingMinute, closingHour, context));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
            ((RestaurantApplication) activity.getApplication()).setWorkingDays(workingDays);

            // Saving branch area...
            BranchArea branchArea = new BranchArea(branchID, areaID, subareaID, areaName, serviceCharge, context);
            BranchArea.setBranchArea(context, branchArea);

        } catch (JSONException e) {
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

}
