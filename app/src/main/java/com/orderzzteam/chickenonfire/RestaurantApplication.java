package com.orderzzteam.chickenonfire;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import org.acra.ACRA;
import org.acra.annotation.AcraMailSender;

import java.util.HashMap;
import java.util.List;

@AcraMailSender(mailTo = "rabih.n.andari@outlook.com")
public class RestaurantApplication extends Application {
    private Activity activeActivity;

    private String appName, appNameAr;
    private String appDescription, appDescriptionAr;
    private String logo;
    private String headerImage, coverImage;
    private String digitalExperts;
    private String privacyPolicy;
    private boolean preOrder;
    private double minimumOrder;
    private double deliveryPrice;
    private int deliveryTime;
    private String phoneNumber;
    private Boolean enableCodAndSchedule;
    private Boolean enablePhoneVerification;
    private String facebook, instagram, twitter;
    private double latitude, longitude;
    private String gmailEmail, gmailPassword;
    private String twilioSecurityApiKey;
    private String bookeyMerchantID, bookeySubMerchantID, bookeySecretKey;
    private List<String> paymentMethods;
    private List<String> featuredItems;
    private String status;
    private boolean isMapEnabled;
    private HashMap<String, WorkDay> workingDays;

    // Restaurant Chain...
    private String backendUrl;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppNameAr() {
        return appNameAr;
    }

    public void setAppNameAr(String appNameAr) {
        this.appNameAr = appNameAr;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getAppDescription() {
        return appDescription;
    }

    public void setAppDescription(String appDescription) {
        this.appDescription = appDescription;
    }

    public String getAppDescriptionAr() {
        return appDescriptionAr;
    }

    public void setAppDescriptionAr(String appDescriptionAr) {
        this.appDescriptionAr = appDescriptionAr;
    }

    public String getHeaderImage() {
        return headerImage;
    }

    public void setHeaderImage(String headerImage) {
        this.headerImage = headerImage;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public boolean isPreOrder() {
        return preOrder;
    }

    public void setPreOrder(boolean preOrder) {
        this.preOrder = preOrder;
    }

    public double getMinimumOrder() {
        return minimumOrder;
    }

    public void setMinimumOrder(double minimumOrder) {
        this.minimumOrder = minimumOrder;
    }

    public double getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(double deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public int getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(int deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getGmailEmail() {
        return gmailEmail;
    }

    public void setGmailEmail(String gmailEmail) {
        this.gmailEmail = gmailEmail;
    }

    public String getGmailPassword() {
        return gmailPassword;
    }

    public void setGmailPassword(String gmailPassword) {
        this.gmailPassword = gmailPassword;
    }

    public String getTwilioSecurityApiKey() {
        return twilioSecurityApiKey;
    }

    public void setTwilioSecurityApiKey(String twilioSecurityApiKey) {
        this.twilioSecurityApiKey = twilioSecurityApiKey;
    }

    public String getBookeyMerchantID() {
        return bookeyMerchantID;
    }

    public void setBookeyMerchantID(String bookeyMerchantID) {
        this.bookeyMerchantID = bookeyMerchantID;
    }

    public String getBookeySubMerchantID() {
        return bookeySubMerchantID;
    }

    public void setBookeySubMerchantID(String bookeySubMerchantID) {
        this.bookeySubMerchantID = bookeySubMerchantID;
    }

    public String getBookeySecretKey() {
        return bookeySecretKey;
    }

    public void setBookeySecretKey(String bookeySecretKey) {
        this.bookeySecretKey = bookeySecretKey;
    }

    public List<String> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(List<String> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDigitalExperts() {
        return digitalExperts;
    }

    public void setDigitalExperts(String digitalExperts) {
        this.digitalExperts = digitalExperts;
    }

    public String getPrivacyPolicy() {
        return privacyPolicy;
    }

    public void setPrivacyPolicy(String privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
    }

    public HashMap<String, WorkDay> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(HashMap<String, WorkDay> workingDays) {
        this.workingDays = workingDays;
    }

    public List<String> getFeaturedItems() {
        return featuredItems;
    }

    public void setFeaturedItems(List<String> featuredItems) {
        this.featuredItems = featuredItems;
    }

    public Boolean getEnableCodAndSchedule() {
        return enableCodAndSchedule;
    }

    public Boolean getEnablePhoneVerification() {
        return enablePhoneVerification;
    }

    public void setEnableCodAndSchedule(Boolean enableCodAndSchedule) {
        this.enableCodAndSchedule = enableCodAndSchedule;
    }

    public void setEnablePhoneVerification(Boolean enablePhoneVerification) {
        this.enablePhoneVerification = enablePhoneVerification;
    }

    public boolean isMapEnabled() {
        return isMapEnabled;
    }

    public void setMapEnabled(boolean mapEnabled) {
        isMapEnabled = mapEnabled;
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    public void setBackendUrl(String backendUrl) {
        this.backendUrl = backendUrl;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupActivityListener();
        ACRA.init(this);
    }

    private void setupActivityListener() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }
            @Override
            public void onActivityStarted(Activity activity) {
            }
            @Override
            public void onActivityResumed(Activity activity) {
                activeActivity = activity;
            }
            @Override
            public void onActivityPaused(Activity activity) {
                activeActivity = null;
            }
            @Override
            public void onActivityStopped(Activity activity) {
            }
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }
            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    public Activity getActiveActivity(){
        return activeActivity;
    }
}
