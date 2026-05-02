// report/ReportPreviewActivity.java

package com.balilihan.mdrrmo.report;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.balilihan.mdrrmo.R;
import com.balilihan.mdrrmo.activities.MainActivity;
import com.balilihan.mdrrmo.database.AppDatabase;
import com.balilihan.mdrrmo.databinding.ActivityReportPreviewBinding;
import com.balilihan.mdrrmo.models.HazardReport;
import com.balilihan.mdrrmo.utils.DateUtils;
import com.bumptech.glide.Glide;

import java.io.File;

public class ReportPreviewActivity extends AppCompatActivity {

    private ActivityReportPreviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long    reportId  = getIntent().getLongExtra(
                ReportFormActivity.EXTRA_REPORT_ID, -1
        );
        boolean isOffline = getIntent().getBooleanExtra("is_offline", false);

        // Show offline warning if saved without internet
        if (isOffline) {
            binding.cardOfflineWarning.setVisibility(View.VISIBLE);
            binding.cardSuccess.setCardBackgroundColor(
                    getColor(R.color.status_pending)
            );
            binding.tvSuccessTitle.setText("Saved offline");
            binding.tvSuccessSubtitle.setText(
                    "Will upload automatically when internet is restored"
            );
        }

        // Load report from Room on background thread
        new Thread(() -> {
            AppDatabase  db     = AppDatabase.getInstance(this);
            HazardReport report = db.reportDao().getReportById(reportId);

            runOnUiThread(() -> {
                if (report != null) populateUI(report);
            });
        }).start();

        // Done button — clear back stack and go to Map
        binding.btnDone.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            );
            startActivity(intent);
            finish();
        });
    }

    private void populateUI(HazardReport report) {

        // Photo — imageUrl is local file path before upload
        if (report.getImageUrl() != null) {
            Glide.with(this)
                    .load(new File(report.getImageUrl()))
                    .centerCrop()
                    .into(binding.ivPhoto);
        }

        // Hazard type — matches hazard.hazard_name
        binding.tvHazardType.setText(report.getHazardName());

        // Location — brgyName from barangay table
        binding.tvLocation.setText(report.getBrgyName());

        // Timestamp — matches report_date in DB
        binding.tvSubmittedAt.setText(
                DateUtils.formatForDisplay(report.getReportDate())
        );

        // Risk level with color — matches riskLevel field
        binding.tvSeverity.setText(report.getRiskLevel());
        int colorRes;
        switch (report.getRiskLevel() != null ? report.getRiskLevel() : "") {
            case "Low":      colorRes = R.color.severity_low;      break;
            case "Moderate": colorRes = R.color.severity_moderate; break;
            case "High":     colorRes = R.color.severity_high;     break;
            case "Critical": colorRes = R.color.severity_critical; break;
            default:         colorRes = R.color.text_secondary;    break;
        }
        binding.tvSeverity.setTextColor(getColor(colorRes));

        // Description — only show if not empty
        if (report.getDescription() != null
                && !report.getDescription().isEmpty()) {
            binding.layoutDescription.setVisibility(View.VISIBLE);
            binding.tvDescription.setText(report.getDescription());
        }
    }
}