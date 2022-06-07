package tech.apps.music.ui.fragments.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.apps.music.R
import tech.apps.music.adapters.HomeListAdapter
import tech.apps.music.adapters.SongAdapter
import tech.apps.music.database.network.YoutubeRepository
import tech.apps.music.databinding.MainFragmentBinding
import tech.apps.music.model.toSongModelForList
import tech.apps.music.model.toYtAudioDataModel
import tech.apps.music.ui.fragments.MainViewModel
import tech.apps.music.ui.more.MoreActivity
import tech.apps.music.util.AdsFunctions
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var recommendAdapter: SongAdapter
    @Inject
    lateinit var homeListAdapter: HomeListAdapter

    private lateinit var viewModel: MainViewModel
    private var _binding: MainFragmentBinding? = null
    private val binding: MainFragmentBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        binding.settingIconHomeFrg.setOnClickListener {
            startActivity(Intent(activity, MoreActivity::class.java))
        }

        binding.searchIconHomeFrg.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

        changeRecyclerViewItem()
        statusBarBackgroundColor(BitmapFactory.decodeResource(context?.resources, R.drawable.focus))

        binding.toggleButton.setOnCheckedStateChangeListener { group, checkedId ->
            if (checkedId.isEmpty()) {
                binding.topBackgroundImageHomeFrg.setImageResource(R.drawable.song)
                changeRecyclerViewItem()
                hideShowRecentList(true)
                return@setOnCheckedStateChangeListener
            }
            hideShowRecentList(false)

            when (checkedId[0]) {
                R.id.songs1 -> {
                    searchQuery("song")
                    statusBarBackgroundColor(
                        BitmapFactory.decodeResource(
                            context?.resources,
                            R.drawable.song
                        )
                    )
                }
                R.id.relax2 -> {
                    statusBarBackgroundColor(
                        BitmapFactory.decodeResource(
                            context?.resources,
                            R.drawable.relax
                        )
                    )
                    searchQuery("relax music")
                }
                R.id.workout3 -> {
                    statusBarBackgroundColor(
                        BitmapFactory.decodeResource(
                            context?.resources,
                            R.drawable.workout
                        )
                    )
                    searchQuery("workout music")
                }
                R.id.podcast4 -> {
                    statusBarBackgroundColor(
                        BitmapFactory.decodeResource(
                            context?.resources,
                            R.drawable.podcast
                        )
                    )
                    searchQuery("podcast")
                }
                R.id.focus5 -> {
                    statusBarBackgroundColor(
                        BitmapFactory.decodeResource(
                            context?.resources,
                            R.drawable.focus
                        )
                    )
                    searchQuery("focus music")
                }
            }
        }
        setUpRecyclerView()
        addingSongIntoRecyclerView()

        recommendAdapter.setItemClickListener { it, position ->
            AdsFunctions.showAds(requireActivity())
            viewModel.playOrToggleListOfSongs(
                recommendAdapter.songs.toYtAudioDataModel(),
                true,
                position,
                it.watchedPosition
            )
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2)
        }

        homeListAdapter.setItemClickListener { it, position ->
            AdsFunctions.showAds(requireActivity())
            viewModel.playOrToggleListOfSongs(
                recommendAdapter.songs.toYtAudioDataModel(),
                true,
                position,
                it.watchedPosition
            )
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2)
        }
        AdsFunctions.loadAds(requireActivity())
    }

    private fun setUpRecyclerView() {

        binding.recyclerViewContinueWatchMFrag.apply {
            adapter = homeListAdapter
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        }
        
        binding.recyclerViewRecommendMFrag.apply { 
            adapter = recommendAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }
    }

    private fun addingSongIntoRecyclerView() {
        viewModel.getLast5RecentList {
            if (it.isNotEmpty()) {
                homeListAdapter.songs = it.toSongModelForList()
            } else {
                hideShowRecentList(false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewContinueWatchMFrag.adapter = null
        _binding = null
    }

    private fun changeRecyclerViewItem() {
        binding.progressBarHomeFrg.isVisible = true

        CoroutineScope(Dispatchers.IO).launch {
            val it = YoutubeRepository().trendingMusicNow(requireActivity())

            withContext(Dispatchers.Main) {
                view?.findViewById<TextView>(R.id.textViewNotFoundSearch)?.isVisible = it.size == 0

                recommendAdapter.songs = it
                view?.findViewById<ProgressBar>(R.id.progressBarHomeFrg)?.isVisible = false
            }
        }
    }

    private fun searchQuery(query: String) {
        binding.progressBarHomeFrg.isVisible = true

        CoroutineScope(Dispatchers.IO).launch {
            val it = YoutubeRepository().searchWithKeywords(query, requireActivity())

            withContext(Dispatchers.Main) {
                view?.findViewById<TextView>(R.id.textViewNotFoundSearch)?.isVisible = it.size == 0

                recommendAdapter.songs = it
                view?.findViewById<ProgressBar>(R.id.progressBarHomeFrg)?.isVisible = false
            }
        }
    }

    private fun statusBarBackgroundColor(bitmap: Bitmap) {

        binding.topBackgroundImageHomeFrg.setImageBitmap(bitmap)
        Palette.from(bitmap).generate {
            val window = requireActivity().window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = it?.dominantSwatch?.rgb ?: ContextCompat.getColor(
                requireActivity(),
                R.color.dark_background
            )
        }
    }


    private fun hideShowRecentList(isRecent: Boolean) {
        binding.labelTrendingHomeFrg.isVisible = isRecent
        binding.recyclerViewContinueWatchMFrag.isVisible = isRecent
        binding.recentTextTextView.isVisible = isRecent
        binding.lineRecentLabel.isVisible = isRecent
        binding.lineTrendingLabel.isVisible = isRecent
    }
}