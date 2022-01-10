package tech.apps.music.ui.fragments.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import tech.apps.music.database.Repository
import tech.apps.music.database.offline.WatchLaterSongModel
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    repository: Repository,
) : ViewModel() {

    val getWatchLaterList: LiveData<List<WatchLaterSongModel>> = repository.getListOfWatchLater()

}