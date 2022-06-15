package com.orderzzteam.chickenonfire;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

public class UserAddress {

    private String firstName;
    private String lastName;
    private String area;
    private String addressType;
    private String block;
    private String street;
    private String avenue;
    private String house;
    private String building;
    private String office;
    private int floor;
    private int apartmentNo;
    private String additionalDirections;
    private LatLng latLng;
    private int phoneCode;
    private int phoneNumber;
    private BranchArea branchArea;

    UserAddress(String firstName, String lastName,String area, String addressType, String block,String street, String avenue, String house,String building, String office, int floor, int apartmentNo, String additionalDirections, LatLng latLng,int phoneCode, int phoneNumber, BranchArea branchArea) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.area = area;
        this.addressType = addressType;
        this.block = block;
        this.street = street;
        this.avenue = avenue;
        this.house = house;
        this.building = building;
        this.office = office;
        this.floor = floor;
        this.apartmentNo = apartmentNo;
        this.additionalDirections = additionalDirections;
        this.latLng = latLng;
        this.phoneCode = phoneCode;
        this.phoneNumber = phoneNumber;
        this.branchArea = branchArea;
    }

    public String getHouse() {
        return house;
    }

    public String getOffice() {
        return office;
    }
    public String getFirstName() {
        return firstName;
    }


    public String getLastName() {
        return lastName;
    }

    public int getPhoneCode() {
        return phoneCode;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public String getArea() {
        return area;
    }

    public String getAddressType() {
        return addressType;
    }

    public String getBlock() {
        return block;
    }

    public String getStreet() {
        return street;
    }

    public String getAvenue() {
        return avenue;
    }

    public String getBuilding() {
        return building;
    }

    public int getFloor() {
        return floor;
    }

    public int getApartmentNo() {
        return apartmentNo;
    }

    public String getAdditionalDirections() {
        return additionalDirections;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public BranchArea getBranchArea() {
        return branchArea;
    }

    public void setBranchArea(BranchArea branchArea) {
        this.branchArea = branchArea;
    }


    static void clearAddresses(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int count = preferences.getInt("User Addresses Count", 0);

        for (int i = 0; i < count; i++){
            preferences.edit().remove("User Address" + i).apply();
        }
        preferences.edit().remove("User Addresses Count").apply();

    }

}
