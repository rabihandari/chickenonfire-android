package com.orderzzteam.chickenonfire.tools;

import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class FacesFeedbackAdapter extends FragmentStatePagerAdapter {

    private int mCurrentPosition = -1;
    private List<Fragment> fragments;

    public FacesFeedbackAdapter(FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<Fragment>();
        fragments.add(new OrderFeedbackFaces());
        fragments.add(new DeliveryFeedbackFaces());
        fragments.add(new FeedbackComment());
        fragments.add(new ThankYouFeedback());

    }


    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (position != mCurrentPosition) {
            Fragment fragment = (Fragment) object;
            CustomViewPager pager = (CustomViewPager) container;
            if (fragment != null && fragment.getView() != null) {
                mCurrentPosition = position;
                pager.measureCurrentView(fragment.getView());
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

}