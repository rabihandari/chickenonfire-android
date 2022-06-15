package com.orderzzteam.chickenonfire.tools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.orderzzteam.chickenonfire.CheckoutActivity;
import com.orderzzteam.chickenonfire.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class VoucherBottomDialogFragment extends BottomSheetDialogFragment {

    private CheckoutActivity checkoutActivity;
    private double subtotal;
    public final static String TAG = "VoucherBottomDialogFragment";

    public VoucherBottomDialogFragment(){}

    public VoucherBottomDialogFragment(CheckoutActivity checkoutActivity, double subtotal){
        this.checkoutActivity = checkoutActivity;
        this.subtotal = subtotal;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_voucher, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText voucherEditText = view.findViewById(R.id.voucher_editText);
        Button useVoucherButton = view.findViewById(R.id.use_voucher_button);

        useVoucherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonView) {

                // Clear error...
                ClearError(view);

                String voucherID = voucherEditText.getText().toString();
                JSONObject body = new JSONObject();
                try {
                    body.put("code", voucherID);
                    body.put("subtotal", subtotal);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final String backendUrl = getResources().getString(R.string.backendUrl);
                final String mJSONURLRequest = backendUrl + "mobile-api/validate-voucher";
                RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(
                        com.android.volley.Request.Method.POST,
                        mJSONURLRequest,
                        body,
                        response -> {
                            try {
                                int errorCode = response.getInt("error");
                                if (errorCode == -1) {
                                    JSONObject discountCard = response.getJSONObject("discountCard");
                                    double discount = discountCard.getDouble("discount_Value");

                                    checkoutActivity.setVoucherDiscount(voucherID, discount);
                                    dismiss();
                                } else {
                                    if (errorCode == 0){
                                        SetError(view, "Invalid voucher");
                                    } else if (errorCode == 1) {
                                        SetError(view, "Voucher expired");
                                    } else if (errorCode == 2) {
                                        double minimumOrder = response.getDouble("minimumOrder");
                                        SetError(view, "The minimum order for this voucher is" + " " + String.format("%.3f", minimumOrder) + " " + getString(R.string.kd));
                                    } else if (errorCode == 3) {
                                        SetError(view, "This voucher has been already used");
                                    } else {
                                        SetError(view, "Something went wrong");
                                    }
                                }


                            } catch (JSONException e) {
                                Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            Toast.makeText(getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Authorization", getResources().getString(R.string.backend_API_Key));
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };
                requestQueue.add(jsonArrayRequest);

            }
        });
    }



    private void ClearError(View view){
        final TextView errorText = view.findViewById(R.id.voucher_error_text);
        errorText.setText("");
        errorText.setVisibility(View.GONE);
    }

    private void SetError(View view, String error){
        final TextView errorText = view.findViewById(R.id.voucher_error_text);
        errorText.setText(error);
        errorText.setVisibility(View.VISIBLE);
    }


}