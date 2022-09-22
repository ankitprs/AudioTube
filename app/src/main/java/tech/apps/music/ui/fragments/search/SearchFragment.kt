package tech.apps.music.ui.fragments.search

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.apps.music.R
import tech.apps.music.adapters.SearchSuggestionAdapter
import tech.apps.music.adapters.SongAdapter
import tech.apps.music.databinding.SearchFragmentBinding
import tech.apps.music.model.toYtAudioDataModel
import tech.apps.music.Constants
import tech.apps.music.ui.fragments.MainViewModel
import tech.apps.music.util.Resource
import javax.inject.Inject

@DelicateCoroutinesApi
@AndroidEntryPoint
class SearchFragment : Fragment() {


    @Inject
    lateinit var songAdapter: SongAdapter
    private lateinit var viewModel: SearchViewModel
    lateinit var searchSuggestionAdapter: SearchSuggestionAdapter
    private var keyboardNeeded = true
    private var _binding: SearchFragmentBinding? = null
    private val binding: SearchFragmentBinding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private var isResultShowing: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]
        _binding = SearchFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(
            requireActivity(),
            R.color.dark_background
        )

        // voiceSearch
        binding.voiceSearchViewSearch.setOnClickListener {
            startVoiceRecognitionActivity()
        }

        settingUpRecyclerView()

        songAdapter.setItemClickListener { _, position ->
            mainViewModel.playOrToggleListOfSongs(
                songAdapter.songs.toYtAudioDataModel(),
                true,
                position
            )
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2)
            viewModel.statusOfSearchFrag = StatusOfSearchFrag.Results
        }

        binding.floatingActionButtonPlayListSearchFrg.setOnClickListener {
            if (songAdapter.songs.isEmpty()) {
                Snackbar.make(
                    it,
                    "Nothing To Play... Search Something For Play",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            mainViewModel.playOrToggleListOfSongs(songAdapter.songs.toYtAudioDataModel(), true, 0)
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2)
        }

        searchSuggestionAdapter.setItemClickListener {
            binding.searchButtonViewSearchFragment.setQuery(it, true)
        }
        searchSuggestionAdapter.setClearClickListener {
            mainViewModel.deleteSearchByQuery(it)
            searchSuggestionAdapter.songs -= it
            viewModel.suggestionList = viewModel.suggestionList - it
        }

        binding.backButtonSearchFragment.setOnClickListener {
            if (binding.recyclerViewSearchResult.isVisible) {
                findNavController().navigateUp()
            } else {
                if (songAdapter.songs.isNotEmpty()) {
                    showSearchSuggestionList(false)
                } else {
                    findNavController().navigateUp()
                }
            }
        }

        if (viewModel.statusOfSearchFrag == StatusOfSearchFrag.Suggest) {
            showSearchSuggestionList(true)
        } else {
            showSearchSuggestionList(false)
        }

        val keyword = arguments?.getString(Constants.PASS_EXPLORE_KEYWORDS)
        if (keyword != null) {
            binding.searchButtonViewSearchFragment.setQuery(keyword, true)
            arguments = null
        }

        updatingListObserver()

        lifecycleScope.launch {
            binding.searchButtonViewSearchFragment.getQueryTextChangeStateFlow().debounce(200)
                .filter {
                    it.isNotEmpty()
                }.distinctUntilChanged()
                .flatMapLatest {
                    viewModel.searchSongResult(it)
                }
                .flowOn(Dispatchers.Default)
                .collect { result ->
                    if (!isResultShowing || _binding == null) {
                        return@collect
                    }
                    binding.progressBarSearchFragment.isVisible =
                        (result is Resource.Loading && result.data.isNullOrEmpty())
                    binding.textViewNotFoundSearch.isVisible =
                        (result is Resource.Error || result is Resource.Success) && result.data.isNullOrEmpty()
                    songAdapter.songs = result.data ?: return@collect
                }
        }
    }

    private fun SearchView.getQueryTextChangeStateFlow(): StateFlow<String> {
        val queryStr = MutableStateFlow("")

        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    queryStr.value = query
                    binding.searchButtonViewSearchFragment.clearFocus()
                    showSearchSuggestionList(false)

                    mainViewModel.insertSearchQuery(it)

                    val firebaseAnalytics = Firebase.analytics
                    firebaseAnalytics.logEvent("Search_Box_Event") {
                        param("Searched_Query", it)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (viewModel.statusOfSearchFrag == StatusOfSearchFrag.Results) {
                    showSearchSuggestionList(false)
                    viewModel.statusOfSearchFrag = StatusOfSearchFrag.Suggest
                } else {
                    showSearchSuggestionList(true)
                }

                newText.let {
                    if (it.isBlank()) {
                        searchSuggestionAdapter.songs = viewModel.suggestionList
                        return@let
                    }

                    viewModel.searchSuggestionText(it)
                }
                return true
            }
        })
        return queryStr
    }

    private fun settingUpRecyclerView() {

        searchSuggestionAdapter = SearchSuggestionAdapter()
        binding.recyclerViewSearchSuggestion.apply {
            adapter = searchSuggestionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.recyclerViewSearchResult.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun updatingListObserver() {
        mainViewModel.getListSearchHistory {
            val list: ArrayList<String> = ArrayList()
            it.forEach {
                list.add(it.queryText)
            }
            viewModel.suggestionList = list
            searchSuggestionAdapter.songs = list
        }
        viewModel.searchSuggestionList.observe(viewLifecycleOwner) {
            if (isResultShowing) {
                return@observe
            }
            searchSuggestionAdapter.songs = it
        }
    }

    override fun onResume() {
        super.onResume()
        if (keyboardNeeded) {
            binding.searchButtonViewSearchFragment.isIconified = false
            keyboardNeeded = false
        }
    }

    private fun showSearchSuggestionList(isSearch: Boolean) {
        isResultShowing = !isSearch
        viewModel.statusOfSearchFrag =
            if (isSearch) StatusOfSearchFrag.Suggest else StatusOfSearchFrag.Results
        binding.recyclerViewSearchSuggestion.isVisible = isSearch
        binding.recyclerViewSearchResult.isVisible = !isSearch
        binding.floatingActionButtonPlayListSearchFrg.isVisible = !isSearch
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.recyclerViewSearchResult.adapter = null
        binding.recyclerViewSearchSuggestion.adapter = null
        _binding = null

    }

    /**
     * Handling voice Search
     */
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
}