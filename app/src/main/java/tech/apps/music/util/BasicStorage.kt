package tech.apps.music.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tech.apps.music.others.Constants

object BasicStorage {

    fun getLastSongData(context: Context): Pair<String?,Long>{
        val sharedPref = context.getSharedPreferences(
            Constants.LAST_SONG_DATA,
            AppCompatActivity.MODE_PRIVATE
        )
        val mediaId: String? = sharedPref.getString(Constants.LAST_SONG_DATA_MEDIA_ID,null)
        val position: Long = sharedPref.getLong(Constants.LAST_SONG_DATA_POSITION, 0L)
        return Pair(mediaId,position)
    }

    fun setLastSongPosition(context: Context,position: Long, mediaId: String){
        val sharedPref = context.getSharedPreferences(
            Constants.LAST_SONG_DATA,
            AppCompatActivity.MODE_PRIVATE
        )

        val sharedPrefEditor = sharedPref.edit()
        sharedPrefEditor.putLong(Constants.LAST_SONG_DATA_POSITION, position)
        sharedPrefEditor.putString(Constants.LAST_SONG_DATA_MEDIA_ID, mediaId)
        sharedPrefEditor.apply()
    }

    var isNetworkConnected: LiveData<Boolean> = MutableLiveData()
}