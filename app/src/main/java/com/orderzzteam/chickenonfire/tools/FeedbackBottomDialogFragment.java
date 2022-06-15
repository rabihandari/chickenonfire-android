package com.orderzzteam.chickenonfire.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.orderzzteam.chickenonfire.HomeActivity;
import com.orderzzteam.chickenonfire.R;
import com.orderzzteam.chickenonfire.RestaurantApplication;
import com.orderzzteam.chickenonfire.UserAddress;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FeedbackBottomDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";

    private Context context;
    private HomeActivity homeActivity;

    private CustomViewPager ratingPager;
    private int orderRating = 3;
    private int deliveryRating = 3;
    private String comment = "";


    public FeedbackBottomDialogFragment(){}

    public FeedbackBottomDialogFragment(Context context,HomeActivity homeActivity){
        this.homeActivity = homeActivity;
        this.context = context;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_feedback, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ratingPager = view.findViewById(R.id.feedback_viewpager);
        FacesFeedbackAdapter feedbackAdapter = new FacesFeedbackAdapter(getChildFragmentManager());
        ratingPager.setPagingEnabled(false);
        ratingPager.setAdapter(feedbackAdapter);

        ImageView cancel = view.findViewById(R.id.feedback_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }


    void setOrderRating(int orderRating){
        this.orderRating = orderRating;
    }

    void setDeliveryRating(int deliveryRating){
        this.deliveryRating = deliveryRating;
    }

    void setComment(String comment){
        this.comment = comment;
    }

    void setPage(int page){


        ratingPager.setCurrentItem(page);
    }

    void submit(){
        sendFeedback();
        homeActivity.setLastOrderAsRated();
        Toast.makeText(context,getResources().getString(R.string.thank_you_for_your_feedback), Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void sendFeedback() {

        String fromEmail = ((RestaurantApplication) getActivity().getApplication()).getGmailEmail();
        String fromPassword = ((RestaurantApplication) getActivity().getApplication()).getGmailPassword();
        String toEmails = ((RestaurantApplication) getActivity().getApplication()).getGmailEmail();
        List<String> toEmailList = Arrays.asList(toEmails
                .split("\\s*,\\s*"));

        String emailSubject = getResources().getString(R.string.customer_feedback);

        String emailBody = "<br><br><b>"+ getResources().getString(R.string.customer_name) + " " + "</b>" + getLastAddress().getFirstName() + " " + getLastAddress().getLastName()  + "<br>"
                + "<b>" + getResources().getString(R.string.customer_phone_number) + "</b>" + " +" + getLastAddress().getPhoneCode() + " " + getLastAddress().getPhoneNumber() + "<br><br>"
                + "<b>" + getResources().getString(R.string.order_rating) + "</b>" + " " + orderRating + "/5<br><b>" + getResources().getString(R.string.delivery_rating) + " " + "</b>" + deliveryRating + "/5" + "<br>"
                + "<b>" + getResources().getString(R.string.comment) + "</b>" + " " + comment;

        new SendMailTask(homeActivity).execute(fromEmail,
                fromPassword, toEmailList, emailSubject, emailBody);


    }

    private UserAddress getLastAddress() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Gson gson = new Gson();
        int count = preferences.getInt("User Addresses Count", 0);
        String json = preferences.getString("User Address" + (count -1) , "");

        return gson.fromJson(json, UserAddress.class);
    }


}