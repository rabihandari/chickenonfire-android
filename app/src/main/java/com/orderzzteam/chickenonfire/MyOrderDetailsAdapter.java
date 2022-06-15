package com.orderzzteam.chickenonfire;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyOrderDetailsAdapter extends RecyclerView.Adapter<MyOrderDetailsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<BasketItem> basketItems;

    MyOrderDetailsAdapter(Context context, ArrayList<BasketItem> basketItems) {
        this.context = context;
        this.basketItems = basketItems;
    }

    @NonNull
    @Override
    public MyOrderDetailsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_order_details_item, parent, false);
        return new MyOrderDetailsAdapter.ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull MyOrderDetailsAdapter.ViewHolder holder, int position) {
        String name = getCurrentLanguage().equals("en") ? basketItems.get(position).getItemName() : basketItems.get(position).getItemNameAr();
        if (basketItems.get(position).getQuantity() > 1) {
            holder.name.setText(basketItems.get(position).getQuantity() + " x " + name);
        } else {
            holder.name.setText(name);
        }
        holder.price.setText((String.format("%.3f", basketItems.get(position).getTotalPrice()) + " " + context.getResources().getString(R.string.kd)));

        ArrayList<String> addonNames = getCurrentLanguage().equals("en") ? basketItems.get(position).getAddOnsNames() : basketItems.get(position).getAddOnsNamesAr();
        StringBuilder resultAddons = new StringBuilder();
        for (int i = 0; i < addonNames.size(); i++) {
            if (i == 2){
                resultAddons.append(addonNames.get(i));
                break;
            }
            resultAddons.append(addonNames.get(i)).append("\n");
        }
        holder.desc.setText(resultAddons);

        Glide.with(holder.itemView).load(basketItems.get(position).getImageUrl()).placeholder(context.getResources().getDrawable(R.drawable.ic_default_menu_item, null)).override(500,500).into(holder.image);

    }

    @Override
    public int getItemCount() {
        return basketItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView desc;
        TextView price;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.my_order_details_item_name);
            desc = itemView.findViewById(R.id.my_order_details_item_desc);
            price = itemView.findViewById(R.id.my_order_details_item_price);
            image = itemView.findViewById(R.id.my_order_details_item_image);
        }
    }


    private String getCurrentLanguage(){
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getString("language", "en");
    }
}
