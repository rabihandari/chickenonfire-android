package com.zeappa.chickenonfire.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.zeappa.chickenonfire.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RestaurantOpenStatus {

    private Context context;



    @SuppressLint("SimpleDateFormat")
    public RestaurantOpenStatus(Context context) {
        this.context = context;

    }

    public boolean isOpen() {

        Calendar calendar = Calendar.getInstance();

        return checkStatus(calendar);
    }


    public boolean isOpen(Date date){

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return checkStatus(calendar);

    }


    private boolean checkStatus(Calendar calendar){

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String currentDay;
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                currentDay = context.getResources().getString(R.string.monday_value_eng);
                break;
            case Calendar.TUESDAY:
                currentDay = context.getResources().getString(R.string.tuesday_value_eng);
                break;
            case Calendar.WEDNESDAY:
                currentDay = context.getResources().getString(R.string.wednesday_value_eng);
                break;
            case Calendar.THURSDAY:
                currentDay = context.getResources().getString(R.string.thursday_value_eng);
                break;
            case Calendar.FRIDAY:
                currentDay = context.getResources().getString(R.string.friday_value_eng);
                break;
            case Calendar.SATURDAY:
                currentDay = context.getResources().getString(R.string.saturday_value_eng);
                break;
            default:
                currentDay = context.getResources().getString(R.string.sunday_value_eng);
                break;
        }


        String openingAmPm = currentDay.split("-")[0].trim().split(" ")[1];
        String closingAmPm = currentDay.split("-")[1].trim().split(" ")[1];

        String inputOpening = getOpeningHour(currentDay) + ":" + getOpeningMinute(currentDay) + ":00 "  + openingAmPm;
        String inputClosing = getClosingHour(currentDay) + ":" + getClosingMinute(currentDay) + ":00 "  + closingAmPm;
        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("hh:mm:ss aa");
        @SuppressLint("SimpleDateFormat") DateFormat outputformat = new SimpleDateFormat("HH:mm:ss");
        Date date2;
        String outputOpening = null;
        String outputClosing = null;
        try{

            date2 = df.parse(inputOpening);
            outputOpening = outputformat.format(date2);

            date2= df.parse(inputClosing);
            outputClosing = outputformat.format(date2);

        }catch(ParseException pe){
            pe.printStackTrace();
        }

        // 24 hour format
        int openingHour = Integer.parseInt(outputOpening.split(":")[0]);
        int closingHour = Integer.parseInt(outputClosing.split(":")[0]);
        int openingMinute = Integer.parseInt(outputOpening.split(":")[1]);
        int closingMinute = Integer.parseInt(outputClosing.split(":")[1]);


        //Conditions
        if(calendar.get(Calendar.HOUR_OF_DAY) == openingHour){
            return calendar.get(Calendar.MINUTE) > openingMinute;
        }
        if(calendar.get(Calendar.HOUR_OF_DAY) == closingHour){
            return calendar.get(Calendar.MINUTE) < closingMinute;
        }

        if(openingHour > closingHour){
            return calendar.get(Calendar.HOUR_OF_DAY) <= closingHour || calendar.get(Calendar.HOUR_OF_DAY) >= openingHour;
        }

        return calendar.get(Calendar.HOUR_OF_DAY) > openingHour && calendar.get(Calendar.HOUR_OF_DAY) < closingHour;
    }




    private int getOpeningHour(String dayValue){
        String openingHour = dayValue.split("-")[0].trim().split(" ")[0].split(":")[0];
        return Integer.parseInt(openingHour);
    }

    private int getClosingHour(String dayValue){
        String closingHour = dayValue.split("-")[1].trim().split(" ")[0].split(":")[0];
        return Integer.parseInt(closingHour);
    }

    private int getOpeningMinute(String dayValue){
        String openingMinute = dayValue.split("-")[0].trim().split(" ")[0].split(":")[1];
        return Integer.parseInt(openingMinute);
    }

    private int getClosingMinute(String dayValue){
        String closingMinute = dayValue.split("-")[1].trim().split(" ")[0].split(":")[1];
        return Integer.parseInt(closingMinute);
    }
}
