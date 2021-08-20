package com.zeappa.chickenonfire;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder>{

    private Context context;
    private ArrayList<String> languageList;
    private HomeActivity mainActivity;

    LanguageAdapter(HomeActivity mainActivity, Context context, ArrayList<String> languageList){

        this.languageList = languageList;
        this.context = context;
        this.mainActivity = mainActivity;

    }

    @NonNull
    @Override
    public LanguageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.language_item,parent,false);
        context = parent.getContext();

        return new LanguageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageAdapter.ViewHolder holder, int position) {

        if(languageList.get(position).equals("English")){


            holder.SetName(languageList.get(position));
            holder.SetImage(ResourcesCompat.getDrawable(mainActivity.getResources(), R.drawable.america_flag, null));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    setLocale("en");


                }
            });

        }

        if(languageList.get(position).equals("Arabic")){


            holder.SetName(languageList.get(position));
            holder.SetImage(ResourcesCompat.getDrawable(mainActivity.getResources(), R.drawable.kuwait_flag, null));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    setLocale("ar");


                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return languageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView languageName;
        ImageView languageImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            languageName = itemView.findViewById(R.id.vendor_item_title);
            languageImage = itemView.findViewById(R.id.vendor_item_image);
        }

        void SetName(String name){

            languageName.setText(name);
        }

        void SetImage(Drawable drawable){

            languageImage.setImageDrawable(drawable);
        }
    }

    private void setLocale(String lang) {

        Resources resources = mainActivity.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(lang.toLowerCase()));
        resources.updateConfiguration(configuration, displayMetrics);
        configuration.locale = new Locale(lang.toLowerCase());
        resources.updateConfiguration(configuration, displayMetrics);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("language",lang);
        myEdit.apply();

        Intent refresh = new Intent(mainActivity, HomeActivity.class);
        mainActivity.finish();
        mainActivity.startActivity(refresh);
    }
}
