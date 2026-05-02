// models/HazardAlert.java
// Represents a single alert item in the Alerts tab
// Fetched from API — shows all active hazards in Balilihan

package com.balilihan.mdrrmo.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HazardAlert {

    // Server report ID
    private long   reportId;

    // Hazard info
    private String hazardName;   // e.g. "Flood"
    private String riskLevel;    // "Low", "Moderate", "High", "Critical"
    private String status;       // "Verified", "Auto-published"

    // Location
    private double latitude;
    private double longitude;
    private String brgyName;

    // Reporter info
    private String username;
    private String reportDate;

    // Description
    private String description;

    // Read state — managed locally on device
    // Not stored in DB — resets when app is cleared
    private boolean isRead = false;
}