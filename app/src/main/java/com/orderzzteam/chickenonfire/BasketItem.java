package com.orderzzteam.chickenonfire;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class BasketItem implements Parcelable {

    private int itemID;
    private ArrayList<AddOnItem> addons;
    private ArrayList<Integer> addOnsCategoryIDs;
    private String itemName;
    private String itemNameAr;
    private ArrayList<String> addOnsNames;
    private ArrayList<String> addOnsNamesAr;
    private String imageUrl;
    private int quantity;
    private String specialRequest;
    private double totalPrice;

    BasketItem(int itemID, ArrayList<AddOnItem> addons, ArrayList<Integer> addOnsCategoryIDs, String itemName,String itemNameAr, ArrayList<String> addOnsNames,ArrayList<String> addOnsNamesAr, String imageUrl, int quantity, String specialRequest, double totalPrice) {
        this.itemID = itemID;
        this.addons = addons;
        this.addOnsCategoryIDs = addOnsCategoryIDs;
        this.itemName = itemName;
        this.itemNameAr = itemNameAr;
        this.addOnsNames = addOnsNames;
        this.addOnsNamesAr = addOnsNamesAr;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.specialRequest = specialRequest;
        this.totalPrice = totalPrice;
    }

    protected BasketItem(Parcel in) {
        itemID = in.readInt();
        addons = in.createTypedArrayList(AddOnItem.CREATOR);
        itemName = in.readString();
        itemNameAr = in.readString();
        addOnsNames = in.createStringArrayList();
        addOnsNamesAr = in.createStringArrayList();
        imageUrl = in.readString();
        quantity = in.readInt();
        specialRequest = in.readString();
        totalPrice = in.readDouble();
    }

    public static final Creator<BasketItem> CREATOR = new Creator<BasketItem>() {
        @Override
        public BasketItem createFromParcel(Parcel in) {
            return new BasketItem(in);
        }

        @Override
        public BasketItem[] newArray(int size) {
            return new BasketItem[size];
        }
    };

    public ArrayList<Integer> getAddOnsCategoryIDs() {
        return addOnsCategoryIDs;
    }

    public void setAddOnsCategoryIDs(ArrayList<Integer> addOnsCategoryIDs) {
        this.addOnsCategoryIDs = addOnsCategoryIDs;
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

    public ArrayList<AddOnItem> getAddOns() {
        return addons;
    }

    public void setAddOns(ArrayList<AddOnItem> addOnsIDs) {
        this.addons = addOnsIDs;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(itemID);
        parcel.writeTypedList(addons);
        parcel.writeString(itemName);
        parcel.writeString(itemNameAr);
        parcel.writeStringList(addOnsNames);
        parcel.writeStringList(addOnsNamesAr);
        parcel.writeString(imageUrl);
        parcel.writeInt(quantity);
        parcel.writeString(specialRequest);
        parcel.writeDouble(totalPrice);
    }
}
