// utils/DateUtils.java

package com.balilihan.mdrrmo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    // Format used when storing timestamps in Room and sending to API
    // Example: "2024-12-14T09:41:00"
    private static final String ISO_FORMAT      =
            "yyyy-MM-dd'T'HH:mm:ss";

    // Format used when displaying to the reporter
    // Example: "Dec 14, 2024 9:41 AM"
    private static final String DISPLAY_FORMAT  =
            "MMM dd, yyyy h:mm a";

    // Get current timestamp in ISO format for storage
    public static String getCurrentTimestamp() {
        return new SimpleDateFormat(ISO_FORMAT, Locale.getDefault())
                .format(new Date());
    }

    // Convert ISO timestamp to human-readable display format
    public static String formatForDisplay(String isoTimestamp) {
        try {
            Date date = new SimpleDateFormat(ISO_FORMAT, Locale.getDefault())
                    .parse(isoTimestamp);
            return new SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault())
                    .format(date);
        } catch (Exception e) {
            return isoTimestamp;
        }
    }
}