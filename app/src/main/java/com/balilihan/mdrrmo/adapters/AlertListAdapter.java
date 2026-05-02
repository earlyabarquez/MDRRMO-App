// adapters/AlertListAdapter.java

package com.balilihan.mdrrmo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.balilihan.mdrrmo.R;
import com.balilihan.mdrrmo.models.HazardAlert;
import com.balilihan.mdrrmo.utils.DateUtils;
import com.balilihan.mdrrmo.utils.LocationUtils;

import java.util.List;

public class AlertListAdapter extends
        RecyclerView.Adapter<AlertListAdapter.AlertViewHolder> {

    public interface OnAlertClickListener {
        void onAlertClick(HazardAlert alert, int position);
    }

    private final List<HazardAlert>     alerts;
    private final OnAlertClickListener  listener;
    private final Context               context;
    private       double                userLat;
    private       double                userLng;

    public AlertListAdapter(Context context,
                            List<HazardAlert> alerts,
                            OnAlertClickListener listener) {
        this.context  = context;
        this.alerts   = alerts;
        this.listener = listener;
        this.userLat  = LocationUtils.BALILIHAN_LAT;
        this.userLng  = LocationUtils.BALILIHAN_LNG;
    }

    // Update user location for distance calculation
    public void updateUserLocation(double lat, double lng) {
        this.userLat = lat;
        this.userLng = lng;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_alert_card, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder,
                                 int position) {
        HazardAlert alert = alerts.get(position);
        holder.bind(alert, position);
    }

    @Override
    public int getItemCount() { return alerts.size(); }

    class AlertViewHolder extends RecyclerView.ViewHolder {

        View     viewRiskBar;
        View     viewUnreadDot;
        TextView tvHazardName;
        TextView tvRiskLevel;
        TextView tvBrgyName;
        TextView tvDate;
        TextView tvDistance;

        AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            viewRiskBar   = itemView.findViewById(R.id.viewRiskBar);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
            tvHazardName  = itemView.findViewById(R.id.tvHazardName);
            tvRiskLevel   = itemView.findViewById(R.id.tvRiskLevel);
            tvBrgyName    = itemView.findViewById(R.id.tvBrgyName);
            tvDate        = itemView.findViewById(R.id.tvDate);
            tvDistance    = itemView.findViewById(R.id.tvDistance);
        }

        void bind(HazardAlert alert, int position) {

            // Hazard name
            tvHazardName.setText(alert.getHazardName());

            // Barangay
            tvBrgyName.setText(alert.getBrgyName());

            // Date — formatted for display
            tvDate.setText(DateUtils.formatForDisplay(alert.getReportDate()));

            // Distance from user
            float distance = LocationUtils.distanceBetween(
                    userLat, userLng,
                    alert.getLatitude(), alert.getLongitude()
            );
            tvDistance.setText(LocationUtils.formatDistance(distance));

            // Risk level badge
            tvRiskLevel.setText(alert.getRiskLevel());
            int riskColor = getRiskColor(alert.getRiskLevel());
            tvRiskLevel.getBackground().setTint(
                    ContextCompat.getColor(context, riskColor)
            );

            // Left risk bar color
            viewRiskBar.setBackgroundColor(
                    ContextCompat.getColor(context, riskColor)
            );

            // Unread indicator — blue dot visible if not read
            viewUnreadDot.setVisibility(
                    alert.isRead() ? View.GONE : View.VISIBLE
            );

            // Card background — slightly tinted if unread
            itemView.setBackgroundColor(
                    ContextCompat.getColor(context,
                            alert.isRead() ? R.color.surface : R.color.primary_light)
            );

            // Click listener — mark as read + open detail
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAlertClick(alert, position);
                }
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
    }
}