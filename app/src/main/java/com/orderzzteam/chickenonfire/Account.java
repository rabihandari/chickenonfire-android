package com.orderzzteam.chickenonfire;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Objects;

public class Account {

    private String firstName;
    private String lastName;
    private String emailAddress;
    private String password;
    private int loginStatus;
    private String method;

    public Account(String firstName, String lastName, String emailAddress,String password, int loginStatus,String method) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.loginStatus = loginStatus;
        this.method = method;
    }

    public int getLoginStatus() {
        return loginStatus;
    }

    public String getPassword() {
        return password;
    }

    public String getMethod() {
        return method;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setLoginStatus(int loginStatus) {
        this.loginStatus = loginStatus;
    }


    static Account getSavedAccount(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = preferences.getString("Account", null);
        try {
            Account account = gson.fromJson(json, Account.class);
            return account;
        }catch (Exception e){
            Log.e("", Objects.requireNonNull(e.getLocalizedMessage()));
            return null;
        }
    }

    static void setAccount(Context context, Account account) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = gson.toJson(account);
        preferences.edit().putString("Account", json).apply();
    }
}
