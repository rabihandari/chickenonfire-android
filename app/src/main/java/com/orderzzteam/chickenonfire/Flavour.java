package com.orderzzteam.chickenonfire;

import android.os.Parcel;
import android.os.Parcelable;

public class Flavour implements Parcelable {
    private String name;
    private String nameAr;
    private String imageUrl;

    public Flavour(String name, String nameAr, String imageUrl) {
        this.name = name;
        this.nameAr = nameAr;
        this.imageUrl = imageUrl;
    }

    protected Flavour(Parcel in) {
        name = in.readString();
        nameAr = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<Flavour> CREATOR = new Creator<Flavour>() {
        @Override
        public Flavour createFromParcel(Parcel in) {
            return new Flavour(in);
        }

        @Override
        public Flavour[] newArray(int size) {
            return new Flavour[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getNameAr() {
        return nameAr;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(nameAr);
        parcel.writeString(imageUrl);
    }

    public static Creator<Flavour> getCREATOR() {
        return CREATOR;
    }
}