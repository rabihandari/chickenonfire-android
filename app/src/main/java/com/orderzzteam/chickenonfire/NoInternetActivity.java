package com.orderzzteam.chickenonfire;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NoInternetActivity extends AppCompatActivity {

    Bundle extras;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_internet);
        setLightStatusBar(this);


    }

    public void retryConnecting(View view) {

        extras = getIntent().getExtras();
        if(extras == null)
            return;

        if(getConnectivity()){

            Intent intent;
            switch (Objects.requireNonNull(extras.getString("Activity"))){
                case "SplashScreen":
                    intent = new Intent(this,SplashScreen.class);
                    break;
                case "AddAddressActivity":
                    intent = new Intent(this,AddAddressActivity.class);
                    break;
                case "BasketActivity":
                    intent = new Intent(this,BasketItem.class);
                    break;
                case "CheckoutActivity":
                    intent = new Intent(this,CheckoutActivity.class);
                    break;
                case "GeneralArea":
                    intent = new Intent(this,GeneralArea.class);
                    break;
                case "GeneralAreaMap":
                    intent = new Intent(this,GeneralAreaMap.class);
                    break;
                case "GetLocationActivity":
                    intent = new Intent(this,GetLocationActivity.class);
                    break;
                case "ItemOrderActivity":
                    intent = new Intent(this,ItemOrderActivity.class);
                    break;
                case "RestaurantInfoActivity":
                    intent = new Intent(this,RestaurantInfoActivity.class);
                    break;
                case "SearchActivity":
                    intent = new Intent(this,SearchActivity.class);
                    break;
                case "SelectAddressActivity":
                    intent = new Intent(this,SelectAddressActivity.class);
                    break;
                case "SignInActivity":
                    intent = new Intent(this,SignInActivity.class);
                    break;
                case "SignInWithEmailActivity":
                    intent = new Intent(this,SignInWithEmailActivity.class);
                    break;
                case "ReviewsActivity":
                    intent = new Intent(this,ReviewsActivity.class);
                    break;
                case "CreateAccountActivity":
                    intent = new Intent(this,CreateAccountActivity.class);
                    break;
                case "FavouritesActivity":
                    intent = new Intent(this,FavouritesActivity.class);
                    break;
                case "AddReviewActivity":
                    intent = new Intent(this,AddReviewActivity.class);
                    break;
                    default:
                        intent = new Intent(this,HomeActivity.class);
                        break;

            }

            startActivity(intent);
            finish();

        }else
            animateSnail();

    }

    private void animateSnail(){
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake_anim);
        ImageView imgBell= findViewById(R.id.ic_no_interent);
        imgBell.startAnimation(shake);

    }

    private boolean getConnectivity(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }

    private void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}
