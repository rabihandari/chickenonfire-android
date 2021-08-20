package com.zeappa.chickenonfire;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReviewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ReviewsActivity reviewsActivity;
    private ArrayList<Review> reviews;

    private final int VIEW_TYPE_ITEM = 0;

    public ReviewsAdapter(){}

    ReviewsAdapter(Context context, ReviewsActivity reviewsActivity,ArrayList<Review> reviews){
        this.context = context;
        this.reviewsActivity = reviewsActivity;
        this.reviews = reviews;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reviews_item, parent, false);
            return new ItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reviews_show_more_item, parent, false);
            return new ShowMoreViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ItemViewHolder) {
            populateItemRows((ItemViewHolder) holder, position);
        } else if (holder instanceof ShowMoreViewHolder) {
            showLoadingView((ShowMoreViewHolder) holder, position);
        }

    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public int getItemViewType(int position) {
        int VIEW_TYPE_LOADING = 1;
        return reviews.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    private String getRatingText(double i, double j, double k, double l){

        double average = (i + j + k + l)/4;

        switch ((int) average){

            case 1:
                return context.getResources().getString(R.string.very_bad);
            case 2:
                return context.getResources().getString(R.string.bad);
            case 3:
                return context.getResources().getString(R.string.good);
            case 5:
                return context.getResources().getString(R.string.excellent);
                default:
                    return context.getResources().getString(R.string.amazing);
        }
    }

    private Drawable getRatingFace(double i, double j, double k, double l){

        double average = (i + j + k + l)/4;

        switch ((int) average){

            case 1:
                return context.getResources().getDrawable(R.drawable.very_bad_face_icon);
            case 2:
                return context.getResources().getDrawable(R.drawable.bad_face_icon);
            case 3:
                return context.getResources().getDrawable(R.drawable.good_face_icon);
            case 5:
                return context.getResources().getDrawable(R.drawable.excellent_face_icon);
            default:
                return context.getResources().getDrawable(R.drawable.amazing_face_icon);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        ImageView faceIcon;
        TextView rating;
        TextView date;
        TextView comment;
        TextView username;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            faceIcon = itemView.findViewById(R.id.review_item_face);
            rating = itemView.findViewById(R.id.review_rate_text);
            date = itemView.findViewById(R.id.review_date);
            comment = itemView.findViewById(R.id.review_comment);
            username = itemView.findViewById(R.id.review_username);

        }
    }

    private void populateItemRows(ItemViewHolder holder, int position) {

        holder.rating.setText(getRatingText(
                reviews.get(position).getOrderPackaginRating(),
                reviews.get(position).getValueForMoneyRating(),
                reviews.get(position).getDeliveryTimeRating(),
                reviews.get(position).getQualityOfFoodRating()));

        holder.date.setText(reviews.get(position).getDate());
        holder.comment.setText(reviews.get(position).getComment());
        holder.username.setText(reviews.get(position).getUserName());

        Glide.with(holder.itemView)
                .load(getRatingFace(
                        reviews.get(position).getOrderPackaginRating(),
                        reviews.get(position).getValueForMoneyRating(),
                        reviews.get(position).getDeliveryTimeRating(),
                        reviews.get(position).getQualityOfFoodRating()))
                .into(holder.faceIcon);

    }

    class ShowMoreViewHolder extends RecyclerView.ViewHolder{

        ProgressBar mProgressBar;
        TextView showMoreText;

        ShowMoreViewHolder(@NonNull View itemView) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.show_more_item_progressbar);
            showMoreText = itemView.findViewById(R.id.show_more_text);

        }
    }

    private void showLoadingView(final ShowMoreViewHolder viewHolder, int position) {

        viewHolder.showMoreText.setVisibility(View.VISIBLE);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                viewHolder.showMoreText.setVisibility(View.INVISIBLE);
                viewHolder.mProgressBar.setVisibility(View.VISIBLE);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        viewHolder.mProgressBar.setVisibility(View.INVISIBLE);
                        reviewsActivity.addMoreItems(ReviewsAdapter.this);

                    }
                }, 1000);
            }
        });

    }
}
