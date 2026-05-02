// fragments/ReportDetailFragment.java

package com.balilihan.mdrrmo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.balilihan.mdrrmo.R;
import com.balilihan.mdrrmo.databinding.FragmentReportDetailBinding;
import com.balilihan.mdrrmo.models.HazardReport;
import com.balilihan.mdrrmo.utils.DateUtils;
import com.bumptech.glide.Glide;

import java.io.File;

public class ReportDetailFragment extends Fragment {

    private FragmentReportDetailBinding binding;
    private HazardReport                report;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReportDetailBinding.inflate(
                inflater, container, false
        );
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get report passed from ReportsFragment via Bundle
        if (getArguments() != null) {
            report = (HazardReport) getArguments()
                    .getSerializable("report");
        }

        if (report != null) {
            populateUI();
        }

        // Back button
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );
    }

    private void populateUI() {

        // Photo
        if (report.getImageUrl() != null) {
            binding.cardPhoto.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(new File(report.getImageUrl()))
                    .centerCrop()
                    .into(binding.ivPhoto);
        }

        // Report details
        binding.tvHazardType.setText(report.getHazardName());
        binding.tvLocation.setText(report.getBrgyName());
        binding.tvDate.setText(
                DateUtils.formatForDisplay(report.getReportDate())
        );
        binding.tvReporter.setText(report.getUsername());

        // Risk level with color
        binding.tvRiskLevel.setText(report.getRiskLevel());
        binding.tvRiskLevel.setTextColor(
                ContextCompat.getColor(requireContext(),
                        getRiskColor(report.getRiskLevel()))
        );

        // Description
        if (report.getDescription() != null
                && !report.getDescription().isEmpty()) {
            binding.cardDescription.setVisibility(View.VISIBLE);
            binding.tvDescription.setText(report.getDescription());
        }

        // Verified by
        if (report.getVerifiedBy() != null
                && !report.getVerifiedBy().isEmpty()) {
            binding.layoutVerifiedBy.setVisibility(View.VISIBLE);
            binding.tvVerifiedBy.setText(report.getVerifiedBy());
        }

        // Status tracker
        updateStatusTracker(report.getStatus());
    }

    private void updateStatusTracker(String status) {
        // Step 1 — Submitted — always complete
        binding.tvStep1Icon.setBackground(
                ContextCompat.getDrawable(requireContext(),
                        R.drawable.circle_severity_low)
        );

        switch (status != null ? status : "") {
            case "Verified":
                // All steps complete — green
                binding.tvStep2Icon.setBackground(
                        ContextCompat.getDrawable(requireContext(),
                                R.drawable.circle_severity_low)
                );
                binding.tvStep3Icon.setText("✓");
                binding.tvStep3Icon.setBackground(
                        ContextCompat.getDrawable(requireContext(),
                                R.drawable.circle_severity_low)
                );
                binding.tvStep3Label.setText("Verified");
                binding.tvStep3Label.setTextColor(
                        ContextCompat.getColor(requireContext(),
                                R.color.status_verified)
                );
                break;

            case "Rejected":
                // Step 3 — red
                binding.tvStep2Icon.setBackground(
                        ContextCompat.getDrawable(requireContext(),
                                R.drawable.circle_severity_low)
                );
                binding.tvStep3Icon.setText("✗");
                binding.tvStep3Icon.setBackground(
                        ContextCompat.getDrawable(requireContext(),
                                R.drawable.circle_severity_critical)
                );
                binding.tvStep3Label.setText("Rejected");
                binding.tvStep3Label.setTextColor(
                        ContextCompat.getColor(requireContext(),
                                R.color.status_rejected)
                );
                break;

            default:
                // Pending — step 2 amber, step 3 gray
                binding.tvStep2Icon.setBackground(
                        ContextCompat.getDrawable(requireContext(),
                                R.drawable.circle_severity_moderate)
                );
                binding.tvStep3Icon.setText("?");
                binding.tvStep3Label.setText("Pending");
                break;
        }
    }

    private int getRiskColor(String riskLevel) {
        switch (riskLevel != null ? riskLevel : "") {
            case "Low":      return R.color.severity_low;
            case "Moderate": return R.color.severity_moderate;
            case "High":     return R.color.severity_high;
            case "Critical": return R.color.severity_critical;
            default:         return R.color.text_secondary;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}