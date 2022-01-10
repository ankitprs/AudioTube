package tech.apps.music.ui.fragments.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.R
import tech.apps.music.adapters.ExploreAdapter
import tech.apps.music.adapters.PremiumListAdapter
import tech.apps.music.adapters.SongAdapter
import tech.apps.music.databinding.MainFragmentBinding
import tech.apps.music.model.SongModelForList
import tech.apps.music.model.toSongModelForList
import tech.apps.music.others.Constants
import tech.apps.music.ui.fragments.MainViewModel
import tech.apps.music.ui.more.MoreActivity
import tech.apps.music.util.VideoData
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    @Inject
    lateinit var recentAudioAdapter: SongAdapter

    private lateinit var exploreAdapter: ExploreAdapter
    private lateinit var binding: MainFragmentBinding

    @Inject
    lateinit var audioBookAdapter: PremiumListAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(layoutInflater, container, false)

        binding.MaterialToolbarHome.inflateMenu(R.menu.home_menu)

        binding.MaterialToolbarHome.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.app_bar_more -> {
                    startActivity(Intent(activity, MoreActivity::class.java))
                }
                R.id.app_bar_search -> {
                    findNavController().navigate(R.id.action_homeFragment_to_searchCatalogFragment)
                }
                else -> {}
            }
            true
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setUpRecyclerView()
        addingSongIntoRecyclerView()

        recentAudioAdapter.setItemClickListener {
            val bundle = Bundle()
            bundle.putString(Constants.SEARCH_FRAGMENT_VIDEO_ID, it.videoId)
            bundle.putLong(Constants.PASSING_SONG_LAST_WATCHED_POS, it.watchedPosition)
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2, bundle)
        }

        exploreAdapter.setItemClickListener {
            val bundle = Bundle()
            bundle.putString(Constants.PASS_EXPLORE_KEYWORDS, it.keyword)
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment, bundle)
        }
        audioBookAdapter.setItemClickListener {
            val bundle = Bundle()
            bundle.putString(Constants.PASSING_EPISODES_MODEL_ID, it.id)
            findNavController().navigate(
                R.id.action_homeFragment_to_songDetailFragment,
                bundle
            )
        }
    }

    private fun setUpRecyclerView() {
        exploreAdapter = ExploreAdapter()

        binding.recyclerViewContinueWatchMFrag.apply {
            adapter = recentAudioAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
        binding.recyclerViewLatestMFrag.apply {
            adapter = exploreAdapter
            layoutManager = GridLayoutManager(
                requireContext(),
                2
            )
        }
        binding.recyclerViewAudioBookMFrag.apply {

            adapter = audioBookAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun addingSongIntoRecyclerView() {
        viewModel.getLast5RecentList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                recentAudioAdapter.songs = it.toSongModelForList()
            } else {
                recentAudioAdapter.songs = listOf(
                    SongModelForList()
                )
            }
        }
        exploreAdapter.songs = VideoData.creatingListOfExplores()

        viewModel.listOfAudioBooks.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                audioBookAdapter.songs = it
            } else {
                binding.textViewAudioBookMainFr.isVisible = false
                binding.recyclerViewAudioBookMFrag.isVisible = false
            }
        }
    }

//    private fun subscribeToObservers() {
//        viewModel.mediaItems.observe(viewLifecycleOwner) {
//            when (it.status) {
//                Status.SUCCESS -> {
////                    recentRecyclerViewProgressBar.visibility=View.GONE
//                }
//                Status.ERROR -> Unit
//                Status.LOADING -> {
////                    recentRecyclerViewProgressBar.visibility=View.VISIBLE
//                }
//            }
//        }
//    }

}