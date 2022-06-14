package tech.apps.music.database.offline

import androidx.room.Database
import androidx.room.RoomDatabase
import tech.apps.music.model.SongModelForList

@Database(entities = [SongModelForList::class], version = 1)
abstract class CacheDatabase: RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}