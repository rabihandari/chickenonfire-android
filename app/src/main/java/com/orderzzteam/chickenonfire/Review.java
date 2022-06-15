package com.orderzzteam.chickenonfire;

public class Review {

    private String userName;
    private String date;
    private double orderPackaginRating;
    private double valueForMoneyRating;
    private double deliveryTimeRating;
    private double qualityOfFoodRating;
    private String comment;

    public Review(String userName,String date, double orderPackaginRating, double valueForMoneyRating, double deliveryTimeRating, double qualityOfFoodRating, String comment) {
        this.userName = userName;
        this.date = date;
        this.orderPackaginRating = orderPackaginRating;
        this.valueForMoneyRating = valueForMoneyRating;
        this.deliveryTimeRating = deliveryTimeRating;
        this.qualityOfFoodRating = qualityOfFoodRating;
        this.comment = comment;
    }

    public String getUserName() {
        return userName;
    }

    public String getDate() {
        return date;
    }

    public double getOrderPackaginRating() {
        return orderPackaginRating;
    }

    public double getValueForMoneyRating() {
        return valueForMoneyRating;
    }

    public double getDeliveryTimeRating() {
        return deliveryTimeRating;
    }

    public double getQualityOfFoodRating() {
        return qualityOfFoodRating;
    }

    public String getComment() {
        return comment;
    }
}
