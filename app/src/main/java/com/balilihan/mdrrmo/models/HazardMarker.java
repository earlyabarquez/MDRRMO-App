// models/HazardMarker.java
// Lightweight model for map pins — fetched from API
// Contains only what the map needs to display pins

package com.balilihan.mdrrmo.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HazardMarker {

    // Server report ID
    private long   reportId;

    // Hazard info
    private String hazardName;   // e.g. "Flood"
    private String riskLevel;    // "Low", "Moderate", "High", "Critical"
    private String status;       // "Verified", "Auto-published"

    // Location
    private double latitude;
    private double longitude;
    private String brgyName;     // barangay name for display

    // Reporter info
    private String username;
    private String reportDate;

    // Photo URL — server path
    private String imageUrl;

    // Description
    private String description;
}