package tech.apps.music.ui.fragments.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import tech.apps.music.database.Repository
import tech.apps.music.database.offline.HistorySongModel
import tech.apps.music.database.offline.WatchLaterSongModel
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    repository: Repository,
) : ViewModel() {

    val getRecentList: LiveData<List<HistorySongModel>> = repository.getAllSongsLiveData()
    val getWatchLaterList: LiveData<List<WatchLaterSongModel>> = repository.getListOfWatchLater()

}