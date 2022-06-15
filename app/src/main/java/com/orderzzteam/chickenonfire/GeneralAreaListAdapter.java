package com.orderzzteam.chickenonfire;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GeneralAreaListAdapter extends RecyclerView.Adapter<GeneralAreaListAdapter.ViewHolder>  {

    private Activity activity;
    private Context context;
    private List<GeneralAreaList.Area> areas;

    public GeneralAreaListAdapter() {}

    GeneralAreaListAdapter(Activity activity, Context context, List<GeneralAreaList.Area> areas) {
        this.activity = activity;
        this.context = context;
        this.areas = areas;
    }

    @NonNull
    @Override
    public GeneralAreaListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.area_list_item, parent, false);
        return new GeneralAreaListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GeneralAreaListAdapter.ViewHolder holder, int position) {
        String areaName = getCurrentLanguage().equals("en") ? areas.get(position).getName() : areas.get(position).getNameAr();
        holder.name.setText(areaName);

        GeneralSubAreaListAdapter adapter = new GeneralSubAreaListAdapter(activity, context, areas.get(position), areas.get(position).getSubAreas());
        holder.subareasRecyclerView.setAdapter(adapter);

    }

    @Override
    public int getItemCount() {
        return areas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private RecyclerView subareasRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.area_name);
            subareasRecyclerView = itemView.findViewById(R.id.subareas_recyclerview);
            subareasRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            subareasRecyclerView.setHasFixedSize(true);

        }
    }

    class GeneralSubAreaListAdapter extends RecyclerView.Adapter<GeneralSubAreaListAdapter.ViewHolder> {

        private Activity activity;
        private Context context;
        private GeneralAreaList.Area area;
        private List<GeneralAreaList.SubArea> subAreas;

        public GeneralSubAreaListAdapter() {}

        GeneralSubAreaListAdapter(Activity activity, Context context, GeneralAreaList.Area area, List<GeneralAreaList.SubArea> subAreas) {
            this.activity = activity;
            this.context = context;
            this.area = area;
            this.subAreas = subAreas;
        }


        @NonNull
        @Override
        public GeneralSubAreaListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subarea_list_item, parent, false);
            return new GeneralSubAreaListAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GeneralSubAreaListAdapter.ViewHolder holder, int position) {
            String subAreaName = getCurrentLanguage().equals("en") ? subAreas.get(position).getName() : subAreas.get(position).getNameAr();
            holder.name.setText(subAreaName);

            holder.itemView.setOnClickListener(view -> {
                GeneralAreaList.SubArea subArea = subAreas.get(position);
                String fullAreaName = getCurrentLanguage().equals("en") ? area.getName() + ", " + subArea.getName() : area.getNameAr() + ", " + subArea.getNameAr();

                boolean isChain = context.getResources().getBoolean(R.bool.chain);
                Intent intent;
                if (isChain) {
                    intent = new Intent(activity, SelectRestaurant.class);
                } else {
                    intent = new Intent(activity, GeneralArea.class);
                }

                intent.putExtra("Branch ID", area.getBranchID());
                intent.putExtra("Area ID", area.getId());
                intent.putExtra("SubArea ID", subArea.getId());
                intent.putExtra("Area Name", fullAreaName);
                intent.putExtra("Type", AreaType.list);
                intent.putExtra("Service Charge", subArea.getServiceFee());
                activity.startActivity(intent);
                activity.finish();
            });
        }

        @Override
        public int getItemCount() {
            return subAreas.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView name;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                name = itemView.findViewById(R.id.subarea_name);
            }
        }
    }

    private String getCurrentLanguage(){
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getString("language", "en");
    }
}

