package com.orderzzteam.chickenonfire.tools;

import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;
import com.orderzzteam.chickenonfire.AddOnItem;

import java.util.List;

public class MultiCheckAddOnCategory extends MultiCheckExpandableGroup {

    private String chooseText;

    public MultiCheckAddOnCategory(String title, List<AddOnItem> items,String chooseText) {
        super(title, items);
        this.chooseText = chooseText;
    }

    String getChooseText() {
        return chooseText;
    }


}