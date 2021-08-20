package com.zeappa.chickenonfire;

import android.content.Context;

public class WorkDay {
    private int openingMinute;
    private int openingHour;
    private int closingMinute;
    private int closingHour;
    private String openingAmPm;
    private String closingAmPm;

    WorkDay(int openingMinute, int openingHour, int closingMinute, int closingHour, Context context) {
        this.openingMinute = openingMinute;
        this.openingHour = openingHour;
        this.closingMinute = closingMinute;
        this.closingHour = closingHour;

        if (openingHour > 12){
            setOpeningAmPm(context.getResources().getString(R.string.pm));
            this.openingHour = this.openingHour - 12;
        }else{
            setOpeningAmPm(context.getResources().getString(R.string.am));
            if (openingHour == 0){
                this.openingHour = 12;
            }
        }

        if (closingHour > 12){
            setClosingAmPm(context.getResources().getString(R.string.pm));
            this.closingHour = this.closingHour - 12;
        }else{
            setClosingAmPm(context.getResources().getString(R.string.am));
            if (closingHour == 0){
                this.closingHour = 12;
            }
        }

    }

    public int getOpeningMinute() {
        return openingMinute;
    }

    public void setOpeningMinute(int openingMinute) {
        this.openingMinute = openingMinute;
    }

    public int getOpeningHour() {
        return openingHour;
    }

    public void setOpeningHour(int openingHour) {
        this.openingHour = openingHour;
    }

    public int getClosingMinute() {
        return closingMinute;
    }

    public void setClosingMinute(int closingMinute) {
        this.closingMinute = closingMinute;
    }

    public int getClosingHour() {
        return closingHour;
    }

    public void setClosingHour(int closingHour) {
        this.closingHour = closingHour;
    }

    public String getOpeningAmPm() {
        return openingAmPm;
    }

    public void setOpeningAmPm(String openingAmPm) {
        this.openingAmPm = openingAmPm;
    }

    public String getClosingAmPm() {
        return closingAmPm;
    }

    public void setClosingAmPm(String closingAmPm) {
        this.closingAmPm = closingAmPm;
    }
}
