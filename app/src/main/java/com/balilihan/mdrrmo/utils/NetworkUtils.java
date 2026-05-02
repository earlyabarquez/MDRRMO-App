// utils/NetworkUtils.java
// Used throughout the app to decide whether to call the API
// or save the report to the offline queue instead.

package com.balilihan.mdrrmo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkUtils {

    // Returns true if device has active internet (WiFi or mobile data)
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities =
                cm.getNetworkCapabilities(network);

        if (capabilities == null) return false;

        // Check for WiFi, mobile data, or ethernet
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
    }
}