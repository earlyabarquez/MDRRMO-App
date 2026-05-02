// utils/PermissionUtils.java

package com.balilihan.mdrrmo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    // Request codes — used to identify which permission
    // was requested in onRequestPermissionsResult()
    public static final int REQUEST_LOCATION    = 100;
    public static final int REQUEST_CAMERA      = 101;
    public static final int REQUEST_NOTIFICATION = 102;

    // ── Location ────────────────────────────────────────────────
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQUEST_LOCATION
        );
    }

    // ── Camera ──────────────────────────────────────────────────
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{ Manifest.permission.CAMERA },
                REQUEST_CAMERA
        );
    }

    // ── Notifications (Android 13+) ──────────────────────────────
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }
        // Below Android 13, notifications don't need runtime permission
        return true;
    }

    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                    REQUEST_NOTIFICATION
            );
        }
    }
}