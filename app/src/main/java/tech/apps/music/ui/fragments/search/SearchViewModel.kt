package tech.apps.music.ui.fragments.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.apps.music.database.network.YoutubeRepository

class SearchViewModel : ViewModel() {

    private val searchSuggestion = YoutubeRepository()

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