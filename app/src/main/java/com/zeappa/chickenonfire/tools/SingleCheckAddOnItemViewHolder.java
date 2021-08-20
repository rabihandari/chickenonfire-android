package com.zeappa.chickenonfire.tools;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;
import com.zeappa.chickenonfire.R;

public class SingleCheckAddOnItemViewHolder extends CheckableChildViewHolder {

    private CheckedTextView childCheckedTextView;
    private TextView childItemName;
    private TextView childItemPrice;

    public SingleCheckAddOnItemViewHolder(View itemView) {
        super(itemView);
        childCheckedTextView =
                (CheckedTextView) itemView.findViewById(R.id.list_item_singlecheck_artist_name);
        childItemName = itemView.findViewById(R.id.list_item_single_check_name);
        childItemPrice = itemView.findViewById(R.id.list_item_single_check_price);
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