// report/ReportViewModel.java

package com.balilihan.mdrrmo.report;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.balilihan.mdrrmo.database.AppDatabase;
import com.balilihan.mdrrmo.models.HazardReport;
import com.balilihan.mdrrmo.models.HazardType;
import com.balilihan.mdrrmo.utils.DateUtils;
import com.balilihan.mdrrmo.utils.NetworkUtils;
import com.balilihan.mdrrmo.utils.SessionManager;

import java.util.List;

public class ReportViewModel extends AndroidViewModel {

    public static class SubmitState {
        public enum Status { LOADING, SUCCESS, SAVED_OFFLINE, ERROR }

        public final Status       status;
        public final HazardReport report;  // non-null on SUCCESS or SAVED_OFFLINE
        public final String       message;

        private SubmitState(Status s, HazardReport r, String m) {
            status = s; report = r; message = m;
        }

        public static SubmitState loading() {
            return new SubmitState(Status.LOADING, null, null);
        }
        public static SubmitState success(HazardReport r) {
            return new SubmitState(Status.SUCCESS, r, null);
        }
        public static SubmitState savedOffline(HazardReport r) {
            return new SubmitState(Status.SAVED_OFFLINE, r, null);
        }
        public static SubmitState error(String m) {
            return new SubmitState(Status.ERROR, null, m);
        }
    }

    private final MutableLiveData<SubmitState> _submitState =
            new MutableLiveData<>();
    public  final LiveData<SubmitState>         submitState  = _submitState;

    private final MutableLiveData<List<HazardType>> _hazardTypes =
            new MutableLiveData<>();
    public  final LiveData<List<HazardType>>         hazardTypes  = _hazardTypes;

    private final AppDatabase    db;
    private final SessionManager session;

    public ReportViewModel(@NonNull Application app) {
        super(app);
        db      = AppDatabase.getInstance(app);
        session = SessionManager.getInstance(app);
    }

    // Called when form opens — loads hazard types from Room cache
    public void loadHazardTypes() {
        new Thread(() -> {
            // Room query must run on background thread
            // We observe the LiveData in the DAO directly
        }).start();
    }

    public LiveData<List<HazardType>> getHazardTypesFromDb() {
        return db.reportDao().getAllHazardTypes();
    }

    public void submitReport(
            int    hazardId,       // FK → hazard.hazard_id
            String hazardName,     // denormalized for display
            int    brgyId,         // FK → barangay.brgy_id
            String brgyName,       // denormalized for display
            String riskLevel,      // "Low", "Moderate", "High", "Critical"
            String description,    // optional notes
            double latitude,       // GPS coordinates
            double longitude,
            String imageUrl        // local file path before upload
    ) {
        // Validate required fields
        if (hazardId == -1) {
            _submitState.setValue(
                    SubmitState.error("Please select a hazard type")
            );
            return;
        }
        if (riskLevel == null || riskLevel.isEmpty()) {
            _submitState.setValue(
                    SubmitState.error("Please select a risk level")
            );
            return;
        }

        _submitState.setValue(SubmitState.loading());

        // Build the report object — matches HazardReport fields
        HazardReport report = new HazardReport();
        report.setUserId(session.getUserId());
        report.setUsername(session.getUsername());
        report.setHazardId(hazardId);
        report.setHazardName(hazardName);
        report.setBrgyId(brgyId);
        report.setBrgyName(brgyName);
        report.setRiskLevel(riskLevel);
        report.setDescription(description);
        report.setLatitude(latitude);
        report.setLongitude(longitude);
        report.setImageUrl(imageUrl);
        report.setReportDate(DateUtils.getCurrentTimestamp());
        report.setStatusId(1);    // 1 = Pending
        report.setStatus("Pending");
        report.setReportId(-1);   // -1 until uploaded to server

        new Thread(() -> {
            if (NetworkUtils.isConnected(getApplication())) {
                // Online — attempt to upload to Spring Boot
                submitToServer(report);
            } else {
                // Offline — save locally and queue for later
                saveOffline(report);
            }
        }).start();
    }

    private void submitToServer(HazardReport report) {
        // Mark as pending upload while we try
        report.setSyncStatus("PENDING_UPLOAD");
        long localId = db.reportDao().insertReport(report);
        report.setLocalId(localId); // ← fixed from setId()

        // TODO in Step 9: actual Retrofit multipart upload
        // For now simulate success for testing
        // Replace this block with real API call in Step 9

        // Simulate network call
        try { Thread.sleep(1500); } catch (Exception ignored) {}

        // On success: mark as uploaded
        report.setSyncStatus("UPLOADED");
        db.reportDao().updateReport(report);

        _submitState.postValue(SubmitState.success(report));
    }

    private void saveOffline(HazardReport report) {
        // No internet — save to Room with PENDING_UPLOAD status
        report.setSyncStatus("PENDING_UPLOAD");
        long localId = db.reportDao().insertReport(report);
        report.setLocalId(localId); // ← fixed from setId()

        // WorkManager will pick this up and upload when internet returns
        // (implemented in Step 7)
        _submitState.postValue(SubmitState.savedOffline(report));
    }
}