package com.zeappa.chickenonfire;

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

    UserAddress(String firstName, String lastName,String area, String addressType, String block,String street, String avenue, String house,String building, String office, int floor, int apartmentNo, String additionalDirections, LatLng latLng,int phoneCode, int phoneNumber) {
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
}
