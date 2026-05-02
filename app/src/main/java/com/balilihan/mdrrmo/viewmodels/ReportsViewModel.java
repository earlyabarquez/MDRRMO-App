// viewmodels/ReportsViewModel.java

package com.balilihan.mdrrmo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.balilihan.mdrrmo.database.AppDatabase;
import com.balilihan.mdrrmo.models.HazardReport;
import com.balilihan.mdrrmo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ReportsViewModel extends AndroidViewModel {

    // Filter options
    public static final String FILTER_ALL      = "All";
    public static final String FILTER_PENDING  = "Pending";
    public static final String FILTER_VERIFIED = "Verified";
    public static final String FILTER_REJECTED = "Rejected";

    // All reports from Room — unfiltered
    private List<HazardReport> allReports = new ArrayList<>();

    // Filtered reports — displayed in RecyclerView
    private final MutableLiveData<List<HazardReport>> _filteredReports =
            new MutableLiveData<>();
    public  final LiveData<List<HazardReport>>         filteredReports  =
            _filteredReports;

    // Current active filter
    private final MutableLiveData<String> _activeFilter =
            new MutableLiveData<>(FILTER_ALL);
    public  final LiveData<String>         activeFilter  = _activeFilter;

    // Pending sync count
    public final LiveData<Integer> pendingCount;

    // Loading state
    private final MutableLiveData<Boolean> _isLoading =
            new MutableLiveData<>(false);
    public  final LiveData<Boolean>         isLoading  = _isLoading;

    private final AppDatabase    db;
    private final SessionManager session;

    public ReportsViewModel(@NonNull Application app) {
        super(app);
        db      = AppDatabase.getInstance(app);
        session = SessionManager.getInstance(app);

        // Observe pending count from Room directly
        pendingCount = db.reportDao().getPendingUploadCount();
    }

    // Fetch all reports for current user from Room
    // TODO Step 9: Also fetch from server and merge
    public void fetchReports() {
        _isLoading.setValue(true);

        new Thread(() -> {
            // Room query on background thread
            // For now loads from local Room only
            // In Step 9 we also fetch from Spring Boot API

            // ── MOCK DATA — replace with Room + API in Step 9 ───
            List<HazardReport> mock = getMockReports();
            allReports = mock;
            applyFilter(_activeFilter.getValue());
            _isLoading.postValue(false);
            // ── END MOCK DATA ────────────────────────────────────
        }).start();
    }

    // Apply filter to the full report list
    public void setFilter(String filter) {
        _activeFilter.setValue(filter);
        applyFilter(filter);
    }

    private void applyFilter(String filter) {
        if (filter == null || filter.equals(FILTER_ALL)) {
            _filteredReports.postValue(new ArrayList<>(allReports));
            return;
        }

        List<HazardReport> filtered = new ArrayList<>();
        for (HazardReport r : allReports) {
            if (filter.equals(r.getStatus())) {
                filtered.add(r);
            }
        }
        _filteredReports.postValue(filtered);
    }

    // Mock reports for UI testing
    private List<HazardReport> getMockReports() {
        List<HazardReport> list = new ArrayList<>();

        HazardReport r1 = new HazardReport();
        r1.setLocalId(1);
        r1.setReportId(41);
        r1.setHazardName("Flood");
        r1.setRiskLevel("High");
        r1.setStatus("Pending");
        r1.setBrgyName("Barangay Dangay");
        r1.setReportDate("2024-12-14T09:41:00");
        r1.setSyncStatus("UPLOADED");
        r1.setDescription("Flood on main road, knee-deep water");
        r1.setLatitude(9.8850);
        r1.setLongitude(123.9820);
        r1.setUsername("Juan Dela Cruz");
        list.add(r1);

        HazardReport r2 = new HazardReport();
        r2.setLocalId(2);
        r2.setReportId(40);
        r2.setHazardName("Landslide");
        r2.setRiskLevel("Critical");
        r2.setStatus("Verified");
        r2.setBrgyName("Barangay Laya");
        r2.setReportDate("2024-12-14T08:30:00");
        r2.setSyncStatus("UPLOADED");
        r2.setDescription("Landslide blocking road to barangay");
        r2.setLatitude(9.8800);
        r2.setLongitude(123.9850);
        r2.setUsername("Juan Dela Cruz");
        list.add(r2);

        HazardReport r3 = new HazardReport();
        r3.setLocalId(3);
        r3.setReportId(-1); // not yet uploaded
        r3.setHazardName("Road Damage");
        r3.setRiskLevel("Moderate");
        r3.setStatus("Pending");
        r3.setBrgyName("Barangay Canayaon");
        r3.setReportDate("2024-12-13T14:20:00");
        r3.setSyncStatus("PENDING_UPLOAD"); // offline report
        r3.setDescription("Large pothole on barangay road");
        r3.setLatitude(9.8820);
        r3.setLongitude(123.9860);
        r3.setUsername("Juan Dela Cruz");
        list.add(r3);

        HazardReport r4 = new HazardReport();
        r4.setLocalId(4);
        r4.setReportId(38);
        r4.setHazardName("Structural Fire");
        r4.setRiskLevel("High");
        r4.setStatus("Rejected");
        r4.setBrgyName("Barangay Boctol");
        r4.setReportDate("2024-12-13T11:05:00");
        r4.setSyncStatus("UPLOADED");
        r4.setDescription("House fire reported near market");
        r4.setLatitude(9.8840);
        r4.setLongitude(123.9810);
        r4.setUsername("Juan Dela Cruz");
        list.add(r4);

        HazardReport r5 = new HazardReport();
        r5.setLocalId(5);
        r5.setReportId(37);
        r5.setHazardName("Downed Power Line");
        r5.setRiskLevel("Critical");
        r5.setStatus("Verified");
        r5.setBrgyName("Barangay Poblacion");
        r5.setReportDate("2024-12-12T07:15:00");
        r5.setSyncStatus("UPLOADED");
        r5.setDescription("Power line down after typhoon");
        r5.setLatitude(9.8870);
        r5.setLongitude(123.9800);
        r5.setUsername("Juan Dela Cruz");
        list.add(r5);

        return list;
    }
}