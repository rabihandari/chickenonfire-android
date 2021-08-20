package com.zeappa.chickenonfire.tools;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;
import com.zeappa.chickenonfire.R;

public class MultiCheckAddOnItemViewHolder extends CheckableChildViewHolder {

    private CheckedTextView childCheckedTextView;
    private TextView childItemName;
    private TextView childItemPrice;

    MultiCheckAddOnItemViewHolder(View itemView) {
        super(itemView);
        childCheckedTextView = itemView.findViewById(R.id.list_item_multicheck_artist_name);
        childItemName = itemView.findViewById(R.id.list_item_multi_check_name);
        childItemPrice = itemView.findViewById(R.id.list_item_multi_check_price);
    }

    @Override
    public Checkable getCheckable() {
        return childCheckedTextView;
    }

    void setAddOnName(String name){
        childItemName.setText(name);
    }

    @SuppressLint("DefaultLocale")
    void setAddOnPrice(double price){
        if(price == 0){
            childItemPrice.setVisibility(View.GONE);
        }else{
            childItemPrice.setText(String.format("%.3f", price));
        }
    }
}