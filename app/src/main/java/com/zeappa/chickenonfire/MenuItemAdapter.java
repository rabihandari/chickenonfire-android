package com.zeappa.chickenonfire;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MenuItem> menuItems;
    private ArrayList<MenuItem> allMenuItems;

    public MenuItemAdapter(){}

    MenuItemAdapter(Context context, ArrayList<MenuItem> menuItems){
        this.context = context;
        this.menuItems = menuItems;

        this.allMenuItems = new ArrayList<>();
        allMenuItems.addAll(menuItems);
    }

    ArrayList<MenuItem> getMenuItems() {
        return menuItems;
    }

    @NonNull
    @Override
    public MenuItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
        return new MenuItemAdapter.ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull MenuItemAdapter.ViewHolder holder, final int position) {

        if(getCurrentLanguage().equals("en")){
            holder.title.setText(menuItems.get(position).getTitle());
            holder.description.setText(menuItems.get(position).getDescription());
        }else{
            holder.title.setText(menuItems.get(position).getTitleAr());
            holder.description.setText(menuItems.get(position).getDescriptionAr());
        }

        holder.price.setText(context.getString(R.string.kd) + " " + String.format("%.3f", menuItems.get(position).getPrice()));

        String imageUrl = menuItems.get(position).getImageUrl();
        Glide.with(holder.itemView)
                .load(imageUrl)
                .placeholder(context.getResources().getDrawable(R.drawable.ic_default_menu_item, null))
                .into(holder.photo);

        if(position == menuItems.size() - 1)
            holder.divider.setVisibility(View.INVISIBLE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context,ItemOrderActivity.class);
                intent.putExtra("Menu Item",menuItems.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        });



        // Setting the discount...

        if(menuItems.get(position).getDiscount() > 0){
            holder.discountIcon.setVisibility(View.VISIBLE);
            holder.discountLine.setVisibility(View.VISIBLE);

            double lastPrice = menuItems.get(position).getPrice() - ((menuItems.get(position).getPrice() * menuItems.get(position).getDiscount()) / 100);
            holder.newPrice.setText(context.getString(R.string.kd) + " " + String.format("%.3f", lastPrice));

            holder.newPrice.setVisibility(View.VISIBLE);
        }

        // Setting containing icons (Chilli etc...)
        String itemContains = menuItems.get(position).getContains();
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(itemContains);
            for (int i = 0; i < jsonArray.length(); i++) {

                holder.containslayout.setVisibility(View.VISIBLE);

                String item = jsonArray.getString(i);
                switch (item){
                    case "Chilli":
                        holder.chilliIcon.setVisibility(View.VISIBLE);
                        break;
                    case "Garlic":
                        holder.garlicIcon.setVisibility(View.VISIBLE);
                        break;
                    case "Onion":
                        holder.onionIcon.setVisibility(View.VISIBLE);
                        break;
                    case "Vegetarian":
                        holder.vegiterianIcon.setVisibility(View.VISIBLE);
                        break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }




    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView description;
        TextView price;
        ImageView photo;
        ImageView divider;
        ConstraintLayout containslayout;
        ImageView chilliIcon, vegiterianIcon, onionIcon, garlicIcon;

        ImageView discountIcon,discountLine;
        TextView newPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.menu_item_title);
            description = itemView.findViewById(R.id.menu_item_description);
            price = itemView.findViewById(R.id.menu_item_price);
            photo = itemView.findViewById(R.id.menu_item_image);
            divider = itemView.findViewById(R.id.divider);
            chilliIcon = itemView.findViewById(R.id.menu_item_chilli_icon);
            vegiterianIcon = itemView.findViewById(R.id.menu_item_vegiterian_icon);
            onionIcon = itemView.findViewById(R.id.menu_item_onion_icon);
            garlicIcon = itemView.findViewById(R.id.menu_item_garlic_icon);
            containslayout =itemView.findViewById(R.id.menu_item_containing_layout);

            discountIcon = itemView.findViewById(R.id.menu_item_disction_icon);
            discountLine = itemView.findViewById(R.id.menu_item_discount_line);
            newPrice = itemView.findViewById(R.id.menu_item_new_price);

        }
    }

    void filter(String charText) {

        charText = charText.toLowerCase();
        menuItems.clear();
        if (charText.length() == 0) {
            menuItems.addAll(allMenuItems);
        } else {
            for (MenuItem menuItem : allMenuItems) {
                if (menuItem.getTitle().toLowerCase().contains(charText) || menuItem.getDescription().toLowerCase().contains(charText)
                        ||menuItem.getTitleAr().toLowerCase().contains(charText) || menuItem.getDescriptionAr().toLowerCase().contains(charText)) {
                    menuItems.add(menuItem);
                }
            }
        }

        notifyDataSetChanged();
    }


    private String getCurrentLanguage(){

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getString("language", "en");
    }



}
