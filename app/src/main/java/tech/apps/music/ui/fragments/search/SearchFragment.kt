package tech.apps.music.ui.fragments.search

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.search_fragment.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.apps.music.R
import tech.apps.music.adapters.SearchSuggestionAdapter
import tech.apps.music.adapters.SongAdapter
import tech.apps.music.database.network.YoutubeSearch
import tech.apps.music.others.Constants
import javax.inject.Inject

@DelicateCoroutinesApi
@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var viewModel: SearchViewModel

    @Inject
    lateinit var searchAdapter: SongAdapter

    lateinit var searchSuggestionAdapter: SearchSuggestionAdapter
    private var keyboardNeeded = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        return inflater.inflate(R.layout.search_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        voiceSearchViewSearch.setOnClickListener {
            startVoiceRecognitionActivity()
        }

        settingUpRecyclerView()
        searchAdapter.isViewHorizontal = true

        searchAdapter.setItemClickListener {
            val bundle = Bundle()
            bundle.putString(Constants.SEARCH_FRAGMENT_VIDEO_ID, it.videoId)
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2, bundle)
        }

        searchSuggestionAdapter.setItemClickListener {
            searchButtonViewSearchFragment.setQuery(it, true)
        }

        backButtonSearchFragment.setOnClickListener {
            findNavController().navigateUp()
        }

        searchButtonViewSearchFragment.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener,
                android.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        searchQuery(it)
                        searchButtonViewSearchFragment.clearFocus()
                        hideSearchSuggestion(false)

                        val firebaseAnalytics = Firebase.analytics
                        firebaseAnalytics.logEvent("Search_Box_Event") {
                            param("Searched_Query", it)
                        }
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    recyclerViewSearchSuggestion.visibility = View.VISIBLE
                    newText?.let {
                        hideSearchSuggestion(true)
                        viewModel.searchSuggestionText(it) { list ->
                            searchSuggestionAdapter.songs = list
                        }
                    }
                    return false
                }
            }
        )
        val keyword = arguments?.getString(Constants.PASS_EXPLORE_KEYWORDS)

        if (keyword != null) {
            searchButtonViewSearchFragment.setQuery(keyword, true)
            keyboardNeeded = false
        }
    }

    private fun settingUpRecyclerView() {

        searchSuggestionAdapter = SearchSuggestionAdapter()
        recyclerViewSearchSuggestion.apply {
            adapter = searchSuggestionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        recyclerViewSearchResult.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

    }

    private fun searchQuery(query: String) {
        progressBarSearchFragment.isVisible = true

        GlobalScope.launch(Dispatchers.IO) {
            YoutubeSearch().searchWithKeywords(query) {

                view?.findViewById<TextView>(R.id.textViewNotFoundSearch)?.isVisible = it.size == 0

                searchAdapter.songs = it
                view?.findViewById<ProgressBar>(R.id.progressBarSearchFragment)?.isVisible = false

            }
        }
    }

    private val REQUEST_CODE = 0

    private fun startVoiceRecognitionActivity() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice searching...")

        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            val matches: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let {
                    it?.get(0)
                }

            if (!matches.isNullOrEmpty()) {
                searchButtonViewSearchFragment.setQuery(matches.toString(), true)
                keyboardNeeded = false
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        if (keyboardNeeded) {
            searchButtonViewSearchFragment.isIconified = false
            keyboardNeeded = false
        }
    }

    private fun hideSearchSuggestion(isSearch: Boolean) {
        recyclerViewSearchSuggestion.isVisible = isSearch
        recyclerViewSearchResult.isVisible = !isSearch
    }
}