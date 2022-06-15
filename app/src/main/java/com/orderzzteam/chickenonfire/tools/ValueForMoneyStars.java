package com.orderzzteam.chickenonfire.tools;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.orderzzteam.chickenonfire.AddReviewActivity;
import com.orderzzteam.chickenonfire.R;

import java.util.Objects;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class ValueForMoneyStars extends Fragment implements View.OnClickListener {

    private ImageView star1,star2,star3,star4,star5;

    public ValueForMoneyStars() {
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stars, container, false);

        TextView ratingTitle = view.findViewById(R.id.star_rating_title);
        ratingTitle.setText( getResources().getString(R.string.how_would_you_rate_the) + " " + getResources().getString(R.string.value_for_money));

        star1 = view.findViewById(R.id.star1);
        star2 = view.findViewById(R.id.star2);
        star3 = view.findViewById(R.id.star3);
        star4 = view.findViewById(R.id.star4);
        star5 = view.findViewById(R.id.star5);

        star1.setOnClickListener(this);
        star2.setOnClickListener(this);
        star3.setOnClickListener(this);
        star4.setOnClickListener(this);
        star5.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        int accentColorID = ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorAccent);

        switch (v.getId()){

            case R.id.star1:
                star1.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star2.setColorFilter(null);
                star3.setColorFilter(null);
                star4.setColorFilter(null);
                star5.setColorFilter(null);
                ((AddReviewActivity) Objects.requireNonNull(getActivity())).setValueForMoney(1);
                break;
            case R.id.star2:
                star1.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star2.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star3.setColorFilter(null);
                star4.setColorFilter(null);
                star5.setColorFilter(null);
                ((AddReviewActivity) Objects.requireNonNull(getActivity())).setValueForMoney(2);
                break;
            case R.id.star3:
                star1.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star2.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star3.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star4.setColorFilter(null);
                star5.setColorFilter(null);
                ((AddReviewActivity) Objects.requireNonNull(getActivity())).setValueForMoney(3);
                break;
            case R.id.star4:
                star1.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star2.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star3.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star4.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star5.setColorFilter(null);
                ((AddReviewActivity) Objects.requireNonNull(getActivity())).setValueForMoney(4);
                break;
            case R.id.star5:
                star1.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star2.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star3.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star4.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                star5.setColorFilter(accentColorID, android.graphics.PorterDuff.Mode.MULTIPLY);
                ((AddReviewActivity) Objects.requireNonNull(getActivity())).setValueForMoney(5);
                break;

            default:
                ((AddReviewActivity) Objects.requireNonNull(getActivity())).setValueForMoney(1);
                break;

        }

        star1.setOnClickListener(null);
        star2.setOnClickListener(null);
        star3.setOnClickListener(null);
        star4.setOnClickListener(null);
        star5.setOnClickListener(null);

        CountDownTimer countDownTimer = new CountDownTimer(500,500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                ((AddReviewActivity) Objects.requireNonNull(getActivity())).SetPage(2);

            }
        };

        countDownTimer.start();

    }
}