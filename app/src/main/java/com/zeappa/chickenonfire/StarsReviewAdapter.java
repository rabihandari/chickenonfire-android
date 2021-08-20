package com.zeappa.chickenonfire;

import com.zeappa.chickenonfire.tools.DeliveryTimeStars;
import com.zeappa.chickenonfire.tools.OrderPackagingStars;
import com.zeappa.chickenonfire.tools.QualityOfFoodStars;
import com.zeappa.chickenonfire.tools.ValueForMoneyStars;

import org.jetbrains.annotations.NotNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class StarsReviewAdapter extends FragmentStatePagerAdapter {


    public StarsReviewAdapter(FragmentManager fm) {
        super(fm);

    }

    @NotNull
    @Override
    public Fragment getItem(int i) {

        switch (i) {
            case 0:
                return new OrderPackagingStars();
            case 1:
                return new ValueForMoneyStars();
            case 2:
                return new DeliveryTimeStars();
            case 3:
                return new QualityOfFoodStars();
        }

        return null;

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 4; //No of Tabs
    }

}