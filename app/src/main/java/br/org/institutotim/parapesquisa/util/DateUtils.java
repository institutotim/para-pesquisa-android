package br.org.institutotim.parapesquisa.util;

import android.content.Context;
import android.os.Build;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static DateTimeFormatter getShortDateInstanceWithoutYears() {
        String pattern = DateTimeFormat.patternForStyle("S-", Locale.getDefault());
        pattern = pattern.replace("/yy", "");
        return DateTimeFormat.forPattern(pattern);
    }

    public static DateTimeFormatter getFullDateTimeInstanceWithoutSeconds() {
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateTimeInstance();
        return DateTimeFormat.forPattern(sdf.toPattern().replaceAll("[^\\p{Alpha}]*s+[^\\p{Alpha}]*", ""));
    }

    public static String formatShortDate(Context context, Date date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            String format = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMddyyyy");
            return android.text.format.DateFormat.format(format, date).toString();
        }
        return android.text.format.DateFormat.getDateFormat(context).format(date);
    }

    public static DateTime parseShortDate(Context context, String date) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            String format = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMddyyyy");
            return DateTimeFormat.forPattern(format).parseDateTime(date);
        } else {
            try {
                return new DateTime(android.text.format.DateFormat.getDateFormat(context).parse(date));
            } catch (ParseException e) {
                return DateTime.now();
            }
        }
    }

    public static String formatShortDate(Context context, DateTime date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            String format = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMddyyyy");
            return android.text.format.DateFormat.format(format, date.toDate()).toString();
        }
        return android.text.format.DateFormat.getDateFormat(context).format(date.toDate());
    }

    public static String formatShortDate(Context context, String date) {
        if (date.length() == "dd-MM-yyyy".length()) {
            try {
                return formatShortDate(context, DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(date));
            } catch (Exception e) {
                return formatShortDate(context, DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(date));
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            String format = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMddyyyy");
            return android.text.format.DateFormat.format(format, new DateTime(date).toDate()).toString();
        }
        return android.text.format.DateFormat.getDateFormat(context).format(new DateTime(date).toDate());
    }
}
