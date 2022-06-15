package com.orderzzteam.chickenonfire;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder>  {

    private Context context;
    private ArrayList<MyOrder> myOrders;

    public OrdersAdapter(Context context, ArrayList<MyOrder> myOrders) {
        this.context = context;
        this.myOrders = myOrders;
    }

    @NonNull
    @Override
    public OrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)  {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.myorder_item, parent, false);
        return new OrdersAdapter.ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull OrdersAdapter.ViewHolder holder, int position) {
        holder.orderID.setText(context.getResources().getString(R.string.order_id) + "" + myOrders.get(position).getId());
        holder.status.setText(getStatusLocalized(myOrders.get(position).getStatus()));
        holder.status.setTextColor(getStatusColor(myOrders.get(position).getStatus()));
        holder.date.setText(myOrders.get(position).getDate());
        holder.total.setText(context.getResources().getString(R.string.total_with_colun) + " " + String.format("%.3f", myOrders.get(position).getTotal()));

        if (!myOrders.get(position).getRejectionReason().isEmpty() || !myOrders.get(position).getCancellationReason().isEmpty()){
            holder.reason.setText(myOrders.get(position).getRejectionReason().isEmpty() ? myOrders.get(position).getCancellationReason() : myOrders.get(position).getRejectionReason());
            holder.reason.setVisibility(View.VISIBLE);
        } else {
            holder.reason.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MyOrderDetails.class);
                intent.putExtra("My Order", myOrders.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });


    }

    private int getStatusColor(String status) {
        switch (status) {
            case "PENDING":
                return context.getResources().getColor(R.color.orangeColor);
            case "ACCEPTED":
                return context.getResources().getColor(R.color.darkYellow);
            case "INDELIVERY":
                return context.getResources().getColor(R.color.lightBlue);
            case "REJECTED":
                return context.getResources().getColor(R.color.redColor);
            case "CANCELLED":
                return context.getResources().getColor(R.color.redColor);
                default:
                    return context.getResources().getColor(R.color.greenColor);
        }
    }




    private String getStatusLocalized(String status) {
        switch (status){
            case "PENDING":
                return context.getResources().getString(R.string.pending);
            case "INDELIVERY":
                return context.getResources().getString(R.string.indelivery);
            case "REJECTED":
                return context.getResources().getString(R.string.rejected);
            case "CANCELLED":
                return context.getResources().getString(R.string.cancelled);
            case "DELIVERED":
                return context.getResources().getString(R.string.delivered);
            default:
                return context.getResources().getString(R.string.accepted);
        }
    }


    @Override
    public int getItemCount() {
        return myOrders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderID;
        TextView status;
        TextView date;
        TextView total;
        TextView reason;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            orderID = itemView.findViewById(R.id.myorder_item_id);
            status = itemView.findViewById(R.id.myorder_item_status);
            date = itemView.findViewById(R.id.myorder_item_date);
            total = itemView.findViewById(R.id.myorder_item_total);
            reason = itemView.findViewById(R.id.myorder_item_reason);

        }
    }
}
