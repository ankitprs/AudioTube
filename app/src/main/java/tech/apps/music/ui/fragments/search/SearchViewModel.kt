package tech.apps.music.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.apps.music.database.network.SearchSuggestion

class SearchViewModel : ViewModel() {

    fun searchSuggestionText(text: String, callback: (textArray: ArrayList<String>) -> Unit) {

        viewModelScope.launch(Dispatchers.IO) {
            SearchSuggestion().searchWithKeywords(text) {
                callback(it)
            }
        }
    }

}