package tech.apps.music.ui.fragments.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.apps.music.database.network.YoutubeRepository
import tech.apps.music.model.SongModelForList

class SearchViewModel : ViewModel() {

    private val searchSuggestion = YoutubeRepository()

    var suggestionList: List<String> = listOf()
    var listOfSearchResults: List<SongModelForList> = listOf()
    var statusOfSearchFrag: StatusOfSearchFrag = StatusOfSearchFrag.Suggest

    fun searchSuggestionText(
        text: String,
        context: Context,
        callback: (textArray: ArrayList<String>) -> Unit
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            callback(searchSuggestion.searchSuggestionWithKeywords(text, context))
        }
    }

}
enum class StatusOfSearchFrag {
    Results,
    Suggest,
}