package tech.apps.music.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object BasicStorage {
    var isNetworkConnected: LiveData<Boolean> = MutableLiveData()
    var lastTimeForShowingAds: Long = 0L
}