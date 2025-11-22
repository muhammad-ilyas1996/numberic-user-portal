package com.numbericsuserportal.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class DateUtils {

    public static final String DATE_FORMAT_2 = "dd-MM-yyyy";
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String WEEK = "week";


    public static String dateToString(Date dtDate, String format) {
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(Objects.isNull(format) || format.isEmpty() ? DATE_FORMAT_2 : format);
            String sDate = dateFormatter.format(dtDate);
            return sDate;
        } catch (Exception e) {
            return null;
        }
    }

    public static Date toDate(String date, String format) {
        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date stringToDate(String dateStr, String format) {
        if (dateStr == null || format == null) return null;
        try {
            return new SimpleDateFormat(format).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Date getDateFromDurationString(String duration){
        Calendar cal = Calendar.getInstance();

        if(duration.trim().equalsIgnoreCase(YEAR)){
            cal.add(Calendar.YEAR,-1);
        }else if(duration.trim().equalsIgnoreCase(MONTH)){
            cal.add(Calendar.MONTH,-1);
        } else if (duration.trim().equalsIgnoreCase(WEEK)) {
            cal.add(Calendar.DATE,-7);
        }

        return cal.getTime();
    }

}
