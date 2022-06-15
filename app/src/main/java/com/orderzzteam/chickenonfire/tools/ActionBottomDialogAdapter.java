package com.orderzzteam.chickenonfire.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.orderzzteam.chickenonfire.MenuCategory;
import com.orderzzteam.chickenonfire.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class ActionBottomDialogAdapter extends RecyclerView.Adapter<ActionBottomDialogAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MenuCategory> menuCategories;
    private RecyclerView menuRecyclerView;

    private RecyclerView.SmoothScroller smoothScroller;
    private BottomSheetDialogFragment bottomSheetDialogFragment;
    private TabLayout tabLayout;

    public ActionBottomDialogAdapter(){}


    public ActionBottomDialogAdapter(Context context, ArrayList<MenuCategory> menuCategories, RecyclerView menuRecyclerView, BottomSheetDialogFragment bottomSheetDialogFragment, TabLayout tabLayout){
        this.context = context;
        this.menuCategories = menuCategories;
        this.menuRecyclerView = menuRecyclerView;
        this.bottomSheetDialogFragment = bottomSheetDialogFragment;
        this.tabLayout = tabLayout;

        smoothScroller = new LinearSmoothScroller(context) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
    }

    @NonNull
    @Override
    public ActionBottomDialogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_menu_item, parent, false);
        return new ActionBottomDialogAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActionBottomDialogAdapter.ViewHolder holder, final int position) {

        holder.title.setText(menuCategories.get(position).getTitle());
        holder.count.setText(String.valueOf(menuCategories.get(position).getItemsCount()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smoothScroller.setTargetPosition(position);
                if (menuRecyclerView.getLayoutManager() == null)
                    return;
                bottomSheetDialogFragment.dismiss();
                tabLayout.getTabAt(position).select();
                menuRecyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuCategories.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView count;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.bottom_menu_item_title);
            count = itemView.findViewById(R.id.bottom_menu_item_count);
        }
    }
}
