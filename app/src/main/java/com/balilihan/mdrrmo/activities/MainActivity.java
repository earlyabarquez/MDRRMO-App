// activities/MainActivity.java

package com.balilihan.mdrrmo.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.balilihan.mdrrmo.R;
import com.balilihan.mdrrmo.databinding.ActivityMainBinding;
import com.balilihan.mdrrmo.services.LocationService;
import com.balilihan.mdrrmo.utils.PermissionUtils;
import com.balilihan.mdrrmo.utils.SessionManager;
import com.balilihan.mdrrmo.viewmodels.AlertsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController        navController;
    private SessionManager       session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        session = SessionManager.getInstance(this);
        setContentView(binding.getRoot());

        // Hide default action bar — we use bottom nav instead
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupNavigation();
        setupFab();
        requestPermissions();
        startLocationService();
    }

    private void startLocationService() {
        // Start foreground location service for continuous GPS tracking
        // and 200m hazard alert radius
        if (PermissionUtils.hasLocationPermission(this)) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }

    private void setupNavigation() {
        // Get NavController from the NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.navHostFragment);

        navController = navHostFragment.getNavController();

        // Connect bottom nav to navigation controller
        // This automatically handles tab switching and back stack
        NavigationUI.setupWithNavController(
                binding.bottomNav,
                navController
        );

        // Hide the placeholder item in the center (FAB gap)
        binding.bottomNav.getMenu()
                .findItem(R.id.placeholder)
                .setEnabled(false);

        // Show unread badge on Alerts tab
        // BadgeDrawable appears on the bell icon
        com.google.android.material.badge.BadgeDrawable badge =
                binding.bottomNav.getOrCreateBadge(R.id.alertsFragment);
        badge.setVisible(false); // hidden by default

        // Update badge when unread count changes
        // AlertsViewModel is shared across the activity
        new ViewModelProvider(this)
                .get(AlertsViewModel.class)
                .unreadCount
                .observe(this, count -> {
                    if (count != null && count > 0) {
                        badge.setVisible(true);
                        badge.setNumber(count);
                    } else {
                        badge.setVisible(false);
                    }
                });

        // Hide bottom nav and FAB on report detail screen
        // Full screen experience when viewing a single report
        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) -> {
                    if (destination.getId() == R.id.reportDetailFragment) {
                        binding.bottomNav.setVisibility(View.GONE);
                        binding.fab.setVisibility(View.GONE);
                    } else {
                        binding.bottomNav.setVisibility(View.VISIBLE);
                        binding.fab.setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    private void setupFab() {
        binding.fab.setOnClickListener(v -> showReportBottomSheet());
    }

    private void showReportBottomSheet() {
        // Check location permission before showing bottom sheet
        if (!PermissionUtils.hasLocationPermission(this)) {
            PermissionUtils.requestLocationPermission(this);
            return;
        }

        // Check camera permission
        if (!PermissionUtils.hasCameraPermission(this)) {
            PermissionUtils.requestCameraPermission(this);
            return;
        }

        // Inflate bottom sheet layout
        View sheetView = getLayoutInflater()
                .inflate(R.layout.bottom_sheet_report, null);

        BottomSheetDialog dialog = new BottomSheetDialog(
                this, R.style.Theme_MDRRMO_BottomSheet
        );
        dialog.setContentView(sheetView);

        // Populate reporter name from session
        androidx.appcompat.widget.AppCompatTextView tvReporterName =
                sheetView.findViewById(R.id.tvReporterName);
        tvReporterName.setText(session.getUsername());

        // Show loading state while GPS fetches coordinates
        androidx.appcompat.widget.AppCompatTextView tvCoordinates =
                sheetView.findViewById(R.id.tvCoordinates);
        androidx.appcompat.widget.AppCompatTextView tvAddress =
                sheetView.findViewById(R.id.tvAddress);

        // Fetch current location and display it
        fetchCurrentLocation(tvCoordinates, tvAddress);

        // Proceed to camera button
        sheetView.findViewById(R.id.btnProceedToCamera)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    startActivity(
                            new Intent(this,
                                    com.balilihan.mdrrmo.report.ReportCameraActivity.class)
                    );
                });

        // Cancel button
        sheetView.findViewById(R.id.btnCancel)
                .setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void fetchCurrentLocation(
            androidx.appcompat.widget.AppCompatTextView tvCoords,
            androidx.appcompat.widget.AppCompatTextView tvAddress) {

        // Uses FusedLocationProvider to get last known location
        // Works offline — GPS doesn't need internet
        com.google.android.gms.location.FusedLocationProviderClient client =
                com.google.android.gms.location.LocationServices
                        .getFusedLocationProviderClient(this);

        if (androidx.core.app.ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return;
        }

        client.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // Format coordinates to 6 decimal places
                String coords = String.format(
                        "%.6f°N, %.6f°E",
                        location.getLatitude(),
                        location.getLongitude()
                );
                tvCoords.setText(coords);

                // Reverse geocode to get human-readable address
                android.location.Geocoder geocoder =
                        new android.location.Geocoder(this,
                                java.util.Locale.getDefault());
                try {
                    java.util.List<android.location.Address> addresses =
                            geocoder.getFromLocation(
                                    location.getLatitude(),
                                    location.getLongitude(), 1
                            );
                    if (addresses != null && !addresses.isEmpty()) {
                        android.location.Address address = addresses.get(0);
                        // Show barangay + municipality
                        String readable = address.getSubLocality() != null
                                ? address.getSubLocality() + ", "
                                + address.getLocality()
                                : address.getLocality();
                        tvAddress.setText(readable);
                    }
                } catch (Exception e) {
                    tvAddress.setText("Balilihan, Bohol");
                }
            } else {
                tvCoords.setText("Acquiring GPS...");
                tvAddress.setText("Please wait");
            }
        });
    }

    private void requestPermissions() {
        // Request all needed permissions on first launch
        if (!PermissionUtils.hasLocationPermission(this)) {
            PermissionUtils.requestLocationPermission(this);
        }
        if (!PermissionUtils.hasNotificationPermission(this)) {
            PermissionUtils.requestNotificationPermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults
        );

        if (requestCode == PermissionUtils.REQUEST_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location granted — start service now
                startLocationService();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp()
                || super.onSupportNavigateUp();
    }
}