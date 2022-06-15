package com.orderzzteam.chickenonfire;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

interface RestaurantsAdapterInterface {
    public void onRestaurantSelected(Restaurant restaurant);
    public void listChanged(boolean isEmpty);
}

interface RestaurantsFilterAdapterInterface {
    public void didFilter(FilterCategory filterCategory);
    public void didClearFilter();
}

public class RestaurantsAdapter extends RecyclerView.Adapter<RestaurantsAdapter.ViewHolder>{

    Application application;
    Context context;
    ArrayList<Restaurant> restaurants;
    ArrayList<Restaurant> restaurantsFiltered;
    String filter;
    RestaurantsAdapterInterface adapterInterface;

    public RestaurantsAdapter(Application application, Context context, ArrayList<Restaurant> restaurants, String filter) {
        this.application = application;
        this.context = context;
        this.restaurants = restaurants;
        this.restaurantsFiltered = new ArrayList<>(restaurants);
        this.filter = filter;
    }

    @NonNull
    @Override
    public RestaurantsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item, parent, false);
        return new RestaurantsAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RestaurantsAdapter.ViewHolder holder, int position) {
        holder.name.setText(getCurrentLanguage().equals("en") ? restaurantsFiltered.get(position).getName() : restaurantsFiltered.get(position).getNameAr());
        holder.description.setText(getCurrentLanguage().equals("en") ? restaurantsFiltered.get(position).getTags() : restaurantsFiltered.get(position).getTagsAr());
        holder.rating.setText(String.valueOf(restaurantsFiltered.get(position).getRating()));
        holder.deliveryTime.setText(String.valueOf(restaurantsFiltered.get(position).getDeliveryTime()));
        Glide.with(holder.itemView).load(Uri.parse("file:///android_asset/" + restaurantsFiltered.get(position).getLogo())).into(holder.logo);
        Glide.with(holder.itemView).load(Uri.parse("file:///android_asset/" + restaurantsFiltered.get(position).getCover())).into(holder.cover);

        if (restaurantsFiltered.get(position).getFilterCategories().isEmpty()){
            holder.specialOfferLayout.setVisibility(View.GONE);
        } else {
            holder.specialOfferLayout.setVisibility(View.VISIBLE);
            if (filter == null){
                holder.specialOfferText.setText(convertFilterToString(restaurantsFiltered.get(position).getFilterCategories().get(0)));
            } else {
                holder.specialOfferText.setText(filter);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterInterface.onRestaurantSelected(restaurantsFiltered.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurantsFiltered.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView logo;
        ImageView cover;
        TextView name;
        TextView description;
        TextView rating;
        TextView deliveryTime;
        ConstraintLayout specialOfferLayout;
        TextView specialOfferText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            logo = itemView.findViewById(R.id.restaurant_item_logo);
            cover = itemView.findViewById(R.id.restaurant_item_cover);
            name = itemView.findViewById(R.id.restaurant_item_name);
            description = itemView.findViewById(R.id.restaurant_item_desc);
            rating = itemView.findViewById(R.id.restaurant_item_rating);
            deliveryTime = itemView.findViewById(R.id.restaurant_item_devliverytime);
            specialOfferLayout = itemView.findViewById(R.id.restaurant_item_special_offer_layout);
            specialOfferText = itemView.findViewById(R.id.restaurant_item_special_offer_text);
        }
    }



    private String getCurrentLanguage(){
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getString("language", "en");
    }

    public void setFilter(String keyword, String tag){
        restaurantsFiltered.clear();
        for (Restaurant restaurant: restaurants) {
            if (keyword.isEmpty() && tag.equals(context.getResources().getString(R.string.all))){
                restaurantsFiltered.addAll(restaurants);
                break;
            }

            boolean containsKeyword = restaurant.getName().toLowerCase().contains(keyword.toLowerCase()) || restaurant.getNameAr().toLowerCase().contains(keyword.toLowerCase())
                    || restaurant.getTags().toLowerCase().contains(keyword.toLowerCase()) || restaurant.getTagsAr().toLowerCase().contains(keyword.toLowerCase());
            boolean containsTag = restaurant.getTags().toLowerCase().contains(tag.toLowerCase()) || restaurant.getTagsAr().toLowerCase().contains(tag.toLowerCase()) || tag.equals(context.getResources().getString(R.string.all));
            if (containsKeyword && containsTag) {
                restaurantsFiltered.add(restaurant);
            }
        }
        notifyDataSetChanged();

        adapterInterface.listChanged(restaurantsFiltered.isEmpty());
    }

    public void resetFilters(){
        restaurantsFiltered.clear();
        restaurantsFiltered.addAll(restaurants);
        adapterInterface.listChanged(false);
        notifyDataSetChanged();
    }

    private String convertFilterToString(FilterCategory filterCategory){
        String[] stringArray = filterCategory.toString().split("(?=\\p{Upper})");

        StringBuilder sb = new StringBuilder();
        for (String s : stringArray) {
            s =  s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
            sb.append(s).append(" ");
        }
        return sb.toString().trim();
    }
}



class RestaurantsFilterAdapter extends RecyclerView.Adapter<RestaurantsFilterAdapter.ViewHolder>{

    Context context;
    ArrayList<RestaurantFilter> restaurantFilters;
    RestaurantsFilterAdapterInterface adapterInterface;

    public RestaurantsFilterAdapter(Context context, ArrayList<RestaurantFilter> restaurantFilters) {
        this.context = context;
        this.restaurantFilters = restaurantFilters;
    }

    @NonNull
    @Override
    public RestaurantsFilterAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_filter_item, parent, false);
        return new RestaurantsFilterAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantsFilterAdapter.ViewHolder holder, int position) {
        Glide.with(holder.itemView).load(Uri.parse("file:///android_asset/" + restaurantFilters.get(position).getBackground())).into(holder.background);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterInterface.didFilter(restaurantFilters.get(position).getFilterCategory());
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurantFilters.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView background;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            background = itemView.findViewById(R.id.restaurant_filter_item_image);
        }
    }
}