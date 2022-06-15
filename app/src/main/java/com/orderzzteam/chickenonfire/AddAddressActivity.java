package com.orderzzteam.chickenonfire;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;
import com.zigis.materialtextfield.MaterialTextField;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;


public class AddAddressActivity extends AppCompatActivity {

    private LatLng selectedLatLng;
    private ImageView mapView;

    private int selectedAddressType = 0;
    private MaterialTextField firstName,lastName,area,addressType,block,street,avenue,house,building,floor,apartmentNo,office,additionalDirections;

    private int editingIndex = -1;

    CountryCodePicker ccp;
    EditText edtPhoneNumber;

    BranchArea branchArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        setLightStatusBar(this);

        if(!getConnectivity()){
            Intent intent = new Intent(this,NoInternetActivity.class);
            intent.putExtra("Activity","AddAddressActivity");
            startActivity(intent);
            finish();
        }

        firstName = findViewById(R.id.add_address_firstName);
        lastName = findViewById(R.id.add_address_lastName);
        area = findViewById(R.id.add_address_area);
        block = findViewById(R.id.add_address_block);
        street = findViewById(R.id.add_address_street);
        avenue = findViewById(R.id.add_address_avenue);
        house = findViewById(R.id.add_address_house);
        building = findViewById(R.id.add_address_bulding);
        office = findViewById(R.id.add_address_office);
        floor = findViewById(R.id.add_address_floor);
        apartmentNo = findViewById(R.id.add_address_apartmentno);
        additionalDirections = findViewById(R.id.add_address_additional_directions);
        ccp = findViewById(R.id.ccp);
        edtPhoneNumber = findViewById(R.id.add_address_phone);
        ccp.registerPhoneNumberTextView(edtPhoneNumber);

        branchArea = BranchArea.getSavedArea(this);
        if(branchArea != null) {
            selectedLatLng = new LatLng(branchArea.latitude, branchArea.longitude);
            if(!branchArea.getAreaName().isEmpty()){
                area.setText(branchArea.getAreaName());
            }
        }

        Account account = Account.getSavedAccount(this);
        if(account != null){
            firstName.setText(account.getFirstName());
            lastName.setText(account.getLastName());
//            edtPhoneNumber.setText(String.valueOf(account.getPhoneNumber()));
//            ccp.setCountryForPhoneCode(account.getPhoneCode());
        }


        Bundle extras = getIntent().getExtras();
        if(extras != null){
            selectedLatLng = new LatLng(extras.getDouble("Latitude"),extras.getDouble("Longitude"));
            editingIndex = extras.getInt("Editing Index", -1);
            if(editingIndex != -1)
                setUserAddress(editingIndex);
        }


        // Setting Map...
        mapView = findViewById(R.id.mapView2);
        if(branchArea.type == AreaType.map) {
            setMapImage();
        } else {
            Button refineLocationButton = findViewById(R.id.refine_location);
            refineLocationButton.setVisibility(View.GONE);
            mapView.setVisibility(View.GONE);
        }


        // Setting Address
        addressType = findViewById(R.id.add_address_addresstype);
        final Spinner addressTypeSpinner = findViewById(R.id.spinner);
        addressTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                selectedAddressType = i;
                addressType.setText(addressTypeSpinner.getSelectedItem().toString());

                switch (i){
                    case 0:
                        house.setVisibility(View.GONE);
                        building.setVisibility(View.VISIBLE);
                        apartmentNo.setVisibility(View.VISIBLE);
                        floor.setVisibility(View.VISIBLE);
                        office.setVisibility(View.GONE);
                        break;
                    case 1:
                        house.setVisibility(View.VISIBLE);
                        building.setVisibility(View.GONE);
                        apartmentNo.setVisibility(View.GONE);
                        floor.setVisibility(View.GONE);
                        office.setVisibility(View.GONE);
                        break;
                    case 2:
                        house.setVisibility(View.GONE);
                        building.setVisibility(View.VISIBLE);
                        apartmentNo.setVisibility(View.GONE);
                        floor.setVisibility(View.VISIBLE);
                        office.setVisibility(View.VISIBLE);
                        default:
                            break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    private void setMapImage() {
        String latitude = String.valueOf(selectedLatLng.latitude);
        String longitude = String.valueOf(selectedLatLng.longitude);
        String apiKey = getResources().getString(R.string.google_maps_key);
        String url = "http://maps.google.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=15&size=500x250&sensor=false&key=" + apiKey;
        Glide.with(this).load(url).into(mapView);

    }

    private void setUserAddress(int editingIndex) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString("User Address" + editingIndex , "");
        UserAddress userAddress = gson.fromJson(json, UserAddress.class);
        firstName.setText(userAddress.getFirstName());
        lastName.setText(userAddress.getLastName());
        area.setText(userAddress.getArea());
        block.setText(userAddress.getBlock());
        street.setText(userAddress.getStreet());
        avenue.setText(userAddress.getAvenue());
        office.setText(userAddress.getOffice());
        house.setText(userAddress.getHouse());
        building.setText(userAddress.getBuilding());
        floor.setText(String.valueOf(userAddress.getFloor()));
        apartmentNo.setText(String.valueOf(userAddress.getApartmentNo()));
        additionalDirections.setText(userAddress.getAdditionalDirections());
        ccp.setCountryForPhoneCode(userAddress.getPhoneCode());
        edtPhoneNumber.setText(String.valueOf(userAddress.getPhoneNumber()));


        if(selectedLatLng.longitude == 0 && selectedLatLng.latitude == 0)
            selectedLatLng = userAddress.getLatLng();

        // Setting the Address Type
        Spinner addressTypeSpinner = findViewById(R.id.spinner);
        String addressTypeString = userAddress.getAddressType();
        if(addressTypeString.equals(getResources().getStringArray(R.array.address_type_static)[0])){
            addressTypeSpinner.setSelection(0);
            floor.setText("");
            apartmentNo.setText("");
        }else if(addressTypeString.equals(getResources().getStringArray(R.array.address_type_static)[1])){
            addressTypeSpinner.setSelection(1);
        }else{
            addressTypeSpinner.setSelection(2);
            apartmentNo.setText("");
        }
    }


