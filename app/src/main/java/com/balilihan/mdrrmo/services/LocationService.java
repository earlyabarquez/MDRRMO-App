// services/LocationService.java
// Foreground service — runs even when app is minimized
// Continuously tracks user GPS position
// Used by MapFragment for centering and by GeofenceManager
// for 200m hazard alert radius

package com.balilihan.mdrrmo.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.balilihan.mdrrmo.MDRRMOApp;
import com.balilihan.mdrrmo.R;
import com.balilihan.mdrrmo.activities.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationService extends Service {

    // Broadcast action — MapFragment listens for this
    public static final String ACTION_LOCATION_UPDATE =
            "com.balilihan.mdrrmo.LOCATION_UPDATE";
    public static final String EXTRA_LATITUDE  = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";

    private FusedLocationProviderClient locationClient;
    private LocationCallback            locationCallback;

    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        setupLocationCallback();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Must call startForeground() immediately — required for foreground service
        startForeground(NOTIFICATION_ID, buildNotification());
        startLocationUpdates();
        // START_STICKY: restart service if killed by system
        return START_STICKY;
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null) {
                    // Broadcast location to MapFragment
                    Intent broadcast = new Intent(ACTION_LOCATION_UPDATE);
                    broadcast.putExtra(EXTRA_LATITUDE,  location.getLatitude());
                    broadcast.putExtra(EXTRA_LONGITUDE, location.getLongitude());
                    sendBroadcast(broadcast);
                }
            }
        };
    }

    private void startLocationUpdates() {
        // Request location every 10 seconds
        // PRIORITY_HIGH_ACCURACY uses GPS for best precision
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10_000 // interval in ms
        )
                .setMinUpdateIntervalMillis(5_000) // fastest update every 5 seconds
                .build();

        try {
            locationClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } catch (SecurityException e) {
            // Permission not granted — service will stop
            stopSelf();
        }
    }

    private Notification buildNotification() {
        // Tapping notification opens MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, MDRRMOApp.CHANNEL_SYNC)
                .setContentTitle("MDRRMO")
                .setContentText("Monitoring for nearby hazards")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)  // can't be dismissed by user
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop location updates when service is destroyed
        if (locationClient != null && locationCallback != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
}