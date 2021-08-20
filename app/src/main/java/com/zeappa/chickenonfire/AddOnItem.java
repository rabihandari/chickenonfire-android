package com.zeappa.chickenonfire;

import android.os.Parcel;
import android.os.Parcelable;

public class AddOnItem implements Parcelable {

    private int aid;
    private String name;
    private String nameAr;
    private double price;
    private String type;
    private int isOptional;
    private int isMultiple;
    private String instruction;
    private int chooseMin;
    private int chooseMax;

    AddOnItem(int aid, String name,String nameAr, double price, String type, int isOptional, int isMultiple,String instruction,int chooseMin, int chooseMax) {
        this.aid = aid;
        this.name = name;
        this.nameAr = nameAr;
        this.price = price;
        this.type = type;
        this.isOptional = isOptional;
        this.isMultiple = isMultiple;
        this.instruction = instruction;
        this.chooseMin = chooseMin;
        this.chooseMax = chooseMax;
    }

    public String getNameAr() {
        return nameAr;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }


    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }
    public int getChooseMin() {
        return chooseMin;
    }

    public int getChooseMax() {
        return chooseMax;
    }

    public String getInstruction() {
        return instruction;
    }

    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid = aid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int isOptional() {
        return isOptional;
    }

    public void setOptional(int optional) {
        isOptional = optional;
    }

    public int isMultiple() {
        return isMultiple;
    }

    public void setMultiple(int multiple) {
        isMultiple = multiple;
    }

    protected AddOnItem(Parcel in) {
        aid = in.readInt();
        name = in.readString();
        nameAr = in.readString();
        price = in.readDouble();
        type = in.readString();
        instruction = in.readString();
        isOptional = in.readInt();
        isMultiple = in.readInt();
        chooseMin = in.readInt();
        chooseMax = in.readInt();
    }

    public static final Creator<AddOnItem> CREATOR = new Creator<AddOnItem>() {
        @Override
        public AddOnItem createFromParcel(Parcel in) {
            return new AddOnItem(in);
        }

        @Override
        public AddOnItem[] newArray(int size) {
            return new AddOnItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(aid);
        parcel.writeString(name);
        parcel.writeString(nameAr);
        parcel.writeDouble(price);
        parcel.writeString(type);
        parcel.writeString(instruction);
        parcel.writeInt(isOptional);
        parcel.writeInt(isMultiple);
        parcel.writeInt(chooseMin);
        parcel.writeInt(chooseMax);
    }


}


