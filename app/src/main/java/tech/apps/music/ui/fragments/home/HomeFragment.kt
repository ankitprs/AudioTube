package tech.apps.music.ui.fragments.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.R
import tech.apps.music.adapters.ExploreAdapter
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

    @Inject
    lateinit var exploreAdapter: ExploreAdapter

    private lateinit var binding: MainFragmentBinding


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
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment,bundle)
        }
    //        premiumLisAdapter.setItemClickListener {
//            val bundle = Bundle()
//            bundle.putSerializable(Constants.PASSING_EPISODES_MODEL, it)
//            findNavController().navigate(
//                R.id.action_homeFragment_to_premiumSongDetailFragment,
//                bundle
//            )
//        }
    }

    private fun setUpRecyclerView() {

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
//        setUpRecyclerView(binding.recyclerViewThrillerMFrag, premiumLisAdapter)
//        setUpRecyclerView(binding.recyclerViewBiographyMFrag, premiumLisAdapter)
    }

//    private fun setUpRecyclerView(recyclerView: RecyclerView, preAdapter: PremiumListAdapter) {
//        recyclerView.apply {
//            adapter = preAdapter
//            layoutManager = LinearLayoutManager(
//                requireContext(),
//                LinearLayoutManager.HORIZONTAL,
//                false
//            )
//        }
//    }

    private fun addingSongIntoRecyclerView() {
        viewModel.getContinueWatchingList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {

                recentAudioAdapter.songs = it.toSongModelForList()
                if (it.size > 20) {
                    viewModel.deleteRecentlyAdded5More(it[20].time)
                }
            } else {
                recentAudioAdapter.songs = listOf(
                    SongModelForList()
                )
            }
        }
        exploreAdapter.songs = VideoData.creatingListOfExplores()
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