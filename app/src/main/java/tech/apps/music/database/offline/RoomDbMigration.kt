package tech.apps.music.database.offline

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomDbMigration {
    val migration_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // delete Table
            database.execSQL("DROP TABLE yt_video_table")
            database.execSQL("DROP TABLE yt_video_table_liked")

            //Create Table
            database.execSQL("CREATE TABLE IF NOT EXISTS History_Song_Model (videoId TEXT NOT NULL, Title TEXT NOT NULL, Channel TEXT NOT NULL, Duration INTEGER NOT NULL, Timing INTEGER NOT NULL, WatchedPosition INTEGER NOT NULL, PRIMARY KEY(videoId))")
            database.execSQL("CREATE TABLE IF NOT EXISTS WatchLater_Song_Model (videoId TEXT NOT NULL, Title TEXT NOT NULL, Channel TEXT NOT NULL, Duration INTEGER NOT NULL, Timing INTEGER NOT NULL, WatchedPosition INTEGER NOT NULL, PRIMARY KEY(videoId))")
        }
    }
    val migration_2_3 = object : Migration(2,3){
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS Search_History (queryText TEXT NOT NULL, Timing INTEGER NOT NULL, PRIMARY KEY(queryText))")
        }
    }

}
