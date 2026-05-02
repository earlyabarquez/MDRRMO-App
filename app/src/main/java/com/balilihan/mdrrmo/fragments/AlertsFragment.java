// fragments/AlertsFragment.java

package com.balilihan.mdrrmo.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.balilihan.mdrrmo.databinding.FragmentAlertsBinding;
import com.balilihan.mdrrmo.adapters.AlertListAdapter;
import com.balilihan.mdrrmo.models.HazardAlert;
import com.balilihan.mdrrmo.services.LocationService;
import com.balilihan.mdrrmo.utils.DateUtils;
import com.balilihan.mdrrmo.utils.LocationUtils;
import com.balilihan.mdrrmo.viewmodels.AlertsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.balilihan.mdrrmo.R;

import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class AlertsFragment extends Fragment {

    private FragmentAlertsBinding binding;
    private AlertsViewModel        viewModel;
    private AlertListAdapter       adapter;

    private double userLat = LocationUtils.BALILIHAN_LAT;
    private double userLng = LocationUtils.BALILIHAN_LNG;

    private BroadcastReceiver locationReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding   = FragmentAlertsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(AlertsViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupObservers();
        setupClickListeners();
        setupLocationReceiver();

        // Fetch alerts on load
        viewModel.fetchAlerts();
    }

    private void setupRecyclerView() {
        adapter = new AlertListAdapter(
                requireContext(),
                new ArrayList<>(),
                (alert, position) -> {
                    // Mark as read
                    viewModel.markAsRead(position);
                    // Show detail bottom sheet
                    showAlertDetailBottomSheet(alert);
                }
        );
        binding.rvAlerts.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observe alerts list
        viewModel.alerts.observe(getViewLifecycleOwner(), alerts -> {
            if (alerts == null || alerts.isEmpty()) {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.rvAlerts.setVisibility(View.GONE);
            } else {
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.rvAlerts.setVisibility(View.VISIBLE);
                adapter.updateUserLocation(userLat, userLng);

                // Refresh adapter with new list
                binding.rvAlerts.setAdapter(
                        new AlertListAdapter(
                                requireContext(),
                                alerts,
                                (alert, position) -> {
                                    viewModel.markAsRead(position);
                                    showAlertDetailBottomSheet(alert);
                                }
                        )
                );
            }
        });

        // Observe unread count — update banner
        viewModel.unreadCount.observe(getViewLifecycleOwner(), count -> {
            if (count > 0) {
                binding.tvUnreadBanner.setVisibility(View.VISIBLE);
                binding.tvUnreadBanner.setText(
                        count + " unread alert" + (count > 1 ? "s" : "")
                );
            } else {
                binding.tvUnreadBanner.setVisibility(View.GONE);
            }
        });

        // Loading state
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(
                    isLoading ? View.VISIBLE : View.GONE
            );
        });
    }

    private void setupClickListeners() {
        // Mark all as read
        binding.tvMarkAllRead.setOnClickListener(v ->
                viewModel.markAllAsRead()
        );
    }

    private void setupLocationReceiver() {
        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                userLat = intent.getDoubleExtra(
                        LocationService.EXTRA_LATITUDE, userLat
                );
                userLng = intent.getDoubleExtra(
                        LocationService.EXTRA_LONGITUDE, userLng
                );
                // Update distances in adapter
                adapter.updateUserLocation(userLat, userLng);
            }
        };
    }

    private void showAlertDetailBottomSheet(HazardAlert alert) {
        View sheetView = getLayoutInflater().inflate(
                R.layout.bottom_sheet_hazard_detail, null
        );

        BottomSheetDialog dialog = new BottomSheetDialog(
                requireContext(),
                R.style.Theme_MDRRMO_BottomSheet
        );
        dialog.setContentView(sheetView);

        // Hazard name
        TextView tvHazardName = sheetView.findViewById(R.id.tvHazardName);
        tvHazardName.setText(alert.getHazardName());

        // Risk level badge
        TextView tvRiskLevel = sheetView.findViewById(R.id.tvRiskLevel);
        tvRiskLevel.setText(alert.getRiskLevel());
        setRiskBadgeColor(tvRiskLevel, alert.getRiskLevel());

        // Status badge
        TextView tvStatus = sheetView.findViewById(R.id.tvStatus);
        tvStatus.setText(alert.getStatus());
        setStatusBadgeColor(tvStatus, alert.getStatus());

        // Location
        TextView tvLocation = sheetView.findViewById(R.id.tvLocation);
        tvLocation.setText(alert.getBrgyName());

        // Reporter
        TextView tvReporter = sheetView.findViewById(R.id.tvReporter);
        tvReporter.setText(alert.getUsername());

        // Date
        TextView tvDate = sheetView.findViewById(R.id.tvDate);
        tvDate.setText(DateUtils.formatForDisplay(alert.getReportDate()));

        // Distance
        TextView tvDistance = sheetView.findViewById(R.id.tvDistance);
        float distance = LocationUtils.distanceBetween(
                userLat, userLng,
                alert.getLatitude(), alert.getLongitude()
        );
        tvDistance.setText(LocationUtils.formatDistance(distance));

        // Description
        if (alert.getDescription() != null
                && !alert.getDescription().isEmpty()) {
            sheetView.findViewById(R.id.layoutDescription)
                    .setVisibility(View.VISIBLE);
            TextView tvDescription =
                    sheetView.findViewById(R.id.tvDescription);
            tvDescription.setText(alert.getDescription());
        }

        dialog.show();
    }

    private void setRiskBadgeColor(TextView tv, String riskLevel) {
        int colorRes;
        switch (riskLevel != null ? riskLevel : "") {
            case "Low":      colorRes = R.color.severity_low;      break;
            case "Moderate": colorRes = R.color.severity_moderate; break;
            case "High":     colorRes = R.color.severity_high;     break;
            case "Critical": colorRes = R.color.severity_critical; break;
            default:         colorRes = R.color.severity_moderate; break;
        }
        tv.getBackground().setTint(
                androidx.core.content.ContextCompat.getColor(
                        requireContext(), colorRes)
        );
    }

    private void setStatusBadgeColor(TextView tv, String status) {
        int colorRes;
        switch (status != null ? status : "") {
            case "Verified":       colorRes = R.color.status_verified;       break;
            case "Auto-published": colorRes = R.color.status_auto_published; break;
            default:               colorRes = R.color.status_pending;        break;
        }
        tv.getBackground().setTint(
                androidx.core.content.ContextCompat.getColor(
                        requireContext(), colorRes)
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().registerReceiver(
                locationReceiver,
                new IntentFilter(LocationService.ACTION_LOCATION_UPDATE)
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            requireActivity().unregisterReceiver(locationReceiver);
        } catch (IllegalArgumentException ignored) {}
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}