    private String getAddressType(int selectedIndex){
        String[] addressTypes = getResources().getStringArray(R.array.address_type_static);
        return addressTypes[selectedIndex];
    }

    private void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void GoBack(View view) {
        onBackPressed();
    }

    public void refineLocation(View view) {

        Intent intent = new Intent(this,GetLocationActivity.class);
        intent.putExtra("Editing Index", editingIndex);
        startActivity(intent);
        finish();
    }

    public void addAddress(View view) {

        if(!isValid())
            return;

        if(floor.getText().toString().isEmpty())
            floor.setText(String.valueOf(-1));

        if(apartmentNo.getText().toString().isEmpty())
            apartmentNo.setText(String.valueOf(-1));

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        UserAddress userAddress = new UserAddress(firstName.getText().toString().trim()
                ,lastName.getText().toString().trim()
                ,area.getText().toString().trim()
                ,getAddressType(selectedAddressType)
                ,block.getText().toString().trim()
                ,street.getText().toString().trim()
                ,avenue.getText().toString()
                ,house.getText().toString()
                ,building.getText().toString()
                ,office.getText().toString()
                ,Integer.parseInt(floor.getText().toString())
                ,Integer.parseInt(apartmentNo.getText().toString())
                ,additionalDirections.getText().toString()
                ,selectedLatLng
                ,ccp.getSelectedCountryCodeAsInt()
                ,Integer.parseInt(edtPhoneNumber.getText().toString())
                ,branchArea);

        SharedPreferences.Editor prefsEditor = preferences.edit();
        int count = preferences.getInt("User Addresses Count", 0);

        Gson gson = new Gson();
        String json = gson.toJson(userAddress);
        if(editingIndex == -1){
            prefsEditor.putString("User Address" + count, json);
            editingIndex = count;
            prefsEditor.putInt("User Addresses Count", count + 1);
        }
        else{
            prefsEditor.putString("User Address" + editingIndex, json);

        }
        prefsEditor.apply();

        Intent intent = new Intent(this,CheckoutActivity.class);
        intent.putExtra("User Address Index", editingIndex);
        startActivity(intent);
        finish();

    }

    private boolean isValid(){

        if(firstName.getText().toString().trim().isEmpty()){
            firstName.setError(getResources().getString(R.string.please_enter_this_field));
            return false;
        }
        if(lastName.getText().toString().trim().isEmpty()){
            lastName.setError(getResources().getString(R.string.please_enter_this_field));
            return false;
        }

        if(area.getText().toString().trim().isEmpty()){
            area.setError(getResources().getString(R.string.please_enter_this_field));
            return false;
        }
        if(block.getText().toString().trim().isEmpty()){
            block.setError(getResources().getString(R.string.please_enter_this_field));
            return false;
        }
        if(street.getText().toString().trim().isEmpty()){
            street.setError(getResources().getString(R.string.please_enter_this_field));
            return false;
        }

        // Apartment
        if(selectedAddressType == 0){
            if(floor.getText().toString().trim().isEmpty()){
                floor.setError(getResources().getString(R.string.please_enter_this_field));
                return false;
            }
            if(apartmentNo.getText().toString().trim().isEmpty()){
                apartmentNo.setError(getResources().getString(R.string.please_enter_this_field));
                return false;
            }
            if(building.getText().toString().trim().isEmpty()){
                building.setError(getResources().getString(R.string.please_enter_this_field));
                return false;
            }
        }

        // House
        if(selectedAddressType == 1){
            if(house.getText().toString().trim().isEmpty()){
                house.setError(getResources().getString(R.string.please_enter_this_field));
                return false;
            }
        }
        // Office
        if(selectedAddressType == 2){
            if(floor.getText().toString().trim().isEmpty()){
                floor.setError(getResources().getString(R.string.please_enter_this_field));
                return false;
            }
            if(office.getText().toString().trim().isEmpty()){
                office.setError(getResources().getString(R.string.please_enter_this_field));
                return false;
            }
            if(building.getText().toString().trim().isEmpty()){
                building.setError(getResources().getString(R.string.please_enter_this_field));
                return false;
            }
        }

        if(edtPhoneNumber.getText().toString().trim().isEmpty()){
            edtPhoneNumber.setError(getResources().getString(R.string.please_enter_this_field));
            return false;
        }

        return true;
    }

    private boolean getConnectivity(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
                Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }
}
