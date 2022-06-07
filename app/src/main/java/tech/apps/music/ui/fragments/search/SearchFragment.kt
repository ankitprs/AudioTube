package tech.apps.music.ui.fragments.search

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.*
import tech.apps.music.R
import tech.apps.music.adapters.SearchSuggestionAdapter
import tech.apps.music.adapters.SongAdapter
import tech.apps.music.database.network.YoutubeRepository
import tech.apps.music.databinding.SearchFragmentBinding
import tech.apps.music.model.toYtAudioDataModel
import tech.apps.music.others.Constants
import tech.apps.music.ui.fragments.MainViewModel
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]
        _binding = SearchFragmentBinding.inflate(layoutInflater,container,false)
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

        songAdapter.setItemClickListener { _ , position ->
            mainViewModel.playOrToggleListOfSongs(songAdapter.songs.toYtAudioDataModel(),true,position)
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2)
        }

        binding.floatingActionButtonPlayListSearchFrg.setOnClickListener {
            if(songAdapter.songs.isEmpty()){
                Snackbar.make(it,"Nothing To Play... Search Something For Play",Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mainViewModel.playOrToggleListOfSongs(songAdapter.songs.toYtAudioDataModel(),true,0)
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
            if(binding.recyclerViewSearchResult.isVisible){
                findNavController().navigateUp()
            }else{
                if(songAdapter.songs.isNotEmpty()){
                    showSearchSuggestionList(false)
                }else{
                    findNavController().navigateUp()
                }
            }
        }

        binding.searchButtonViewSearchFragment.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener,
                android.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        searchQuery(it)
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

                override fun onQueryTextChange(newText: String?): Boolean {
                    if(viewModel.statusOfSearchFrag == StatusOfSearchFrag.Results){
                        showSearchSuggestionList(false)
                        viewModel.statusOfSearchFrag = StatusOfSearchFrag.Suggest
                    }else{
                        showSearchSuggestionList(true)
                    }

                    newText?.let {
                        if(it.isBlank()){
                            searchSuggestionAdapter.songs = viewModel.suggestionList
                            return@let
                        }

                        viewModel.searchSuggestionText(it, requireActivity()) { list ->
                            searchSuggestionAdapter.songs = list
                        }
                    }
                    return false
                }
            }
        )
        if(viewModel.statusOfSearchFrag == StatusOfSearchFrag.Suggest){
            showSearchSuggestionList(true)
        }else{
            showSearchSuggestionList(false)
        }

        val keyword = arguments?.getString(Constants.PASS_EXPLORE_KEYWORDS)
        if (keyword != null) {
            binding.searchButtonViewSearchFragment.setQuery(keyword, true)
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
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        mainViewModel.getListSearchHistory {
            val list: ArrayList<String> = ArrayList()
                it.forEach {
                    list.add(it.queryText)
                }
            viewModel.suggestionList = list
            searchSuggestionAdapter.songs = list
        }
    }

    private fun searchQuery(query: String) {
        binding.progressBarSearchFragment.isVisible = true

        GlobalScope.launch(Dispatchers.IO) {
            val it = YoutubeRepository().searchWithKeywords(query, requireActivity())

            withContext(Dispatchers.Main){
                view?.findViewById<TextView>(R.id.textViewNotFoundSearch)?.isVisible = it.size == 0

                songAdapter.songs = it
                viewModel.listOfSearchResults = it
                view?.findViewById<ProgressBar>(R.id.progressBarSearchFragment)?.isVisible = false
            }
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
        viewModel.statusOfSearchFrag = if(isSearch) StatusOfSearchFrag.Suggest else StatusOfSearchFrag.Results
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