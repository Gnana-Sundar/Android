package com.sundar.devtech.Services;

import android.os.Handler;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateAndTime {

    public static SimpleDateFormat getDate(Calendar CALENDAR) {
        String DateFormat = "dd-MM-yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormat, Locale.US);
        return dateFormat;
    }

    // Correct date format for months
    public static String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(new Date());
    }

    // Correct method for 12-hour time format
    public static String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        return dateFormat.format(new Date());
    }

    // Date with day name
    public static String getDateAndDay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy / EEEE");
        return dateFormat.format(new Date());
    }

    // Time with AM/PM marker
    public static String getTimeAndMarker() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
        return dateFormat.format(new Date());
    }

    // SQL date format
    public static String getSqlDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }

    // Correct date format for months
    public static String getDeviceDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    // Correct method for 12-hour time format
    public static String getDeviceTime() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        return dateFormat.format(date);
    }
}
