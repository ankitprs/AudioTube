package tech.apps.music.database.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    version = 3,
    entities = [
        HistorySongModel::class,
        WatchLaterSongModel::class,
        SearchHistory::class
    ]
)
abstract class OfflineDatabase : RoomDatabase() {

    abstract fun getYTVideoDao(): YTVideoDao

    companion object {
        @Volatile
        private var INSTANCE: OfflineDatabase? = null

        fun getDatabase(context: Context): OfflineDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        OfflineDatabase::class.java,
                        "SongDatabase"
                    )
                        .addMigrations(RoomDbMigration.migration_1_2, RoomDbMigration.migration_2_3)
                        .build()
                }
            }
            return INSTANCE!!
        }
    }
}