// services/FCMService.java
// This class runs automatically when a push notification arrives.
// Firebase calls onMessageReceived() — we build and show the notification here.

package com.balilihan.mdrrmo.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.balilihan.mdrrmo.MDRRMOApp;
import com.balilihan.mdrrmo.R;
import com.balilihan.mdrrmo.activities.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Called whenever Firebase generates a new FCM token for this device.
        // This token is how your Spring Boot backend knows WHERE to send
        // the push notification for this specific device.
        // You need to send this token to your Spring Boot API so it can
        // store it against the logged-in user.
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        // Called when a push notification arrives while the app is
        // in the foreground. When app is in background, Firebase
        // shows the notification automatically without calling this.

        String title = "MDRRMO Alert";
        String body  = "";

        // Try notification payload first (sent by Firebase Console)
        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body  = message.getNotification().getBody();
        }

        // Then check data payload (sent by Spring Boot backend)
        // Data payload works in both foreground AND background
        if (message.getData().size() > 0) {
            if (message.getData().containsKey("title")) {
                title = message.getData().get("title");
            }
            if (message.getData().containsKey("body")) {
                body = message.getData().get("body");
            }
        }

        // Determine which notification channel to use
        // based on the type sent from Spring Boot
        String type    = message.getData().get("type");
        String channel = getChannelForType(type);

        showNotification(title, body, channel);
    }

    private String getChannelForType(String type) {
        if (type == null) return MDRRMOApp.CHANNEL_HAZARD_ALERTS;
        switch (type) {
            case "REPORT_STATUS":
                return MDRRMOApp.CHANNEL_REPORT_STATUS;
            case "SYNC":
                return MDRRMOApp.CHANNEL_SYNC;
            default:
                // "HAZARD_ALERT" or anything else
                return MDRRMOApp.CHANNEL_HAZARD_ALERTS;
        }
    }

    private void showNotification(String title, String body, String channelId) {
        // Tapping the notification opens MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)  // see Step 2 below
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)   // dismisses notification when tapped
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Use current time as notification ID so multiple notifications
        // don't overwrite each other
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void sendTokenToServer(String token) {
        // TODO in Step 9 (Spring Boot API integration):
        // Call your API endpoint to save this token against the logged-in user
        // Example: POST /api/users/fcm-token  { "token": "xxxxx" }
        // For now just log it so you can see it in Logcat
        android.util.Log.d("FCMService", "FCM Token: " + token);
    }
}