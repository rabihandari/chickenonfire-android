package com.zeappa.chickenonfire;

import java.util.ArrayList;

public class MenuCategory {

    private String title;
    private int itemsCount;
    private ArrayList<MenuItem> menuItems;

    MenuCategory(String title, int itemsCount, ArrayList<MenuItem> menuItems) {
        this.title = title;
        this.itemsCount = itemsCount;
        this.menuItems = menuItems;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }

    ArrayList<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(ArrayList<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }
}
