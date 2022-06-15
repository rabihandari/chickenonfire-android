package com.orderzzteam.chickenonfire;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class MenuItem implements Parcelable {

    private int id;
    private String title;
    private String titleAr;
    private String description;
    private String descriptionAr;
    private String imageUrl;
    private double price;
    private double discount;
    private ArrayList<Flavour> flavours;

    public MenuItem(int id, String title, String titleAr, String description, String descriptionAr, String imageUrl, double price, double discount, ArrayList<Flavour> flavours) {
        this.id = id;
        this.title = title;
        this.titleAr = titleAr;
        this.description = description;
        this.descriptionAr = descriptionAr;
        this.imageUrl = imageUrl;
        this.price = price;
        this.discount = discount;
        this.flavours = flavours;
    }


    public ArrayList<Flavour> getFlavours() {
        return flavours;
    }

    public void setFlavours(ArrayList<Flavour> flavours) {
        this.flavours = flavours;
    }


    public String getTitleAr() {
        return titleAr;
    }

    public void setTitleAr(String titleAr) {
        this.titleAr = titleAr;
    }

    public String getDescriptionAr() {
        return descriptionAr;
    }

    public void setDescriptionAr(String descriptionAr) {
        this.descriptionAr = descriptionAr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(titleAr);
        parcel.writeString(description);
        parcel.writeString(descriptionAr);
        parcel.writeString(imageUrl);
        parcel.writeDouble(price);
        parcel.writeDouble(discount);
        parcel.writeTypedList(flavours);
    }

    public static final Creator<MenuItem> CREATOR = new Creator<MenuItem>() {
        @Override
        public MenuItem createFromParcel(Parcel in) {
            return new MenuItem(in);
        }

        @Override
        public MenuItem[] newArray(int size) {
            return new MenuItem[size];
        }
    };

    private MenuItem(Parcel in) {
        id = in.readInt();
        title = in.readString();
        titleAr = in.readString();
        description = in.readString();
        descriptionAr = in.readString();
        imageUrl = in.readString();
        price = in.readDouble();
        discount = in.readDouble();
        flavours = in.createTypedArrayList(Flavour.CREATOR);

    }




}

