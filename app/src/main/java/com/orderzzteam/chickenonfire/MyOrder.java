package com.orderzzteam.chickenonfire;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;

public class MyOrder implements Parcelable{
    private int id;
    private String status;
    private String date;
    private ArrayList<BasketItem> basketItems;
    private Double total;
    private String rejectionReason;
    private String cancellationReason;

    public MyOrder(int id, String status, String date, ArrayList<BasketItem> basketItems, Double total, String rejectionReason, String cancellationReason) {
        this.id = id;
        this.status = status;
        this.date = date;
        this.basketItems = basketItems;
        this.total = total;
        this.rejectionReason = rejectionReason;
        this.cancellationReason = cancellationReason;
    }

    protected MyOrder(Parcel in) {
        id = in.readInt();
        status = in.readString();
        date = in.readString();
        basketItems = in.createTypedArrayList(BasketItem.CREATOR);
        if (in.readByte() == 0) {
            total = null;
        } else {
            total = in.readDouble();
        }
        rejectionReason = in.readString();
        cancellationReason = in.readString();
    }

    public static final Creator<MyOrder> CREATOR = new Creator<MyOrder>() {
        @Override
        public MyOrder createFromParcel(Parcel in) {
            return new MyOrder(in);
        }

        @Override
        public MyOrder[] newArray(int size) {
            return new MyOrder[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<BasketItem> getBasketItems() {
        return basketItems;
    }

    public Double getTotal() {
        return total;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }


    public static ArrayList<MyOrder> getMyOrders(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        int count = preferences.getInt("My Orders Count", 0);
        ArrayList<MyOrder> myOrders = new ArrayList<>();
        for (int i = 0; i < count; i++){
            Gson gson = new Gson();
            String json = preferences.getString("My Order" + i, null);
            MyOrder myOrder = gson.fromJson(json, MyOrder.class);
            myOrders.add(myOrder);
        }

        return myOrders;

    }


    public static MyOrder getMyOrder(Context context, int orderID) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        int count = preferences.getInt("My Orders Count", 0);
        ArrayList<MyOrder> myOrders = new ArrayList<>();
        for (int i = 0; i < count; i++){
            Gson gson = new Gson();
            String json = preferences.getString("My Order" + i, null);
            MyOrder myOrder = gson.fromJson(json, MyOrder.class);
            if (myOrder.getId() == orderID){
                return myOrder;
            }
        }

        return null;

    }



    public static void UpdateOrder(Context context, int id, String status, String rejectionReason, String cancellationReason){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        int count = preferences.getInt("My Orders Count", 0);
        for (int i = 0; i < count; i++){
            Gson gson = new Gson();
            String json = preferences.getString("My Order" + i, null);
            MyOrder myOrder = gson.fromJson(json, MyOrder.class);

            if (myOrder.id == id) {
                myOrder.status = status;
                myOrder.rejectionReason = rejectionReason;
                myOrder.cancellationReason = cancellationReason;

                Gson gson2 = new Gson();
                String json2 = gson2.toJson(myOrder);
                preferences.edit().putString("My Order" + i, json2).apply();
            }
        }

    }


    static void ClearOrders(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        int count = preferences.getInt("My Orders Count", 0);
        for (int i = 0; i < count; i++){
            preferences.edit().remove("My Order" + i).apply();
        }

        preferences.edit().remove("My Orders Count").apply();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(status);
        parcel.writeString(date);
        parcel.writeTypedList(basketItems);
        if (total == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(total);
        }
        parcel.writeString(rejectionReason);
        parcel.writeString(cancellationReason);
    }
}