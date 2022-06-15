package com.orderzzteam.chickenonfire;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GeneralAreaList extends Activity {
    private Activity activity;
    private List<Area> areas, filteredAreas;

    GeneralAreaListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.general_area_list);
        activity = this;
        setLightStatusBar(this);


        areas = new ArrayList<>();
        filteredAreas = new ArrayList<>();

        // Getting areas...
        ProgressBar progressBar = findViewById(R.id.general_area_list_progressbar);
        progressBar.setVisibility(View.VISIBLE);

        final String backendUrl = getResources().getString(R.string.backendUrl);
        final String mJSONURLRequest = backendUrl + "mobile-api/get-areas";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                mJSONURLRequest,
                null,
                response -> {
                    try{
                        for(int i=0; i<response.length(); i++){
                            JSONObject areaResponse = response.getJSONObject(i);
                            int areaID = areaResponse.getInt("id");
                            String areaName = areaResponse.getString("nm");
                            String areaNameAr = areaResponse.getString("nmL");
                            int branch = areaResponse.getInt("branch");
                            JSONArray subareasResponse = areaResponse.getJSONArray("deliverysubarea_set");

                            // Setting Sub areas...
                            ArrayList<SubArea> subAreas = new ArrayList<>();
                            for(int j=0; j<subareasResponse.length(); j++){
                                JSONObject subareaResponse = subareasResponse.getJSONObject(j);
                                int subareaID = subareaResponse.getInt("id");
                                String subareaName = subareaResponse.getString("nm");
                                String subareaNameAr = subareaResponse.getString("nmL");
                                Double serviceFee = subareaResponse.getDouble("sf");

                                SubArea subArea = new SubArea(subareaID, subareaName, subareaNameAr, serviceFee);
                                subAreas.add(subArea);
                            }

                            Area area = new Area(areaID, branch, areaName, areaNameAr, subAreas);
                            areas.add(area);

                        }

                        filteredAreas.addAll(areas);

                        RecyclerView recyclerView = findViewById(R.id.areas_recyclerview);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        recyclerView.setHasFixedSize(true);
                        adapter = new GeneralAreaListAdapter(activity, getApplicationContext(), filteredAreas);
                        recyclerView.setAdapter(adapter);

                        progressBar.setVisibility(View.INVISIBLE);


                    }catch (JSONException e){
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
        );
        requestQueue.add(jsonArrayRequest);


        // Init search listener...
        EditText searchBox = findViewById(R.id.general_area_list_searchbox);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (areas.isEmpty() || adapter == null) return;

                String keyword = editable.toString();
                filteredAreas.clear();
                for (Area area: areas){
                    if (area.name.toLowerCase().contains(keyword.toLowerCase()) || area.nameAr.toLowerCase().contains(keyword.toLowerCase())){
                        filteredAreas.add(area);
                    } else {
                        ArrayList<SubArea> subAreas = new ArrayList<>();
                        for (SubArea subArea: area.subAreas){
                            if (subArea.name.toLowerCase().contains(keyword.toLowerCase()) || subArea.nameAr.toLowerCase().contains(keyword.toLowerCase())){
                                subAreas.add(subArea);
                            }
                        }
                        if (!subAreas.isEmpty()){
                            filteredAreas.add(new Area(area.id, area.branchID, area.name, area.nameAr, subAreas));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

    }


    public void GoBack(View view) {
        super.onBackPressed();
    }


    private void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    class Area {
        private int id;
        private int branchID;
        private String name;
        private String nameAr;
        private List<SubArea> subAreas;

        public Area(int id, int branchID, String name, String nameAr, List<SubArea> subAreas) {
            this.id = id;
            this.branchID = branchID;
            this.name = name;
            this.nameAr = nameAr;
            this.subAreas = subAreas;
        }

        public int getId() {
            return id;
        }

        int getBranchID() {
            return branchID;
        }

        public String getName() {
            return name;
        }

        public String getNameAr() {
            return nameAr;
        }

        public List<SubArea> getSubAreas() {
            return subAreas;
        }
    }


    class SubArea {
        private int id;
        private String name;
        private String nameAr;
        private Double serviceFee;

        public SubArea(int id, String name, String nameAr, Double serviceFee) {
            this.id = id;
            this.name = name;
            this.nameAr = nameAr;
            this.serviceFee = serviceFee;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getNameAr() {
            return nameAr;
        }

        public Double getServiceFee() {
            return serviceFee;
        }
    }
}
