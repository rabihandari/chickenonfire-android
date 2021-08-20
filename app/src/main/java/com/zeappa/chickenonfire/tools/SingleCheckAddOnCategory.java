package com.zeappa.chickenonfire.tools;

import android.os.Parcel;

import com.thoughtbot.expandablecheckrecyclerview.models.SingleCheckExpandableGroup;

import java.util.List;

public class SingleCheckAddOnCategory extends SingleCheckExpandableGroup {


    private String chooseText;

    public SingleCheckAddOnCategory(String title, List items,String chooseText) {
        super(title, items);
        this.chooseText = chooseText;
    }

    String getChooseText() {
        return chooseText;
    }

    private SingleCheckAddOnCategory(Parcel in) {
        super(in);
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SingleCheckAddOnCategory> CREATOR = new Creator<SingleCheckAddOnCategory>() {
        @Override
        public SingleCheckAddOnCategory createFromParcel(Parcel in) {
            return new SingleCheckAddOnCategory(in);
        }

        @Override
        public SingleCheckAddOnCategory[] newArray(int size) {
            return new SingleCheckAddOnCategory[size];
        }
    };
}