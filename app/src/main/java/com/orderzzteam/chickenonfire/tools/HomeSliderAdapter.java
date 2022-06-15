package com.orderzzteam.chickenonfire.tools;


import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.orderzzteam.chickenonfire.RestaurantApplication;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.orderzzteam.chickenonfire.R;

import java.util.ArrayList;
import java.util.List;

public class HomeSliderAdapter extends SliderViewAdapter<HomeSliderAdapter.SliderAdapterVH> {
    private Application application;
    private List<String> imageUrls;
    private Context context;

    public HomeSliderAdapter(Application application, Context context, List<String> imageUrls) {
        this.application = application;
        this.imageUrls = imageUrls;
        this.context = context;

        if (imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
        return new SliderAdapterVH(view);
    }

    @Override
    public void onBindViewHolder(final SliderAdapterVH viewHolder, final int position) {
        String backendUrl = ((RestaurantApplication) application).getBackendUrl();
        Glide.with(viewHolder.itemView)
                .load(backendUrl + "static/media/" + imageUrls.get(position))
                .centerCrop()
                .dontAnimate()
                .into(viewHolder.imageViewBackground);


    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;

        SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.image_slider);
            this.itemView = itemView;
        }
    }
}