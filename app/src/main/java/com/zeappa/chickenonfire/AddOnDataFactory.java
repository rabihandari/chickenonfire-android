package com.zeappa.chickenonfire;

import android.content.Context;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.zeappa.chickenonfire.tools.MultiCheckAddOnCategory;
import com.zeappa.chickenonfire.tools.SingleCheckAddOnCategory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class AddOnDataFactory {

    private static LinkedHashMap<String, List<AddOnItem>> items;

    public static List<? extends ExpandableGroup> makeAddOns(Context context, ArrayList<AddOnItem> addOnItems) {

        List<ExpandableGroup> addOnsList = new ArrayList<>();
        ArrayList<AddOnItem> nonOptionalAddOnItems = new ArrayList<>();

        items = new LinkedHashMap<>();

        List<MultiCheckAddOnCategory> multiAddOnsList = new ArrayList<>();
        List<SingleCheckAddOnCategory> singleAddOnsList = new ArrayList<>();

        for(AddOnItem addOnItem : addOnItems){
            if(addOnItem.isOptional() == 0){
                addToList(addOnItem.getType(),addOnItem);
            }else{
                nonOptionalAddOnItems.add(addOnItem);
            }
        }

        for(AddOnItem addOnItem : nonOptionalAddOnItems)
            addToList(addOnItem.getType(),addOnItem);

        for (String key : items.keySet()) {
            if(items.get(key).get(0).isMultiple() == 1){
                multiAddOnsList.add(new MultiCheckAddOnCategory(key, items.get(key),items.get(key).get(0).getInstruction()));
            }else{
                singleAddOnsList.add(new SingleCheckAddOnCategory(key, items.get(key),items.get(key).get(0).getInstruction()));
            }
        }

        addOnsList.addAll(singleAddOnsList);
        addOnsList.addAll(multiAddOnsList);

        return addOnsList;

    }

    private static synchronized void addToList(String mapKey, AddOnItem myItem) {
        List<AddOnItem> itemsList = items.get(mapKey);

        if(itemsList == null) {
            itemsList = new ArrayList<>();
            itemsList.add(myItem);
            items.put(mapKey, itemsList);
        } else {
            if(!itemsList.contains(myItem))
                itemsList.add(myItem);
        }
    }

}
