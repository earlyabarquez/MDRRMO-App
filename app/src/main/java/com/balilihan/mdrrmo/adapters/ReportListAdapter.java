// adapters/ReportListAdapter.java

package com.balilihan.mdrrmo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.balilihan.mdrrmo.R;
import com.balilihan.mdrrmo.models.HazardReport;
import com.balilihan.mdrrmo.utils.DateUtils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class ReportListAdapter extends
        RecyclerView.Adapter<ReportListAdapter.ReportViewHolder> {

    public interface OnReportClickListener {
        void onReportClick(HazardReport report);
    }

    private       List<HazardReport>    reports;
    private final OnReportClickListener listener;
    private final Context               context;

    public ReportListAdapter(Context context,
                             List<HazardReport> reports,
                             OnReportClickListener listener) {
        this.context  = context;
        this.reports  = reports;
        this.listener = listener;
    }

    public void updateReports(List<HazardReport> newReports) {
        this.reports = newReports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_report_card, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder,
                                 int position) {
        holder.bind(reports.get(position));
    }

    @Override
    public int getItemCount() {
        return reports != null ? reports.size() : 0;
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {

        View      viewRiskBar;
        ImageView ivReportPhoto;
        TextView  tvHazardName;
        TextView  tvStatus;
        TextView  tvBrgyName;
        TextView  tvDate;
        TextView  tvRiskLevel;
        TextView  tvOfflineIndicator;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            viewRiskBar        = itemView.findViewById(R.id.viewRiskBar);
            ivReportPhoto      = itemView.findViewById(R.id.ivReportPhoto);
            tvHazardName       = itemView.findViewById(R.id.tvHazardName);
            tvStatus           = itemView.findViewById(R.id.tvStatus);
            tvBrgyName         = itemView.findViewById(R.id.tvBrgyName);
            tvDate             = itemView.findViewById(R.id.tvDate);
            tvRiskLevel        = itemView.findViewById(R.id.tvRiskLevel);
            tvOfflineIndicator = itemView.findViewById(R.id.tvOfflineIndicator);
        }

        void bind(HazardReport report) {

            // Hazard name
            tvHazardName.setText(report.getHazardName());

            // Barangay
            tvBrgyName.setText(report.getBrgyName());

            // Date
            tvDate.setText(DateUtils.formatForDisplay(report.getReportDate()));

            // Risk level badge
            tvRiskLevel.setText(report.getRiskLevel());
            int riskColor = getRiskColor(report.getRiskLevel());
            tvRiskLevel.getBackground().setTint(
                    ContextCompat.getColor(context, riskColor)
            );

            // Left risk bar
            viewRiskBar.setBackgroundColor(
                    ContextCompat.getColor(context, riskColor)
            );

            // Status badge
            tvStatus.setText(report.getStatus());
            tvStatus.getBackground().setTint(
                    ContextCompat.getColor(context,
                            getStatusColor(report.getStatus()))
            );

            // Offline indicator
            if ("PENDING_UPLOAD".equals(report.getSyncStatus())) {
                tvOfflineIndicator.setVisibility(View.VISIBLE);
            } else {
                tvOfflineIndicator.setVisibility(View.GONE);
            }

            // Photo thumbnail
            if (report.getImageUrl() != null) {
                Glide.with(context)
                        .load(new File(report.getImageUrl()))
                        .centerCrop()
                        .placeholder(R.drawable.ic_notification)
                        .into(ivReportPhoto);
            }

            // Click — open detail screen
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onReportClick(report);
            });
        }

        private int getRiskColor(String riskLevel) {
            switch (riskLevel != null ? riskLevel : "") {
                case "Low":      return R.color.severity_low;
                case "Moderate": return R.color.severity_moderate;
                case "High":     return R.color.severity_high;
                case "Critical": return R.color.severity_critical;
                default:         return R.color.severity_moderate;
            }
        }

        private int getStatusColor(String status) {
            switch (status != null ? status : "") {
                case "Verified":       return R.color.status_verified;
                case "Rejected":       return R.color.status_rejected;
                case "Auto-published": return R.color.status_auto_published;
                default:               return R.color.status_pending;
            }
        }
    }
}