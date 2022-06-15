package com.orderzzteam.chickenonfire.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.thoughtbot.expandablecheckrecyclerview.ChildCheckController;
import com.thoughtbot.expandablecheckrecyclerview.listeners.OnCheckChildClickListener;
import com.thoughtbot.expandablecheckrecyclerview.listeners.OnChildCheckChangedListener;
import com.thoughtbot.expandablecheckrecyclerview.listeners.OnChildrenCheckStateChangedListener;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablerecyclerview.MultiTypeExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.listeners.ExpandCollapseListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.models.ExpandableListPosition;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.orderzzteam.chickenonfire.AddOnItem;
import com.orderzzteam.chickenonfire.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.LayoutInflater.from;

public class MultiTypeCheckAddOnCategoryAdapter extends MultiTypeExpandableRecyclerViewAdapter<AddOnCategoryViewHolder, ChildViewHolder>
        implements  OnChildrenCheckStateChangedListener, ExpandCollapseListener {

    private static final String CHECKED_STATE_MAP = "child_check_controller_checked_state_map";

    private static final int IS_NOT_MULTIPLE = 3;
    private static final int IS_MULTIPLE = 4;

    private ChildCheckController childCheckController;
    private OnCheckChildClickListener childClickListener;

    private Context context;
    private TextView totalPriceText;
    private TextView quantityText;
    private RecyclerView recyclerView;
    private NestedScrollView nestedScrollView;

    private LinkedHashMap<Integer, List<AddOnItem>> selectedAddOns;
    private HashMap<ExpandableGroup, AddOnCategoryViewHolder> categoriesHolders;
    private int categoryPosition = 0;

    public MultiTypeCheckAddOnCategoryAdapter(Context context,List<? extends ExpandableGroup> groups,TextView totalPriceText,TextView quantityText,RecyclerView recyclerView, NestedScrollView nestedScrollView) {
        super(groups);
        this.context = context;
        this.recyclerView =recyclerView;
        this.totalPriceText = totalPriceText;
        this.quantityText = quantityText;
        this.nestedScrollView = nestedScrollView;
        childCheckController = new ChildCheckController(expandableList, this);
        selectedAddOns = new LinkedHashMap<>();
        categoriesHolders = new HashMap<>();;

    }

    public LinkedHashMap<Integer, List<AddOnItem>> getSelectedAddOns() {
        return selectedAddOns;
    }


    @Override
    public AddOnCategoryViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = from(parent.getContext())
                .inflate(R.layout.list_item_add_on_category, parent, false);
        AddOnCategoryViewHolder addOnCategoryViewHolder = new AddOnCategoryViewHolder(view);
        return addOnCategoryViewHolder;
    }

    @Override
    public ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case IS_MULTIPLE:
                View view1 = from(parent.getContext()).inflate(R.layout.list_item_multicheck_add_on_item, parent, false);
                return new MultiCheckAddOnItemViewHolder(view1);
            case IS_NOT_MULTIPLE:
                View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_singlecheck_add_on_item, parent, false);
                return new SingleCheckAddOnItemViewHolder(view2);
            default:
                throw new IllegalArgumentException(viewType + " is an Invalid viewType");
        }
    }

    @Override
    public void onBindChildViewHolder(final ChildViewHolder holder, int flatPosition, final ExpandableGroup group,
                                      final int childIndex) {
        int viewType = getItemViewType(flatPosition);
        AddOnItem artist = (AddOnItem) group.getItems().get(childIndex);


        switch (viewType) {
            case IS_MULTIPLE:
                ExpandableListPosition listPosition1 = expandableList.getUnflattenedPosition(flatPosition);
                ((MultiCheckAddOnItemViewHolder) holder).onBindViewHolder(flatPosition, childCheckController.isChildChecked(listPosition1));
                if(getCurrentLanguage().equals("en")){
                    ((MultiCheckAddOnItemViewHolder) holder).setAddOnName(artist.getName());
                }else{
                    ((MultiCheckAddOnItemViewHolder) holder).setAddOnName(artist.getNameAr());
                }
                ((MultiCheckAddOnItemViewHolder) holder).setAddOnPrice(artist.getPrice());
                ((MultiCheckAddOnItemViewHolder) holder).setOnChildCheckedListener(new OnChildCheckChangedListener() {
                    @SuppressLint({"DefaultLocale", "SetTextI18n"})
                    @Override
                    public void onChildCheckChanged(View view, boolean checked, int flatPos) {

                        ExpandableListPosition listPos = expandableList.getUnflattenedPosition(flatPos);
                        AddOnItem addOnItem = (AddOnItem) group.getItems().get(childIndex);

                        if(selectedAddOns.get(addOnItem.getCid()) != null){
                            if(selectedAddOns.get(addOnItem.getCid()).size() == addOnItem.getChooseMax() && checked){
                                Toast.makeText(context,context.getString(R.string.you_cannot_choose_more_than) + " " + addOnItem.getChooseMax(),Toast.LENGTH_SHORT).show();
                                childCheckController.onChildCheckChanged(false, listPos);
                                if (childClickListener != null) {
                                    childClickListener.onCheckChildCLick(view, false, (CheckedExpandableGroup) expandableList.getExpandableGroup(listPos), listPos.childPos);
                                }
                                return;
                            }
                        }

                        childCheckController.onChildCheckChanged(checked, listPos);
                        if (childClickListener != null) {
                            childClickListener.onCheckChildCLick(view, checked, (CheckedExpandableGroup) expandableList.getExpandableGroup(listPos), listPos.childPos);
                        }

                        if(checked){
                            double totalPrice = Double.parseDouble(totalPriceText.getText().toString().split(" ")[0]) + (addOnItem.getPrice() * Integer.parseInt(quantityText.getText().toString()));
                            totalPriceText.setText(String.format("%.3f", totalPrice) + " " + context.getString(R.string.kd));

                            addToList(addOnItem.getCid(),addOnItem);
                        }
                        else{
                            double totalPrice = Double.parseDouble(totalPriceText.getText().toString().split(" ")[0]) - (addOnItem.getPrice() * Integer.parseInt(quantityText.getText().toString()));
                            totalPriceText.setText(String.format("%.3f", totalPrice) + " " + context.getString(R.string.kd));

                            List<AddOnItem> listItems = selectedAddOns.get(addOnItem.getCid());
                            for(int i = 0; i < listItems.size(); i++){
                                if(listItems.get(i).getAid() == addOnItem.getAid()){
                                    listItems.remove(i);
                                    selectedAddOns.put(addOnItem.getCid(), listItems);
                                    return;
                                }
                            }
                        }

                    }
                });
                break;
            case IS_NOT_MULTIPLE:
                ExpandableListPosition listPosition2 = expandableList.getUnflattenedPosition(flatPosition);
                ((SingleCheckAddOnItemViewHolder) holder).onBindViewHolder(flatPosition, childCheckController.isChildChecked(listPosition2));
                if(getCurrentLanguage().equals("en")){
                    ((SingleCheckAddOnItemViewHolder) holder).setAddOnName(artist.getName());
                }else{
                    ((SingleCheckAddOnItemViewHolder) holder).setAddOnName(artist.getNameAr());
                }
                ((SingleCheckAddOnItemViewHolder) holder).setAddOnPrice(artist.getPrice());
                ((SingleCheckAddOnItemViewHolder) holder).setOnChildCheckedListener(new OnChildCheckChangedListener() {
                    @SuppressLint({"DefaultLocale", "SetTextI18n"})
                    @Override
                    public void onChildCheckChanged(View view, boolean checked, int flatPos) {

                        ExpandableListPosition listPos = expandableList.getUnflattenedPosition(flatPos);
                        childCheckController.onChildCheckChanged(checked, listPos);
                        if (childClickListener != null) {
                            childClickListener.onCheckChildCLick(view, checked, (CheckedExpandableGroup) expandableList.getExpandableGroup(listPos), listPos.childPos);
                        }
                        AddOnItem addOnItem = (AddOnItem) group.getItems().get(childIndex);
                        if(checked){

                            double totalPrice = Double.parseDouble(totalPriceText.getText().toString().split(" ")[0]) + (addOnItem.getPrice() * Integer.parseInt(quantityText.getText().toString()));

                            if(selectedAddOns.get(addOnItem.getCid()) == null){
                                addToList(addOnItem.getCid(), addOnItem);
                            }else{

                                totalPrice -= selectedAddOns.get(addOnItem.getCid()).get(0).getPrice() * Integer.parseInt(quantityText.getText().toString());

                                List<AddOnItem> listItems = new ArrayList<>();
                                selectedAddOns.put(addOnItem.getCid(),listItems);
                                addToList(addOnItem.getCid(),addOnItem);
                            }

                            totalPriceText.setText(String.format("%.3f", totalPrice) + " " + context.getString(R.string.kd));

                        }else{
                            double totalPrice = Double.parseDouble(totalPriceText.getText().toString().split(" ")[0]) - (addOnItem.getPrice() * Integer.parseInt(quantityText.getText().toString()));
                            totalPriceText.setText(String.format("%.3f", totalPrice) + " " + context.getString(R.string.kd));
                        }

                        collapseGroup(group);

                    }
                });
        }


    }

    private synchronized void addToList(int mapKey, AddOnItem myItem) {
        List<AddOnItem> itemsList = selectedAddOns.get(mapKey);

        if(itemsList == null) {
            itemsList = new ArrayList<>();
            itemsList.add(myItem);
            selectedAddOns.put(mapKey, itemsList);
        } else {
            if(!itemsList.contains(myItem))
                itemsList.add(myItem);
        }
    }


    @Override
    public void onBindGroupViewHolder(final AddOnCategoryViewHolder holder, final int flatPosition,
                                      final ExpandableGroup group) {
        holder.setGenreTitle(group);
        holder.expand();
        recyclerView.post(new Runnable()
        {
            @Override
            public void run() {
                categoriesHolders.put(group,holder);
            }
        });



    }

    public void viewError(int missingAddOnID){
        Toast.makeText(context, context.getResources().getString(R.string.please_select_all_required_fields), Toast.LENGTH_SHORT).show();

//        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(context) {
//            @Override protected int getVerticalSnapPreference() {
//                return LinearSmoothScroller.SNAP_TO_START;
//            }
//        };
//
//
//        smoothScroller.setTargetPosition(1);
//        Objects.requireNonNull(recyclerView.getLayoutManager()).startSmoothScroll(smoothScroller);
//
//        for(ExpandableGroup group: categoriesHolders.keySet()){
//            categoriesHolders.get(group).hideError();
//
//            ArrayList<AddOnItem> addOnItems = new ArrayList<>(group.getItems());
//            for(AddOnItem item: addOnItems){
//                if(item.getAid() == missingAddOnID){
//                    nestedScrollView.setSmoothScrollingEnabled(true);
//                    categoriesHolders.get(group).showError();
//                    nestedScrollView.smoothScrollTo(0, categoriesHolders.get(group).itemView.getTop() + 620, 500);
//                }
//            }
//        }



    }


    @Override
    public void updateChildrenCheckState(int firstChildFlattenedIndex, int numChildren) {
        notifyItemRangeChanged(firstChildFlattenedIndex, numChildren);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(CHECKED_STATE_MAP, new ArrayList(expandableList.groups));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null || !savedInstanceState.containsKey(CHECKED_STATE_MAP)) {
            return;
        }
        expandableList.groups = savedInstanceState.getParcelableArrayList(CHECKED_STATE_MAP);
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    public boolean isChild(int viewType) {
        return viewType == IS_NOT_MULTIPLE || viewType == IS_MULTIPLE;
    }

    @Override
    public int getChildViewType(int position, ExpandableGroup group, int childIndex) {
        if (((AddOnItem) (group).getItems().get(childIndex)).isMultiple() == 1) {
            return IS_MULTIPLE;
        } else {
            return IS_NOT_MULTIPLE;
        }
    }


    public void expandAll(){

        for(ExpandableGroup group : getGroups()){
            toggleGroup(categoryPosition);
            categoryPosition += group.getItems().size() + 1;
        }
    }

    private void collapseGroup(ExpandableGroup group){
        toggleGroup(group);
        categoriesHolders.get(group).collapse();

    }



    private String getCurrentLanguage(){

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getString("language", "en");
    }
}