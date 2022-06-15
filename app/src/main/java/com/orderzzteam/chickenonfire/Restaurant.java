package com.orderzzteam.chickenonfire;

import java.util.ArrayList;

enum FilterCategory {
    freeDelivery,
    deliveryOffers,
    fiftyPercentOff,
    buyOneGetOneFree,
    happyHour,
    vegan,
    specialOffers,
    fastDelivery
}


class RestaurantFilter {
    private String background;
    private FilterCategory filterCategory;

    public RestaurantFilter(String background, FilterCategory filterCategory) {
        this.background = background;
        this.filterCategory = filterCategory;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public FilterCategory getFilterCategory() {
        return filterCategory;
    }

    public void setFilterCategory(FilterCategory filterCategory) {
        this.filterCategory = filterCategory;
    }
}


public class Restaurant {
    private int id;
    private String name;
    private String nameAr;
    private String tags;
    private String tagsAr;
    private String logo;
    private String cover;
    private double rating;
    private String deliveryTime;
    private String backendUrl;
    private ArrayList<FilterCategory> filterCategories;

    public Restaurant(int id, String name, String nameAr, String tags, String tagsAr, String logo, String cover, double rating, String deliveryTime, String backendUrl, ArrayList<FilterCategory> filterCategories) {
        this.id = id;
        this.name = name;
        this.nameAr = nameAr;
        this.tags = tags;
        this.tagsAr = tagsAr;
        this.logo = logo;
        this.cover = cover;
        this.rating = rating;
        this.deliveryTime = deliveryTime;
        this.backendUrl = backendUrl;
        this.filterCategories = filterCategories;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameAr() {
        return nameAr;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTagsAr() {
        return tagsAr;
    }

    public void setTagsAr(String tagsAr) {
        this.tagsAr = tagsAr;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    public void setBackendUrl(String backendUrl) {
        this.backendUrl = backendUrl;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public ArrayList<FilterCategory> getFilterCategories() {
        return filterCategories;
    }

    public void setFilterCategories(ArrayList<FilterCategory> filterCategories) {
        this.filterCategories = filterCategories;
    }

    public static class Tag {
        private String tagName;
        private int iconID;

        public Tag(String tagName, int iconID) {
            this.tagName = tagName;
            this.iconID = iconID;
        }

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        public int getIconID() {
            return iconID;
        }

        public void setIconID(int iconID) {
            this.iconID = iconID;
        }
    }
}
