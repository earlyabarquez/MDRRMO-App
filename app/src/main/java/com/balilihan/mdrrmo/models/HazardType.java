package com.balilihan.mdrrmo.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "hazard_types")
public class HazardType {

    @PrimaryKey
    private int    hazardId;
    private String hazardName;

    public HazardType() {}

    public HazardType(int hazardId, String hazardName) {
        this.hazardId   = hazardId;
        this.hazardName = hazardName;
    }

    public int    getHazardId()   { return hazardId; }
    public String getHazardName() { return hazardName; }

    public void setHazardId(int id)        { this.hazardId = id; }
    public void setHazardName(String name) { this.hazardName = name; }
}