package com.zeappa.chickenonfire.tools;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zeappa.chickenonfire.R;

import androidx.fragment.app.Fragment;

public class ThankYouFeedback extends Fragment {


    public ThankYouFeedback() {
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback_thankyou, container, false);

        TextView body = view.findViewById(R.id.feedback_thankyou_body);
        body.setText(getResources().getString(R.string.you_just_helped_us_make) + " " + getResources().getString(R.string.app_name) + " " +getResources().getString(R.string.even_better));

        Button mContinue = view.findViewById(R.id.faces_continue_button);
        mContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FeedbackBottomDialogFragment)getParentFragment()).submit();

            }
        });


        return view;
    }



}
