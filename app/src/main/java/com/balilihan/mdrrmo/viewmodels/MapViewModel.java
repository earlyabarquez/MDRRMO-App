// viewmodels/MapViewModel.java

package com.balilihan.mdrrmo.viewmodels;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.balilihan.mdrrmo.models.HazardMarker;
import com.balilihan.mdrrmo.utils.LocationUtils;

import java.util.ArrayList;
import java.util.List;

public class MapViewModel extends AndroidViewModel {

    // User's current location — updated by LocationService
    private final MutableLiveData<Location> _userLocation =
            new MutableLiveData<>();
    public  final LiveData<Location>         userLocation  = _userLocation;

    // Hazard markers fetched from API
    private final MutableLiveData<List<HazardMarker>> _hazardMarkers =
            new MutableLiveData<>();
    public  final LiveData<List<HazardMarker>>         hazardMarkers  =
            _hazardMarkers;

    // Loading state
    private final MutableLiveData<Boolean> _isLoading =
            new MutableLiveData<>(false);
    public  final LiveData<Boolean>         isLoading  = _isLoading;

    // Error state
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public  final LiveData<String>         error  = _error;

    public MapViewModel(@NonNull Application application) {
        super(application);
    }

    public void updateUserLocation(Location location) {
        _userLocation.setValue(location);
    }

    // Fetch hazard markers from Spring Boot API
    // TODO Step 9: Replace mock data with real API call
    // GET /api/hazards/map?status=Verified&status=Auto-published
    public void fetchHazardMarkers() {
        _isLoading.setValue(true);

        // ── MOCK DATA — replace with real API call in Step 9 ────
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(() -> {
                    List<HazardMarker> mockMarkers = getMockHazardMarkers();
                    _hazardMarkers.setValue(mockMarkers);
                    _isLoading.setValue(false);
                }, 1000);
        // ── END MOCK DATA ────────────────────────────────────────

        // ── REAL API CALL — uncomment in Step 9 ─────────────────
        // TODO: Uncomment when Spring Boot backend is ready
//        ApiClient.getApiService(getApplication())
//                .getHazardMarkers()
//                .enqueue(new Callback<List<HazardMarker>>() {
//                    @Override
//                    public void onResponse(Call<List<HazardMarker>> call,
//                                           Response<List<HazardMarker>> response) {
//                        _isLoading.postValue(false);
//                        if (response.isSuccessful() && response.body() != null) {
//                            _hazardMarkers.postValue(response.body());
//                        } else {
//                            _error.postValue("Failed to load hazards");
//                        }
//                    }
//                    @Override
//                    public void onFailure(Call<List<HazardMarker>> call, Throwable t) {
//                        _isLoading.postValue(false);
//                        _error.postValue("Cannot reach server");
//                    }
//                });
        // ── END REAL API CALL ────────────────────────────────────
    }

    // Mock hazard markers around Balilihan, Bohol for UI testing
    private List<HazardMarker> getMockHazardMarkers() {
        List<HazardMarker> markers = new ArrayList<>();

        HazardMarker m1 = new HazardMarker();
        m1.setReportId(1);
        m1.setHazardName("Flood");
        m1.setRiskLevel("High");
        m1.setStatus("Verified");
        m1.setLatitude(9.8850);
        m1.setLongitude(123.9820);
        m1.setBrgyName("Barangay Dangay");
        m1.setUsername("Juan D.");
        m1.setReportDate("2024-12-14T09:41:00");
        m1.setDescription("Flood on main road, knee-deep water");
        markers.add(m1);

        HazardMarker m2 = new HazardMarker();
        m2.setReportId(2);
        m2.setHazardName("Landslide");
        m2.setRiskLevel("Critical");
        m2.setStatus("Auto-published");
        m2.setLatitude(9.8800);
        m2.setLongitude(123.9850);
        m2.setBrgyName("Barangay Laya");
        m2.setUsername("Maria S.");
        m2.setReportDate("2024-12-14T08:30:00");
        m2.setDescription("Landslide blocking road to barangay");
        markers.add(m2);

        HazardMarker m3 = new HazardMarker();
        m3.setReportId(3);
        m3.setHazardName("Downed Power Line");
        m3.setRiskLevel("Critical");
        m3.setStatus("Verified");
        m3.setLatitude(9.8870);
        m3.setLongitude(123.9800);
        m3.setBrgyName("Barangay Poblacion");
        m3.setUsername("Pedro L.");
        m3.setReportDate("2024-12-14T07:15:00");
        m3.setDescription("Power line down after typhoon");
        markers.add(m3);

        HazardMarker m4 = new HazardMarker();
        m4.setReportId(4);
        m4.setHazardName("Road Damage");
        m4.setRiskLevel("Moderate");
        m4.setStatus("Verified");
        m4.setLatitude(9.8820);
        m4.setLongitude(123.9860);
        m4.setBrgyName("Barangay Canayaon");
        m4.setUsername("Ana R.");
        m4.setReportDate("2024-12-13T14:20:00");
        m4.setDescription("Large pothole on barangay road");
        markers.add(m4);

        HazardMarker m5 = new HazardMarker();
        m5.setReportId(5);
        m5.setHazardName("Structural Fire");
        m5.setRiskLevel("High");
        m5.setStatus("Auto-published");
        m5.setLatitude(9.8840);
        m5.setLongitude(123.9810);
        m5.setBrgyName("Barangay Boctol");
        m5.setUsername("Carlo M.");
        m5.setReportDate("2024-12-13T11:05:00");
        m5.setDescription("House fire reported near market");
        markers.add(m5);

        return markers;
    }
}