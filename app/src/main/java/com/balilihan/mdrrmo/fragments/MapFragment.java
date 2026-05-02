// fragments/MapFragment.java

package com.balilihan.mdrrmo.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.balilihan.mdrrmo.R;
import com.balilihan.mdrrmo.databinding.FragmentMapBinding;
import com.balilihan.mdrrmo.models.HazardMarker;
import com.balilihan.mdrrmo.services.LocationService;
import com.balilihan.mdrrmo.utils.DateUtils;
import com.balilihan.mdrrmo.utils.LocationUtils;
import com.balilihan.mdrrmo.viewmodels.MapViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private MapViewModel       viewModel;
    private GoogleMap          googleMap;

    // Maps marker ID to HazardMarker data for bottom sheet
    private final Map<String, HazardMarker> markerDataMap = new HashMap<>();

    // User's last known location
    private double userLat = LocationUtils.BALILIHAN_LAT;
    private double userLng = LocationUtils.BALILIHAN_LNG;

    // Receives location broadcasts from LocationService
    private BroadcastReceiver locationReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding   = FragmentMapBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Google Map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupObservers();
        setupLocationReceiver();
        setupClickListeners();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Map style settings
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);

        // Enable my location layer if permission granted
        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            // Permission not granted — location dot won't show
        }

        // Center on Balilihan initially — will update when GPS fires
        LatLng balilihan = new LatLng(
                LocationUtils.BALILIHAN_LAT,
                LocationUtils.BALILIHAN_LNG
        );
        googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(balilihan, 14f)
        );

        // Listen for pin taps
        googleMap.setOnMarkerClickListener(marker -> {
            HazardMarker hazard = markerDataMap.get(marker.getId());
            if (hazard != null) {
                showHazardDetailBottomSheet(hazard);
            }
            return true;
        });

        // Load hazard markers
        viewModel.fetchHazardMarkers();
    }

    private void setupObservers() {
        // Observe hazard markers — add pins to map when loaded
        viewModel.hazardMarkers.observe(getViewLifecycleOwner(), markers -> {
            if (markers != null) {
                addMarkersToMap(markers);
                binding.tvHazardCount.setText(markers.size() + " active");
            }
        });

        // Loading state
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.mapProgressBar.setVisibility(
                    isLoading ? View.VISIBLE : View.GONE
            );
        });

        // User location from ViewModel
        viewModel.userLocation.observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLng = location.getLongitude();
                centerOnUserLocation();
            }
        });
    }

    private void setupLocationReceiver() {
        // Listen for location updates from LocationService
        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double lat = intent.getDoubleExtra(
                        LocationService.EXTRA_LATITUDE, 0
                );
                double lng = intent.getDoubleExtra(
                        LocationService.EXTRA_LONGITUDE, 0
                );
                if (lat != 0 && lng != 0) {
                    userLat = lat;
                    userLng = lng;
                    // Only center once on first location fix
                    if (googleMap != null) {
                        centerOnUserLocation();
                    }
                }
            }
        };
    }

    private void setupClickListeners() {
        // My location FAB — re-center map on user
        binding.fabMyLocation.setOnClickListener(v -> centerOnUserLocation());
    }

    private void centerOnUserLocation() {
        if (googleMap == null) return;
        LatLng userLatLng = new LatLng(userLat, userLng);
        googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
        );
    }

    private void addMarkersToMap(List<HazardMarker> markers) {
        if (googleMap == null) return;

        // Clear existing markers
        googleMap.clear();
        markerDataMap.clear();

        for (HazardMarker hazard : markers) {
            LatLng position = new LatLng(
                    hazard.getLatitude(),
                    hazard.getLongitude()
            );

            // Get pin color based on risk level
            BitmapDescriptor icon = getMarkerIcon(hazard.getRiskLevel());

            MarkerOptions options = new MarkerOptions()
                    .position(position)
                    .title(hazard.getHazardName())
                    .icon(icon);

            Marker marker = googleMap.addMarker(options);
            if (marker != null) {
                // Store hazard data mapped to marker ID for bottom sheet
                markerDataMap.put(marker.getId(), hazard);
            }
        }
    }

    private BitmapDescriptor getMarkerIcon(String riskLevel) {
        // Create colored circular marker based on risk level
        int colorRes;
        switch (riskLevel != null ? riskLevel : "") {
            case "Low":      colorRes = R.color.severity_low;      break;
            case "Moderate": colorRes = R.color.severity_moderate; break;
            case "High":     colorRes = R.color.severity_high;     break;
            case "Critical": colorRes = R.color.severity_critical; break;
            default:         colorRes = R.color.severity_moderate; break;
        }

        // Draw a filled circle as the marker
        int size   = 48;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(ContextCompat.getColor(requireContext(), colorRes));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        // White border
        Paint borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(0xFFFFFFFF);
        borderPaint.setStrokeWidth(3f);
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 2, borderPaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void showHazardDetailBottomSheet(HazardMarker hazard) {
        View sheetView = getLayoutInflater().inflate(
                R.layout.bottom_sheet_hazard_detail, null
        );

        BottomSheetDialog dialog = new BottomSheetDialog(
                requireContext(),
                R.style.Theme_MDRRMO_BottomSheet
        );
        dialog.setContentView(sheetView);

        // Hazard name
        android.widget.TextView tvHazardName =
                sheetView.findViewById(R.id.tvHazardName);
        tvHazardName.setText(hazard.getHazardName());

        // Risk level badge with color
        android.widget.TextView tvRiskLevel =
                sheetView.findViewById(R.id.tvRiskLevel);
        tvRiskLevel.setText(hazard.getRiskLevel());
        setRiskLevelBadgeColor(tvRiskLevel, hazard.getRiskLevel());

        // Status badge
        android.widget.TextView tvStatus =
                sheetView.findViewById(R.id.tvStatus);
        tvStatus.setText(hazard.getStatus());
        setStatusBadgeColor(tvStatus, hazard.getStatus());

        // Location
        android.widget.TextView tvLocation =
                sheetView.findViewById(R.id.tvLocation);
        tvLocation.setText(hazard.getBrgyName());

        // Reporter
        android.widget.TextView tvReporter =
                sheetView.findViewById(R.id.tvReporter);
        tvReporter.setText(hazard.getUsername());

        // Date
        android.widget.TextView tvDate =
                sheetView.findViewById(R.id.tvDate);
        tvDate.setText(DateUtils.formatForDisplay(hazard.getReportDate()));

        // Distance from user
        android.widget.TextView tvDistance =
                sheetView.findViewById(R.id.tvDistance);
        float distance = LocationUtils.distanceBetween(
                userLat, userLng,
                hazard.getLatitude(), hazard.getLongitude()
        );
        tvDistance.setText(LocationUtils.formatDistance(distance));

        // Description
        if (hazard.getDescription() != null
                && !hazard.getDescription().isEmpty()) {
            sheetView.findViewById(R.id.layoutDescription)
                    .setVisibility(View.VISIBLE);
            android.widget.TextView tvDescription =
                    sheetView.findViewById(R.id.tvDescription);
            tvDescription.setText(hazard.getDescription());
        }

        // Photo — skip for now (local path not applicable for server markers)
        // Will be implemented in Step 9 with real image URLs

        dialog.show();
    }

    private void setRiskLevelBadgeColor(android.widget.TextView tv,
                                        String riskLevel) {
        int colorRes;
        switch (riskLevel != null ? riskLevel : "") {
            case "Low":      colorRes = R.color.severity_low;      break;
            case "Moderate": colorRes = R.color.severity_moderate; break;
            case "High":     colorRes = R.color.severity_high;     break;
            case "Critical": colorRes = R.color.severity_critical; break;
            default:         colorRes = R.color.severity_moderate; break;
        }
        tv.getBackground().setTint(
                ContextCompat.getColor(requireContext(), colorRes)
        );
    }

    private void setStatusBadgeColor(android.widget.TextView tv,
                                     String status) {
        int colorRes;
        switch (status != null ? status : "") {
            case "Verified":       colorRes = R.color.status_verified;       break;
            case "Auto-published": colorRes = R.color.status_auto_published; break;
            default:               colorRes = R.color.status_pending;        break;
        }
        tv.getBackground().setTint(
                ContextCompat.getColor(requireContext(), colorRes)
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register location receiver when fragment is visible
        requireActivity().registerReceiver(
                locationReceiver,
                new IntentFilter(LocationService.ACTION_LOCATION_UPDATE)
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister when fragment is not visible
        try {
            requireActivity().unregisterReceiver(locationReceiver);
        } catch (IllegalArgumentException ignored) {
            // Receiver was not registered
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}