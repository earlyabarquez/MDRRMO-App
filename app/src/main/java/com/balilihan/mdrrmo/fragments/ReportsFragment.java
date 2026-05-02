// fragments/ReportsFragment.java

package com.balilihan.mdrrmo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.balilihan.mdrrmo.R;
import com.balilihan.mdrrmo.adapters.ReportListAdapter;
import com.balilihan.mdrrmo.databinding.FragmentReportsBinding;
import com.balilihan.mdrrmo.models.HazardReport;
import com.balilihan.mdrrmo.services.SyncService;
import com.balilihan.mdrrmo.viewmodels.ReportsViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class ReportsFragment extends Fragment {

    private FragmentReportsBinding binding;
    private ReportsViewModel        viewModel;
    private ReportListAdapter       adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding   = FragmentReportsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ReportsViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFilterButtons();
        setupObservers();
        setupClickListeners();

        viewModel.fetchReports();
    }

    private void setupRecyclerView() {
        adapter = new ReportListAdapter(
                requireContext(),
                new ArrayList<>(),
                report -> navigateToDetail(report)
        );
        binding.rvReports.setAdapter(adapter);
    }

    private void setupFilterButtons() {
        // Set All as default active
        setActiveFilter(binding.btnFilterAll,
                ReportsViewModel.FILTER_ALL);

        binding.btnFilterAll.setOnClickListener(v -> {
            setActiveFilter(binding.btnFilterAll,
                    ReportsViewModel.FILTER_ALL);
            viewModel.setFilter(ReportsViewModel.FILTER_ALL);
        });

        binding.btnFilterPending.setOnClickListener(v -> {
            setActiveFilter(binding.btnFilterPending,
                    ReportsViewModel.FILTER_PENDING);
            viewModel.setFilter(ReportsViewModel.FILTER_PENDING);
        });

        binding.btnFilterVerified.setOnClickListener(v -> {
            setActiveFilter(binding.btnFilterVerified,
                    ReportsViewModel.FILTER_VERIFIED);
            viewModel.setFilter(ReportsViewModel.FILTER_VERIFIED);
        });

        binding.btnFilterRejected.setOnClickListener(v -> {
            setActiveFilter(binding.btnFilterRejected,
                    ReportsViewModel.FILTER_REJECTED);
            viewModel.setFilter(ReportsViewModel.FILTER_REJECTED);
        });
    }

    private void setActiveFilter(MaterialButton activeBtn, String filter) {
        // Reset all buttons to outlined style
        MaterialButton[] buttons = {
                binding.btnFilterAll,
                binding.btnFilterPending,
                binding.btnFilterVerified,
                binding.btnFilterRejected
        };
        for (MaterialButton btn : buttons) {
            btn.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.surface)
            );
            btn.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.text_secondary)
            );
        }

        // Highlight active button
        activeBtn.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.primary)
        );
        activeBtn.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.surface)
        );
    }

    private void setupObservers() {
        // Observe filtered reports
        viewModel.filteredReports.observe(getViewLifecycleOwner(), reports -> {
            if (reports == null || reports.isEmpty()) {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.rvReports.setVisibility(View.GONE);

                // Update empty message based on active filter
                String filter = viewModel.activeFilter.getValue();
                if (filter != null && !filter.equals(ReportsViewModel.FILTER_ALL)) {
                    binding.tvEmptyTitle.setText("No " + filter.toLowerCase()
                            + " reports");
                } else {
                    binding.tvEmptyTitle.setText("No reports yet");
                }
            } else {
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.rvReports.setVisibility(View.VISIBLE);
                adapter.updateReports(reports);
                binding.tvTotalCount.setText(reports.size() + " reports");
            }
        });

        // Observe pending sync count
        viewModel.pendingCount.observe(getViewLifecycleOwner(), count -> {
            if (count != null && count > 0) {
                binding.layoutSyncBanner.setVisibility(View.VISIBLE);
                binding.tvPendingCount.setText(
                        count + " report" + (count > 1 ? "s" : "")
                                + " pending upload"
                );
            } else {
                binding.layoutSyncBanner.setVisibility(View.GONE);
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
        // Manual sync button
        binding.tvSyncNow.setOnClickListener(v -> {
            SyncService.scheduleSyncNow(requireContext());
            com.google.android.material.snackbar.Snackbar.make(
                    binding.getRoot(),
                    "Syncing reports...",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show();
        });
    }

    private void navigateToDetail(HazardReport report) {
        // Pass report to detail fragment via Bundle
        Bundle args = new Bundle();
        args.putSerializable("report", report);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_reports_to_detail, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}