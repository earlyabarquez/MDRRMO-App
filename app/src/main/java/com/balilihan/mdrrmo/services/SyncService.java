// services/SyncService.java
// WorkManager worker — automatically triggered when internet
// is restored. Uploads all PENDING_UPLOAD reports to Spring Boot.

package com.balilihan.mdrrmo.services;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.balilihan.mdrrmo.database.AppDatabase;
import com.balilihan.mdrrmo.models.HazardReport;

import java.util.List;

public class SyncService extends Worker {

    public SyncService(@NonNull Context context,
                       @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // This runs on a background thread automatically
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        // Get all pending reports from Room
        List<HazardReport> pending =
                db.reportDao().getPendingUploadReports();

        if (pending.isEmpty()) return Result.success();

        // TODO Step 9: Replace simulation with real Retrofit upload
        // For each pending report:
        // 1. Build multipart request with photo
        // 2. POST to /api/reports
        // 3. On success → mark as UPLOADED in Room
        // 4. On failure → return Result.retry()

        for (HazardReport report : pending) {
            try {
                // Simulate upload for now
                Thread.sleep(500);

                // Mark as uploaded
                db.syncQueueDao().markAsUploaded(report.getLocalId());

            } catch (Exception e) {
                // Retry later if upload fails
                return Result.retry();
            }
        }

        return Result.success();
    }

    // Schedule sync — called when user taps "Sync now"
    // or when app detects internet restored
    public static void scheduleSyncNow(Context context) {
        // Only run when internet is available
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest =
                new OneTimeWorkRequest.Builder(SyncService.class)
                        .setConstraints(constraints)
                        .build();

        // KEEP existing work — don't cancel if already running
        WorkManager.getInstance(context).enqueue(syncRequest);
    }
}