package com.balilihan.mdrrmo.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reports")
public class HazardReport implements java.io.Serializable{

    // Local Room ID — auto generated, only exists on device
    @PrimaryKey(autoGenerate = true)
    private long   localId;

    // Server-assigned ID after upload — -1 if not yet uploaded
    private long   reportId;

    // FK → users.user_id
    private long   userId;

    // Display name — from users.username
    private String username;

    // FK → hazard.hazard_id
    private int    hazardId;

    // Denormalized for display without extra join
    private String hazardName;

    // FK → barangay.brgy_id — auto detected from GPS
    private int    brgyId;
    private String brgyName;

    // FK → status.status_id
    private int    statusId;
    private String status;

    // New column to add in PostgreSQL — "Low", "Moderate", "High", "Critical"
    private String riskLevel;

    // Local file path before upload, server URL after upload
    private String imageUrl;

    // GPS coordinates — matches numeric(9,6) in DB
    private double latitude;
    private double longitude;

    // Optional reporter notes
    private String description;

    // Matches report_date in DB
    private String reportDate;

    // Admin verification info
    private String verifiedBy;
    private String verifiedAt;

    // Sync tracking — local only, not in PostgreSQL
    // "PENDING_UPLOAD" or "UPLOADED"
    private String syncStatus;

    // ── Getters ─────────────────────────────────────────────────
    public long   getLocalId()     { return localId; }
    public long   getReportId()    { return reportId; }
    public long   getUserId()      { return userId; }
    public String getUsername()    { return username; }
    public int    getHazardId()    { return hazardId; }
    public String getHazardName()  { return hazardName; }
    public int    getBrgyId()      { return brgyId; }
    public String getBrgyName()    { return brgyName; }
    public int    getStatusId()    { return statusId; }
    public String getStatus()      { return status; }
    public String getRiskLevel()   { return riskLevel; }
    public String getImageUrl()    { return imageUrl; }
    public double getLatitude()    { return latitude; }
    public double getLongitude()   { return longitude; }
    public String getDescription() { return description; }
    public String getReportDate()  { return reportDate; }
    public String getVerifiedBy()  { return verifiedBy; }
    public String getVerifiedAt()  { return verifiedAt; }
    public String getSyncStatus()  { return syncStatus; }

    // ── Setters ─────────────────────────────────────────────────
    public void setLocalId(long id)          { this.localId = id; }
    public void setReportId(long id)         { this.reportId = id; }
    public void setUserId(long id)           { this.userId = id; }
    public void setUsername(String name)     { this.username = name; }
    public void setHazardId(int id)          { this.hazardId = id; }
    public void setHazardName(String name)   { this.hazardName = name; }
    public void setBrgyId(int id)            { this.brgyId = id; }
    public void setBrgyName(String name)     { this.brgyName = name; }
    public void setStatusId(int id)          { this.statusId = id; }
    public void setStatus(String status)     { this.status = status; }
    public void setRiskLevel(String level)   { this.riskLevel = level; }
    public void setImageUrl(String url)      { this.imageUrl = url; }
    public void setLatitude(double lat)      { this.latitude = lat; }
    public void setLongitude(double lng)     { this.longitude = lng; }
    public void setDescription(String desc)  { this.description = desc; }
    public void setReportDate(String date)   { this.reportDate = date; }
    public void setVerifiedBy(String by)     { this.verifiedBy = by; }
    public void setVerifiedAt(String at)     { this.verifiedAt = at; }
    public void setSyncStatus(String status) { this.syncStatus = status; }
}