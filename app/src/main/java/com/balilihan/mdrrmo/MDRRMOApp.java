// MDRRMOApp.java
// Runs once on app launch before any Activity.
// Initializes notification channels for the entire app lifetime.

package com.balilihan.mdrrmo;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MDRRMOApp extends Application {

    // Notification channel IDs
    // Referenced anywhere a notification is built in the app
    public static final String CHANNEL_HAZARD_ALERTS = "hazard_alerts";
    public static final String CHANNEL_REPORT_STATUS = "report_status";
    public static final String CHANNEL_SYNC          = "sync_status";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        // Notification channels only exist on Android 8.0 (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            // Channel 1 — Hazard Alerts
            // IMPORTANCE_HIGH: Makes sound + heads-up popup on screen
            // Used when a new hazard appears within the reporter's 200m radius
            NotificationChannel hazardChannel = new NotificationChannel(
                    CHANNEL_HAZARD_ALERTS,
                    "Hazard Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            hazardChannel.setDescription(
                    "Alerts for hazards detected within your radius"
            );

            // Channel 2 — Report Status
            // IMPORTANCE_DEFAULT: Makes sound, no popup
            // Used when admin verifies or rejects a submitted report
            NotificationChannel statusChannel = new NotificationChannel(
                    CHANNEL_REPORT_STATUS,
                    "Report Status",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            statusChannel.setDescription(
                    "Updates when your reports are verified or rejected"
            );

            // Channel 3 — Sync Status
            // IMPORTANCE_LOW: Silent, no sound
            // Used for background sync progress indicator
            NotificationChannel syncChannel = new NotificationChannel(
                    CHANNEL_SYNC,
                    "Sync Status",
                    NotificationManager.IMPORTANCE_LOW
            );
            syncChannel.setDescription(
                    "Background sync progress for offline reports"
            );

            manager.createNotificationChannel(hazardChannel);
            manager.createNotificationChannel(statusChannel);
            manager.createNotificationChannel(syncChannel);
        }
    }
}