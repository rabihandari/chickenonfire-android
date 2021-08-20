package com.zeappa.chickenonfire;

import java.util.ArrayList;

public class BasketItem {

    private int itemID;
    private ArrayList<Integer> addOnsIDs;
    private String itemName;
    private String itemNameAr;
    private ArrayList<String> addOnsNames;
    private ArrayList<String> addOnsNamesAr;
    private String imageUrl;
    private int quantity;
    private String specialRequest;
    private double totalPrice;

    BasketItem(int itemID, ArrayList<Integer> addOnsIDs, String itemName,String itemNameAr, ArrayList<String> addOnsNames,ArrayList<String> addOnsNamesAr, String imageUrl, int quantity, String specialRequest, double totalPrice) {
        this.itemID = itemID;
        this.addOnsIDs = addOnsIDs;
        this.itemName = itemName;
        this.itemNameAr = itemNameAr;
        this.addOnsNames = addOnsNames;
        this.addOnsNamesAr = addOnsNamesAr;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.specialRequest = specialRequest;
        this.totalPrice = totalPrice;
    }

    public String getItemNameAr() {
        return itemNameAr;
    }

    public void setItemNameAr(String itemNameAr) {
        this.itemNameAr = itemNameAr;
    }

    public ArrayList<String> getAddOnsNamesAr() {
        return addOnsNamesAr;
    }

    public void setAddOnsNamesAr(ArrayList<String> addOnsNamesAr) {
        this.addOnsNamesAr = addOnsNamesAr;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public ArrayList<Integer> getAddOnsIDs() {
        return addOnsIDs;
    }

    public void setAddOnsIDs(ArrayList<Integer> addOnsIDs) {
        this.addOnsIDs = addOnsIDs;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public ArrayList<String> getAddOnsNames() {
        return addOnsNames;
    }

    public void setAddOnsNames(ArrayList<String> addOnsNames) {
        this.addOnsNames = addOnsNames;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSpecialRequest() {
        return specialRequest;
    }

    public void setSpecialRequest(String specialRequest) {
        this.specialRequest = specialRequest;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
