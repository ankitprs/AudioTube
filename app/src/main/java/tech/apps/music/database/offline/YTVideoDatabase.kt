package tech.apps.music.database.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [YTVideoLink::class,YTVideoLinkLiked::class],version = 1)
abstract class YTVideoDatabase :RoomDatabase(){

    abstract fun getYTVideoDao():YTVideoDao

    companion object{
        @Volatile
        private  var INSTANCE :YTVideoDatabase?=null

        fun getDatabase(context: Context):YTVideoDatabase{
            if(INSTANCE==null){
                synchronized(this){
                    INSTANCE= Room.databaseBuilder(
                        context.applicationContext,
                        YTVideoDatabase::class.java,
                        "SongDatabase"
                    ).build()
                }
            }
            return INSTANCE!!
        }
    }
}