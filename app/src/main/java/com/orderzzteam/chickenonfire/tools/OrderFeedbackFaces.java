package com.orderzzteam.chickenonfire.tools;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.orderzzteam.chickenonfire.R;

import androidx.fragment.app.Fragment;

public class OrderFeedbackFaces extends Fragment implements View.OnClickListener  {
    
    private ImageView face1,face2,face3,face4,face5;

    public OrderFeedbackFaces() {
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback_faces, container, false);

        TextView ratingTitle = view.findViewById(R.id.face_rating_title);
        ratingTitle.setText(getResources().getString(R.string.how_was_your_order_from) + " " + getResources().getString(R.string.app_name) + "?");

        face1 = view.findViewById(R.id.face1);
        face2 = view.findViewById(R.id.face2);
        face3 = view.findViewById(R.id.face3);
        face4 = view.findViewById(R.id.face4);
        face5 = view.findViewById(R.id.face5);

        face1.setOnClickListener(this);
        face2.setOnClickListener(this);
        face3.setOnClickListener(this);
        face4.setOnClickListener(this);
        face5.setOnClickListener(this);

        face3.performClick();

        Button mContinue = view.findViewById(R.id.faces_continue_button);
        mContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FeedbackBottomDialogFragment)getParentFragment()).setPage(1);

            }
        });

        ImageView backArrow = view.findViewById(R.id.faces_back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FeedbackBottomDialogFragment)getParentFragment()).dismiss();
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()){

            case R.id.face1:
                face1.setImageDrawable(getResources().getDrawable(R.drawable.very_bad_red_face_icon));
                face2.setImageDrawable(getResources().getDrawable(R.drawable.bad_face_icon));
                face3.setImageDrawable(getResources().getDrawable(R.drawable.good_face_icon));
                face4.setImageDrawable(getResources().getDrawable(R.drawable.amazing_face_icon));
                face5.setImageDrawable(getResources().getDrawable(R.drawable.excellent_face_icon));
                ((FeedbackBottomDialogFragment)getParentFragment()).setOrderRating(1);
                break;
            case R.id.face2:
                face1.setImageDrawable(getResources().getDrawable(R.drawable.very_bad_face_icon));
                face2.setImageDrawable(getResources().getDrawable(R.drawable.bad_red_face_icon));
                face3.setImageDrawable(getResources().getDrawable(R.drawable.good_face_icon));
                face4.setImageDrawable(getResources().getDrawable(R.drawable.amazing_face_icon));
                face5.setImageDrawable(getResources().getDrawable(R.drawable.excellent_face_icon));
                ((FeedbackBottomDialogFragment)getParentFragment()).setOrderRating(2);
                break;
            case R.id.face3:
                face1.setImageDrawable(getResources().getDrawable(R.drawable.very_bad_face_icon));
                face2.setImageDrawable(getResources().getDrawable(R.drawable.bad_face_icon));
                face3.setImageDrawable(getResources().getDrawable(R.drawable.good_red_face_icon));
                face4.setImageDrawable(getResources().getDrawable(R.drawable.amazing_face_icon));
                face5.setImageDrawable(getResources().getDrawable(R.drawable.excellent_face_icon));
                ((FeedbackBottomDialogFragment)getParentFragment()).setOrderRating(3);
                break;
            case R.id.face4:
                face1.setImageDrawable(getResources().getDrawable(R.drawable.very_bad_face_icon));
                face2.setImageDrawable(getResources().getDrawable(R.drawable.bad_face_icon));
                face3.setImageDrawable(getResources().getDrawable(R.drawable.good_face_icon));
                face4.setImageDrawable(getResources().getDrawable(R.drawable.amazing_red_face_icon));
                face5.setImageDrawable(getResources().getDrawable(R.drawable.excellent_face_icon));
                ((FeedbackBottomDialogFragment)getParentFragment()).setOrderRating(4);
                break;
            case R.id.face5:
                face1.setImageDrawable(getResources().getDrawable(R.drawable.very_bad_face_icon));
                face2.setImageDrawable(getResources().getDrawable(R.drawable.bad_face_icon));
                face3.setImageDrawable(getResources().getDrawable(R.drawable.good_face_icon));
                face4.setImageDrawable(getResources().getDrawable(R.drawable.amazing_face_icon));
                face5.setImageDrawable(getResources().getDrawable(R.drawable.excellent_red_face_icon));
                ((FeedbackBottomDialogFragment)getParentFragment()).setOrderRating(5);
                break;

            default:
                ((FeedbackBottomDialogFragment)getParentFragment()).setOrderRating(1);
                break;

        }


    }
}
