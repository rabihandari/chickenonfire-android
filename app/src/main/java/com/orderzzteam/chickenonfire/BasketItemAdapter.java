package com.orderzzteam.chickenonfire;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BasketItemAdapter extends RecyclerView.Adapter<BasketItemAdapter.ViewHolder>  {

    private BasketActivity basketActivity;
    private Context context;
    private ArrayList<BasketItem> basketItems;
    private TextView subtotalText;
    private ArrayList<Integer> unavailableIDs;

    private SharedPreferences preferences;

    BasketItemAdapter(BasketActivity basketActivity, Context context, ArrayList<BasketItem> basketItems, TextView subtotalText, ArrayList<Integer> unavailableIDs) {
        this.basketActivity = basketActivity;
        this.context = context;
        this.basketItems = basketItems;
        this.subtotalText = subtotalText;
        this.unavailableIDs = unavailableIDs;

        preferences  = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @NonNull
    @Override
    public BasketItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.basket_item, parent, false);
        return new BasketItemAdapter.ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final BasketItemAdapter.ViewHolder holder, final int position) {

        if(getCurrentLanguage().equals("en")){
            holder.name.setText(basketItems.get(position).getItemName());
            String addOns = android.text.TextUtils.join(", ", basketItems.get(position).getAddOnsNames());
            holder.description.setText(addOns);
        }else{
            holder.name.setText(basketItems.get(position).getItemNameAr());
            String addOns = android.text.TextUtils.join(", ", basketItems.get(position).getAddOnsNamesAr());
            holder.description.setText(addOns);

        }
        final double price = basketItems.get(position).getTotalPrice();
        holder.price.setText(String.format("%.3f", price) + " " + context.getString(R.string.kd));
        final int[] quantity = {basketItems.get(position).getQuantity()};
        holder.quantity.setText(String.valueOf(quantity[0]));

        final double priceOfOne = price/quantity[0];

        Glide.with(holder.itemView).load(basketItems.get(position).getImageUrl()).placeholder(context.getResources().getDrawable(R.drawable.ic_default_menu_item, null)).override(500,500).into(holder.itemImage);

        boolean isChain = context.getResources().getBoolean(R.bool.chain);
        if (unavailableIDs.contains(basketItems.get(position).getItemID()) && !isChain){
            holder.unavailableItem.setVisibility(View.VISIBLE);
        }else{
            holder.unavailableItem.setVisibility(View.GONE);
        }

        holder.increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double newPrice = Double.parseDouble(subtotalText.getText().toString().split(" ")[0]) + (priceOfOne);
                quantity[0]++;
                holder.quantity.setText(String.valueOf(quantity[0]));
                subtotalText.setText(String.format("%.3f", newPrice) + " " + context.getString(R.string.kd));
                basketActivity.ValidateBasket(newPrice);
                holder.price.setText(String.format("%.3f", priceOfOne * quantity[0]) + " " + context.getString(R.string.kd));
                Gson gson = new Gson();
                basketItems.get(position).setQuantity(quantity[0]);
                basketItems.get(position).setTotalPrice(priceOfOne * quantity[0]);
                preferences.edit().putString("Basket Item" + position, gson.toJson(basketItems.get(position))).apply();

            }
        });

        holder.decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(quantity[0] == 1)
                    return;
                double newPrice = Double.parseDouble(subtotalText.getText().toString().split(" ")[0]) - (priceOfOne);
                quantity[0]--;
                holder.quantity.setText(String.valueOf(quantity[0]));
                subtotalText.setText(String.format("%.3f", newPrice) + " " + context.getString(R.string.kd));
                basketActivity.ValidateBasket(newPrice);
                holder.price.setText(String.format("%.3f", priceOfOne * quantity[0]) + " " + context.getString(R.string.kd));
                Gson gson = new Gson();
                basketItems.get(position).setQuantity(quantity[0]);
                basketItems.get(position).setTotalPrice(priceOfOne * quantity[0]);
                preferences.edit().putString("Basket Item" + position, gson.toJson(basketItems.get(position))).apply();
            }
        });

        holder.removeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteItem(holder.itemView,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return basketItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView description;
        private TextView price;
        private TextView quantity;
        private ImageView itemImage;

        private TextView increment,decrement;
        private TextView removeItem;
        private TextView unavailableItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.basket_item_name);
            description = itemView.findViewById(R.id.basket_item_desc);
            price = itemView.findViewById(R.id.basket_item_price);
            quantity = itemView.findViewById(R.id.basket_item_quantity);
            itemImage = itemView.findViewById(R.id.basket_item_image);

            increment = itemView.findViewById(R.id.basket_item_increment);
            decrement = itemView.findViewById(R.id.basket_item_decrement);
            removeItem = itemView.findViewById(R.id.basket_remove_item);
            unavailableItem = itemView.findViewById(R.id.basket_item_notavailable);

        }
    }

    private void deleteItem(final View rowView, final int position) {

        removeFromPrefs(position);

        Animation anim = AnimationUtils.loadAnimation(context,
                android.R.anim.slide_out_right);
        anim.setDuration(300);
        rowView.startAnimation(anim);

        new Handler().postDelayed(new Runnable() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            public void run() {

                double oldSubtotalValue = Double.parseDouble(subtotalText.getText().toString().split(" ")[0]);
                double newPrice = Double.parseDouble(((TextView)rowView.findViewById(R.id.basket_item_price)).getText().toString().split(" ")[0]);
                subtotalText.setText(String.format("%.3f", (oldSubtotalValue - newPrice)) + " " + context.getString(R.string.kd));

                basketItems.remove(position);
                notifyDataSetChanged(); //Refresh list

                basketActivity.ValidateBasket(oldSubtotalValue - newPrice);

                if(basketItems.size() == 0){
                    Intent intent = new Intent(context,HomeActivity.class);
                    context.startActivity(intent);
                }



            }

        }, anim.getDuration());
    }

    private void removeFromPrefs(int position){

        Gson gson = new Gson();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int newCount  = prefs.getInt("Basket Items Count", 0);
        String json = prefs.getString("Basket Item" + position, "");
        prefs.edit().remove(json).apply();
        for(int i = position; i < newCount; i++){
            if(i+1 == newCount){
                prefs.edit().remove(prefs.getString("Basket Item"+(i+1), "")).apply();
                prefs.edit().putInt("Basket Items Count", (newCount - 1)).apply();
                return;
            }
            if((i + 1) == basketItems.size())
                return;
            json = gson.toJson(basketItems.get(i+1));
            prefs.edit().putString("Basket Item" + i, json).apply();
        }

    }


    private String getCurrentLanguage(){

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getString("language", "en");
    }
}
