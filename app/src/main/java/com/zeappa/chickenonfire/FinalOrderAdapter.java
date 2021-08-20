package com.zeappa.chickenonfire;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FinalOrderAdapter extends RecyclerView.Adapter<FinalOrderAdapter.ViewHolder> {

    private Context context;
    private ArrayList<BasketItem> basketItems;

    public FinalOrderAdapter(){}

    public FinalOrderAdapter(Context context, ArrayList<BasketItem> basketItems) {
        this.context = context;
        this.basketItems = basketItems;
    }

    @NonNull
    @Override
    public FinalOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.final_order_item, parent, false);
        return new FinalOrderAdapter.ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull FinalOrderAdapter.ViewHolder holder, int position) {

        String addOns;
        if(getCurrentLanguage().equals("en")){
            holder.name.setText(basketItems.get(position).getItemName());
            addOns = android.text.TextUtils.join(", ", basketItems.get(position).getAddOnsNames());
        }else{
            holder.name.setText(basketItems.get(position).getItemNameAr());
            addOns = android.text.TextUtils.join(", ", basketItems.get(position).getAddOnsNamesAr());
        }

        holder.description.setText(addOns);

        holder.quantity.setText(String.valueOf(basketItems.get(position).getQuantity()));
        final double price = basketItems.get(position).getTotalPrice();
        holder.price.setText(String.format("%.3f", price) + " " + context.getString(R.string.kd));
    }

    @Override
    public int getItemCount() {
        return basketItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView quantity;
        private TextView name;
        private TextView description;
        private TextView price;


        ViewHolder(@NonNull View itemView) {
            super(itemView);

            quantity = itemView.findViewById(R.id.final_item_quantity);
            name = itemView.findViewById(R.id.final_item_name);
            description = itemView.findViewById(R.id.final_item_desc);
            price = itemView.findViewById(R.id.final_item_price);
        }
    }


    private String getCurrentLanguage(){

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getString("language", "en");
    }
}
