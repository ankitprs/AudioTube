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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.apps.music.R
import tech.apps.music.adapters.SearchSuggestionAdapter
import tech.apps.music.adapters.SongAdapter
import tech.apps.music.database.network.YoutubeSearch
import tech.apps.music.databinding.SearchFragmentBinding
import tech.apps.music.model.toYtAudioDataModel
import tech.apps.music.others.Constants
import tech.apps.music.ui.fragments.MainViewModel
import javax.inject.Inject

@DelicateCoroutinesApi
@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var viewModel: SearchViewModel

    @Inject
    lateinit var searchAdapter: SongAdapter

    lateinit var searchSuggestionAdapter: SearchSuggestionAdapter
    private var keyboardNeeded = true
    private var _binding: SearchFragmentBinding? = null
    private val binding: SearchFragmentBinding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        _binding = SearchFragmentBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        binding.voiceSearchViewSearch.setOnClickListener {
            startVoiceRecognitionActivity()
        }

        settingUpRecyclerView()

        searchAdapter.setItemClickListener {
            mainViewModel.changeIsYoutubeVideoCurSong(true)
            mainViewModel.playOrToggleListOfSongs((listOf(it)).toYtAudioDataModel(),true,0)
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2)
        }

        binding.floatingActionButtonPlayListSearchFrg.setOnClickListener {
            if(searchAdapter.songs.isEmpty()){
                Snackbar.make(it,"Nothing To Play... Search Something For Play",Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mainViewModel.changeIsYoutubeVideoCurSong(true)
            mainViewModel.playOrToggleListOfSongs(searchAdapter.songs.toYtAudioDataModel(),true,0)
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2)
        }

        searchSuggestionAdapter.setItemClickListener {
            binding.searchButtonViewSearchFragment.setQuery(it, true)
        }

        binding.backButtonSearchFragment.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.searchButtonViewSearchFragment.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener,
                android.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        searchQuery(it)
                        binding.searchButtonViewSearchFragment.clearFocus()
                        hideSearchSuggestion(false)

                        val firebaseAnalytics = Firebase.analytics
                        firebaseAnalytics.logEvent("Search_Box_Event") {
                            param("Searched_Query", it)
                        }
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    binding.recyclerViewSearchSuggestion.visibility = View.VISIBLE
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
            binding.searchButtonViewSearchFragment.setQuery(keyword, false)
//            keyboardNeeded = false
            arguments = null
        }
    }

    private fun settingUpRecyclerView() {

        searchSuggestionAdapter = SearchSuggestionAdapter()
        binding.recyclerViewSearchSuggestion.apply {
            adapter = searchSuggestionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.recyclerViewSearchResult.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

    }

    private fun searchQuery(query: String) {
        binding.progressBarSearchFragment.isVisible = true

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
                binding.searchButtonViewSearchFragment.setQuery(matches.toString(), true)
                keyboardNeeded = false
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        if (keyboardNeeded) {
            binding.searchButtonViewSearchFragment.isIconified = false
            keyboardNeeded = false
        }
        hideSearchSuggestion(false)
    }

    private fun hideSearchSuggestion(isSearch: Boolean) {
        binding.recyclerViewSearchSuggestion.isVisible = isSearch
        binding.recyclerViewSearchResult.isVisible = !isSearch
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewSearchResult.adapter = null
        binding.recyclerViewSearchSuggestion.adapter = null
        _binding = null
    }
}