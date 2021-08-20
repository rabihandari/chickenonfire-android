package com.zeappa.chickenonfire;

public class Account {

    private String firstName;
    private String lastName;
    private String emailAddress;
    private String password;
    private int phoneCode;
    private int phoneNumber;
    private int loginStatus;
    private String method;

    public Account(String firstName, String lastName, String emailAddress,String password, int phoneCode, int phoneNumber,int loginStatus,String method) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.phoneCode = phoneCode;
        this.phoneNumber = phoneNumber;
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

    public int getPhoneCode() {
        return phoneCode;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setLoginStatus(int loginStatus) {
        this.loginStatus = loginStatus;
    }
}
