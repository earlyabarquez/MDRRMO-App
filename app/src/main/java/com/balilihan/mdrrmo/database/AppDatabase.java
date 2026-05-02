// database/AppDatabase.java

package com.balilihan.mdrrmo.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.balilihan.mdrrmo.models.HazardReport;
import com.balilihan.mdrrmo.models.HazardType;
import com.balilihan.mdrrmo.database.ReportDao;

// List all entity classes and set the version number.
// Every time you change a model (add/remove a field),
// increment the version number and write a migration.
@Database(
        entities = { HazardReport.class, HazardType.class },
        version  = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // Only one instance of the database ever exists
    private static volatile AppDatabase instance;

    // Access all DAO operations through this
    public abstract ReportDao reportDao();
    public abstract SyncQueueDao syncQueueDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "mdrrmo_database"  // database file name on device
                            )
                            // fallbackToDestructiveMigration: if version changes
                            // and no migration is written, wipe and rebuild.
                            // Safe for development — remove before production.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}