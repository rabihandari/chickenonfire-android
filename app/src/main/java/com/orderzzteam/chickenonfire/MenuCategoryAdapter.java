package com.orderzzteam.chickenonfire;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MenuCategoryAdapter extends RecyclerView.Adapter<MenuCategoryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MenuCategory> menuCategories;

    public MenuCategoryAdapter(){}


    MenuCategoryAdapter(Context context, ArrayList<MenuCategory> menuCategories){
        this.context = context;
        this.menuCategories = menuCategories;

    }

    @NonNull
    @Override
    public MenuCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_category_item, parent, false);
        return new MenuCategoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuCategoryAdapter.ViewHolder holder, int position) {

        holder.title.setText(menuCategories.get(position).getTitle());

        holder.items.setLayoutManager(new LinearLayoutManager(context));
        holder.items.setHasFixedSize(true);
        holder.items.setNestedScrollingEnabled(false);
        MenuItemAdapter menuItemAdapter = new MenuItemAdapter(context,menuCategories.get(position).getMenuItems());
        holder.items.setAdapter(menuItemAdapter);

    }

    @Override
    public int getItemCount() {
        return menuCategories.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

            TextView title;
            RecyclerView items;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                title = itemView.findViewById(R.id.category_item_title);
                items = itemView.findViewById(R.id.category_item_recyclerview);

            }
        }

}
