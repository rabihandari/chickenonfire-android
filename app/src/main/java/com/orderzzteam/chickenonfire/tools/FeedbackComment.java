package com.orderzzteam.chickenonfire.tools;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.orderzzteam.chickenonfire.R;

import androidx.fragment.app.Fragment;

public class FeedbackComment extends Fragment {

    private EditText comment;

    public FeedbackComment() {
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback_comment, container, false);

        TextView ratingTitle = view.findViewById(R.id.face_rating_title);
        ratingTitle.setText(R.string.write_your_feedback);

        comment = view.findViewById(R.id.feedback_comment);

        Button mContinue = view.findViewById(R.id.faces_continue_button);
        mContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FeedbackBottomDialogFragment)getParentFragment()).setComment(comment.getText().toString());
                ((FeedbackBottomDialogFragment)getParentFragment()).setPage(3);

            }
        });

        ImageView backArrow = view.findViewById(R.id.faces_back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FeedbackBottomDialogFragment)getParentFragment()).setPage(1);
            }
        });

        return view;
    }

}
