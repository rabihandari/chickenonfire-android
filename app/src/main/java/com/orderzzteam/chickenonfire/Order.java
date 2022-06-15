package com.orderzzteam.chickenonfire;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public class Order {

    private ArrayList<BasketItem> basketItems;
    private boolean rated;
    private boolean dismissed;

    Order(ArrayList<BasketItem> basketItems, boolean rated, boolean dismissed) {
        this.basketItems = new ArrayList<>();
        this.basketItems.addAll(basketItems);
        this.rated = rated;
        this.dismissed = dismissed;
    }

    public ArrayList<BasketItem> getBasketItems() {
        return basketItems;
    }

    public void setBasketItems(ArrayList<BasketItem> basketItems) {
        this.basketItems = basketItems;
    }

    public boolean isRated() {
        return rated;
    }

    public void setRated(boolean rated) {
        this.rated = rated;
    }

    public boolean isDismissed() {
        return dismissed;
    }

    public void setDismissed(boolean dismissed) {
        this.dismissed = dismissed;
    }



    static void clearLastOrder(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().remove("Last Order").apply();

    }

}

