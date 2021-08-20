package com.zeappa.chickenonfire.tools;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.zeappa.chickenonfire.CheckoutActivity;
import com.zeappa.chickenonfire.HomeActivity;
import com.zeappa.chickenonfire.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cz.msebera.android.httpclient.client.HttpClient;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class VoucherBottomDialogFragment extends BottomSheetDialogFragment {

    private CheckoutActivity checkoutActivity;
    public final static String TAG = "VoucherBottomDialogFragment";

    public VoucherBottomDialogFragment(){}

    public VoucherBottomDialogFragment(CheckoutActivity checkoutActivity){
        this.checkoutActivity = checkoutActivity;
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
            public void onClick(View view) {

                final String voucher = voucherEditText.getText().toString();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .addHeader("Authorization",getResources().getString(R.string.backend_API_Key))
                        .url(getResources().getString(R.string.backendUrl) + "mobile-api/get-vouchers/")
                        .get()
                        .build();
                OkHttpClient httpClient = new OkHttpClient();
                httpClient.newCall(request)
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull final okhttp3.Response response) {
                                if (!response.isSuccessful()) {

                                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getContext(), "Error: " + response.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    });

                                } else {

                                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {

                                            try {

                                                String jsonData = response.body().string();
                                                JSONArray responses = new JSONArray(jsonData);

                                                for (int i = 0; i < responses.length(); i++){


                                                    String voucherID = ((JSONObject)responses.get(i)).getString("voucherID");
                                                    String discount = ((JSONObject)responses.get(i)).getString("discount");
                                                    double discountValue = Double.parseDouble(discount);

                                                    if(voucherID.equals(voucher)){
                                                        checkoutActivity.setVoucherDiscount(voucherID, discountValue);
                                                        dismiss();
                                                        return;
                                                    }
                                                }

                                                Toast.makeText(getContext(),getResources().getString( R.string.voucher_code_invalid),Toast.LENGTH_SHORT).show();


                                            } catch (IOException | JSONException e) {
                                                e.printStackTrace();
                                            }


                                        }
                                    });
                                }
                            }
                        });

            }
        });





    }



}