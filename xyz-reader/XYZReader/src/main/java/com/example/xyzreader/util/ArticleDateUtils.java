package com.example.xyzreader.util;

import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class ArticleDateUtils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private static final DateFormat outputFormat = DateFormat.getDateInstance();
    // Most time functions can only handle 1902 - 2037
    private static final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    private static Date parseStringToDate(String publishedDate) {
        try {
            return dateFormat.parse(publishedDate);
        } catch (ParseException ex) {
            return new Date();
        }
    }

    public static String parsePublishedDate(String publishedDateString) {

        Date publishedDate = parseStringToDate(publishedDateString);

        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            return DateUtils.getRelativeTimeSpanString(
                    publishedDate.getTime(),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString();
        } else {
            // If date is before 1902, just show the string
            return outputFormat.format(publishedDate);
        }
    }
}
