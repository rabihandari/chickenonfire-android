package com.orderzzteam.chickenonfire.tools;

import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;
import com.orderzzteam.chickenonfire.AddOnCategory;
import com.orderzzteam.chickenonfire.AddOnItem;
import com.orderzzteam.chickenonfire.R;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class AddOnCategoryViewHolder extends GroupViewHolder {

    private TextView categoryName;
    private ImageView arrow;
    private TextView chooseText;
    private TextView optionalText;
    private TextView errorText;

    AddOnCategoryViewHolder(View itemView) {
        super(itemView);
        categoryName = itemView.findViewById(R.id.list_item_genre_name);
        arrow = itemView.findViewById(R.id.list_item_genre_arrow);
        chooseText = itemView.findViewById(R.id.list_item_choose_text);
        optionalText = itemView.findViewById(R.id.list_item_optional);
        errorText = itemView.findViewById(R.id.list_item_error);

    }

    void setGenreTitle(ExpandableGroup genre) {
        if (genre instanceof AddOnCategory) {
            categoryName.setText(genre.getTitle());
        }
        if (genre instanceof MultiCheckAddOnCategory) {
            categoryName.setText(genre.getTitle());
            chooseText.setText(((MultiCheckAddOnCategory) genre).getChooseText());
        }
        if (genre instanceof SingleCheckAddOnCategory) {
            categoryName.setText(genre.getTitle());
            chooseText.setText(((SingleCheckAddOnCategory) genre).getChooseText());
        }

        if (((AddOnItem)genre.getItems().get(0)).isOptional() == 1) {
            optionalText.setVisibility(View.VISIBLE);
        }

    }

    void showError(){
        errorText.setVisibility(View.VISIBLE);
    }

    void hideError(){
        errorText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void expand() {
        animateExpand();

    }

    @Override
    public void collapse() {
        animateCollapse();
    }

    private void animateExpand() {
        RotateAnimation rotate =
                new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.setAnimation(rotate);
    }

    private void animateCollapse() {
        RotateAnimation rotate =
                new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.setAnimation(rotate);
    }
}
