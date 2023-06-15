package tech.apps.music.ui.fragments.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import tech.apps.music.database.CacheRepository
import tech.apps.music.model.SongModelForList
import tech.apps.music.util.Resource
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val cacheRepository: CacheRepository
) : ViewModel() {

    var searchSongResultList = cacheRepository.getListOfSongWithKeyword("")

    var searchSuggestionList: MutableLiveData<List<String>> = MutableLiveData()
    var suggestionList: List<String> = listOf()
    var statusOfSearchFrag: StatusOfSearchFrag = StatusOfSearchFrag.Suggest

    fun searchSuggestionText(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            searchSuggestionList.postValue(cacheRepository.searchSuggestionWithKeywords(text))
        }
    }

    fun searchSongResult(query: String):  Flow<Resource<out List<SongModelForList>>> {
        val songList = cacheRepository.getListOfSongWithKeyword(query)
        searchSongResultList = songList
        return songList
    }
}

enum class StatusOfSearchFrag {
    Results,
    Suggest,
}