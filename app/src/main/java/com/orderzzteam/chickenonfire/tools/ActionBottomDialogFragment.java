package com.orderzzteam.chickenonfire.tools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.orderzzteam.chickenonfire.MenuCategory;
import com.orderzzteam.chickenonfire.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ActionBottomDialogFragment extends BottomSheetDialogFragment{

    public static final String TAG = "ActionBottomDialog";

    private ArrayList<MenuCategory> menuCategories;
    private RecyclerView menuRecyclerView;
    private TabLayout tabLayout;

    public ActionBottomDialogFragment(){}

    public ActionBottomDialogFragment(ArrayList<MenuCategory> menuCategories,RecyclerView menuRecyclerView, TabLayout tabLayout){
        this.menuCategories = menuCategories;
        this.menuRecyclerView = menuRecyclerView;
        this.tabLayout = tabLayout;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_menu, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.bottom_sheet_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        ActionBottomDialogAdapter actionBottomDialogAdapter = new ActionBottomDialogAdapter(getContext(),menuCategories,menuRecyclerView,this,tabLayout);
        recyclerView.setAdapter(actionBottomDialogAdapter);

        ImageView cancel = view.findViewById(R.id.bottom_sheet_close_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


    }



}