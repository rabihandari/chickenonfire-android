package com.orderzzteam.chickenonfire;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserAddressesAdapter extends RecyclerView.Adapter<UserAddressesAdapter.ViewHolder> {

    private ArrayList<UserAddress> userAddresses;
    private Context context;
    private SelectAddressActivity selectAddressActivity;

    private BranchArea branchArea;

    public UserAddressesAdapter(){}

    UserAddressesAdapter(ArrayList<UserAddress> userAddresses, Context context, SelectAddressActivity selectAddressActivity) {
        this.userAddresses = userAddresses;
        this.context = context;
        this.selectAddressActivity = selectAddressActivity;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        Gson gson = new Gson();
        String json = preferences.getString("General Area", null);
        try {
            branchArea = gson.fromJson(json, BranchArea.class);
        }catch (Exception e){
            Log.e("", Objects.requireNonNull(e.getLocalizedMessage()));
        }
    }

    @NonNull
    @Override
    public UserAddressesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_address_item, parent, false);
        return new UserAddressesAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull UserAddressesAdapter.ViewHolder holder, final int position) {

        holder.area.setText(userAddresses.get(position).getArea());

        String blocketcString = context.getResources().getString(R.string.block) + " " + userAddresses.get(position).getBlock() + ", "
                + context.getResources().getString(R.string.street) + " " + userAddresses.get(position).getStreet() + ", ";

        if(!userAddresses.get(position).getHouse().isEmpty())
            blocketcString += context.getResources().getString(R.string.house) + " " + userAddresses.get(position).getHouse() + ", ";
        if(!userAddresses.get(position).getBuilding().isEmpty())
            blocketcString += context.getResources().getString(R.string.building) + " " + userAddresses.get(position).getBuilding() + ", ";
        if(userAddresses.get(position).getFloor() != -1)
            blocketcString += context.getResources().getString(R.string.floor) + " " + userAddresses.get(position).getFloor() + ", ";
        if(userAddresses.get(position).getApartmentNo() != -1)
            blocketcString += context.getResources().getString(R.string.apartment_no) + " " + userAddresses.get(position).getApartmentNo() + ", ";
        if(!userAddresses.get(position).getOffice().isEmpty())
            blocketcString += context.getResources().getString(R.string.office) + " " + userAddresses.get(position).getOffice() + ", ";

        blocketcString = blocketcString.trim();
        blocketcString = blocketcString.substring(0, blocketcString.length() - 1);

        holder.desc.setText(blocketcString);

        holder.mobile.setText(context.getString(R.string.mobile)+ ": +" + userAddresses.get(position).getPhoneCode() + " " + userAddresses.get(position).getPhoneNumber());

        if (userAddresses.get(position).getBranchArea().getId() == branchArea.getId()){

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectAddressActivity.SelectAddress(position);

                }
            });
        } else {
            holder.area.setAlpha(0.5f);
        }
    }

    @Override
    public int getItemCount() {
        return userAddresses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView area;
        TextView desc;
        TextView mobile;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            area = itemView.findViewById(R.id.user_address_item_area);
            desc = itemView.findViewById(R.id.user_address_item_desc);
            mobile = itemView.findViewById(R.id.user_address_item_mobile);

        }
    }
}
