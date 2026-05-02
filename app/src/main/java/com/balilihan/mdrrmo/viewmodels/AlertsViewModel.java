// viewmodels/AlertsViewModel.java

package com.balilihan.mdrrmo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.balilihan.mdrrmo.models.HazardAlert;

import java.util.ArrayList;
import java.util.List;

public class AlertsViewModel extends AndroidViewModel {

    // All alerts — newest first
    private final MutableLiveData<List<HazardAlert>> _alerts =
            new MutableLiveData<>();
    public  final LiveData<List<HazardAlert>>         alerts  = _alerts;

    // Unread count — drives the nav tab badge
    private final MutableLiveData<Integer> _unreadCount =
            new MutableLiveData<>(0);
    public  final LiveData<Integer>         unreadCount  = _unreadCount;

    // Loading state
    private final MutableLiveData<Boolean> _isLoading =
            new MutableLiveData<>(false);
    public  final LiveData<Boolean>         isLoading  = _isLoading;

    public AlertsViewModel(@NonNull Application application) {
        super(application);
    }

    // Fetch all active hazards in Balilihan — newest first
    // TODO Step 9: Replace mock data with real API call
    // GET /api/hazards?status=Verified&status=Auto-published&sort=newest
    public void fetchAlerts() {
        _isLoading.setValue(true);

        // ── MOCK DATA — replace with real API call in Step 9 ────
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(() -> {
                    List<HazardAlert> mockAlerts = getMockAlerts();
                    _alerts.setValue(mockAlerts);
                    updateUnreadCount(mockAlerts);
                    _isLoading.setValue(false);
                }, 800);
        // ── END MOCK DATA ────────────────────────────────────────

        // ── REAL API CALL — uncomment in Step 9 ─────────────────
        // TODO: Uncomment when Spring Boot backend is ready
//        ApiClient.getApiService(getApplication())
//                .getAlerts()
//                .enqueue(new Callback<List<HazardAlert>>() {
//                    @Override
//                    public void onResponse(Call<List<HazardAlert>> call,
//                                           Response<List<HazardAlert>> response) {
//                        _isLoading.postValue(false);
//                        if (response.isSuccessful() && response.body() != null) {
//                            List<HazardAlert> alerts = response.body();
//                            _alerts.postValue(alerts);
//                            updateUnreadCount(alerts);
//                        }
//                    }
//                    @Override
//                    public void onFailure(Call<List<HazardAlert>> call,
//                                         Throwable t) {
//                        _isLoading.postValue(false);
//                    }
//                });
        // ── END REAL API CALL ────────────────────────────────────
    }

    // Mark a single alert as read
    public void markAsRead(int position) {
        List<HazardAlert> current = _alerts.getValue();
        if (current == null || position >= current.size()) return;

        current.get(position).setRead(true);
        _alerts.setValue(current);
        updateUnreadCount(current);
    }

    // Mark all alerts as read
    public void markAllAsRead() {
        List<HazardAlert> current = _alerts.getValue();
        if (current == null) return;

        for (HazardAlert alert : current) {
            alert.setRead(true);
        }
        _alerts.setValue(current);
        _unreadCount.setValue(0);
    }

    private void updateUnreadCount(List<HazardAlert> alerts) {
        int count = 0;
        for (HazardAlert alert : alerts) {
            if (!alert.isRead()) count++;
        }
        _unreadCount.setValue(count);
    }

    // Mock alerts — newest first, all in Balilihan
    private List<HazardAlert> getMockAlerts() {
        List<HazardAlert> list = new ArrayList<>();

        HazardAlert a1 = new HazardAlert();
        a1.setReportId(1);
        a1.setHazardName("Flood");
        a1.setRiskLevel("High");
        a1.setStatus("Verified");
        a1.setLatitude(9.8850);
        a1.setLongitude(123.9820);
        a1.setBrgyName("Barangay Dangay");
        a1.setUsername("Juan D.");
        a1.setReportDate("2024-12-14T09:41:00");
        a1.setDescription("Flood on main road, knee-deep water");
        a1.setRead(false);
        list.add(a1);

        HazardAlert a2 = new HazardAlert();
        a2.setReportId(2);
        a2.setHazardName("Landslide");
        a2.setRiskLevel("Critical");
        a2.setStatus("Auto-published");
        a2.setLatitude(9.8800);
        a2.setLongitude(123.9850);
        a2.setBrgyName("Barangay Laya");
        a2.setUsername("Maria S.");
        a2.setReportDate("2024-12-14T08:30:00");
        a2.setDescription("Landslide blocking road to barangay");
        a2.setRead(false);
        list.add(a2);

        HazardAlert a3 = new HazardAlert();
        a3.setReportId(3);
        a3.setHazardName("Downed Power Line");
        a3.setRiskLevel("Critical");
        a3.setStatus("Verified");
        a3.setLatitude(9.8870);
        a3.setLongitude(123.9800);
        a3.setBrgyName("Barangay Poblacion");
        a3.setUsername("Pedro L.");
        a3.setReportDate("2024-12-14T07:15:00");
        a3.setDescription("Power line down after typhoon");
        a3.setRead(true); // already read
        list.add(a3);

        HazardAlert a4 = new HazardAlert();
        a4.setReportId(4);
        a4.setHazardName("Road Damage");
        a4.setRiskLevel("Moderate");
        a4.setStatus("Verified");
        a4.setLatitude(9.8820);
        a4.setLongitude(123.9860);
        a4.setBrgyName("Barangay Canayaon");
        a4.setUsername("Ana R.");
        a4.setReportDate("2024-12-13T14:20:00");
        a4.setDescription("Large pothole on barangay road");
        a4.setRead(true); // already read
        list.add(a4);

        HazardAlert a5 = new HazardAlert();
        a5.setReportId(5);
        a5.setHazardName("Structural Fire");
        a5.setRiskLevel("High");
        a5.setStatus("Auto-published");
        a5.setLatitude(9.8840);
        a5.setLongitude(123.9810);
        a5.setBrgyName("Barangay Boctol");
        a5.setUsername("Carlo M.");
        a5.setReportDate("2024-12-13T11:05:00");
        a5.setDescription("House fire reported near market");
        a5.setRead(false);
        list.add(a5);

        return list;
    }
}