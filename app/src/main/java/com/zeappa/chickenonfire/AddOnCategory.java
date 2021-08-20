package com.zeappa.chickenonfire;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class AddOnCategory extends ExpandableGroup<AddOnItem> {


    public AddOnCategory(String title, List<AddOnItem> items) {
        super(title, items);
    }


}