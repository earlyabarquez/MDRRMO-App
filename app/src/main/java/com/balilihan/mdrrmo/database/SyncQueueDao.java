// database/SyncQueueDao.java
// Handles all database operations for the offline sync queue

package com.balilihan.mdrrmo.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.balilihan.mdrrmo.models.HazardReport;

import java.util.List;

@Dao
public interface SyncQueueDao {

    // Add a report to the sync queue
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addToQueue(HazardReport report);

    // Get all pending reports in the queue
    @Query("SELECT * FROM reports WHERE syncStatus = 'PENDING_UPLOAD' ORDER BY localId ASC")
    List<HazardReport> getPendingReports();

    // Count of pending reports — drives the sync badge
    @Query("SELECT COUNT(*) FROM reports WHERE syncStatus = 'PENDING_UPLOAD'")
    LiveData<Integer> getPendingCount();

    // Remove from queue after successful upload
    @Query("UPDATE reports SET syncStatus = 'UPLOADED' WHERE localId = :localId")
    void markAsUploaded(long localId);

    // Clear entire queue — used after bulk sync
    @Query("UPDATE reports SET syncStatus = 'UPLOADED' WHERE syncStatus = 'PENDING_UPLOAD'")
    void clearQueue();
}