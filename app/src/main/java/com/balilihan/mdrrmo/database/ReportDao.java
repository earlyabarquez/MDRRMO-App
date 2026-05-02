package com.balilihan.mdrrmo.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.balilihan.mdrrmo.models.HazardReport;
import com.balilihan.mdrrmo.models.HazardType;

import java.util.List;

@Dao
public interface ReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertReport(HazardReport report);

    @Update
    void updateReport(HazardReport report);

    // Get reports by user — ordered newest first
    @Query("SELECT * FROM reports WHERE userId = :userId ORDER BY localId DESC")
    LiveData<List<HazardReport>> getReportsByUser(long userId);

    // Get pending upload reports for sync
    @Query("SELECT * FROM reports WHERE syncStatus = 'PENDING_UPLOAD'")
    List<HazardReport> getPendingUploadReports();

    // Count pending — shown in Reports tab
    @Query("SELECT COUNT(*) FROM reports WHERE syncStatus = 'PENDING_UPLOAD'")
    LiveData<Integer> getPendingUploadCount();

    @Query("SELECT * FROM reports WHERE localId = :localId")
    HazardReport getReportById(long localId);

    @Query("DELETE FROM reports WHERE localId = :localId")
    void deleteReport(long localId);

    // Hazard types
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHazardTypes(List<HazardType> types);

    @Query("SELECT * FROM hazard_types ORDER BY hazardName")
    LiveData<List<HazardType>> getAllHazardTypes();

    @Query("SELECT COUNT(*) FROM hazard_types")
    int getHazardTypeCount();
}