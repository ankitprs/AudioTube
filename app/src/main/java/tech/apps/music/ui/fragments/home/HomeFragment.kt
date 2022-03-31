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
import tech.apps.music.model.toYtAudioDataModel
import tech.apps.music.others.Constants
import tech.apps.music.ui.fragments.MainViewModel
import tech.apps.music.ui.more.MoreActivity
import tech.apps.music.util.creatingListOfExplores
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    @Inject
    lateinit var recentAudioAdapter: SongAdapter

    private lateinit var exploreAdapter: ExploreAdapter
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

        binding.MaterialToolbarHome.inflateMenu(R.menu.home_menu)
        binding.MaterialToolbarHome.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.app_bar_more -> {
                    startActivity(Intent(activity, MoreActivity::class.java))
                }
                R.id.app_bar_search -> {
                    findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
                }
                else -> {}
            }
            true
        }
        setUpRecyclerView()
        addingSongIntoRecyclerView()

        recentAudioAdapter.setItemClickListener {
            viewModel.playOrToggleListOfSongs(
                (listOf(it)).toYtAudioDataModel(),
                true,
                0,
                it.watchedPosition
            )
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2)
        }

        exploreAdapter.setItemClickListener {
            val bundle = Bundle()
            bundle.putString(Constants.PASS_EXPLORE_KEYWORDS, it.keyword)
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment, bundle)
        }
    }

    private fun setUpRecyclerView() {
        exploreAdapter = ExploreAdapter()

        binding.recyclerViewContinueWatchMFrag.apply {
            adapter = recentAudioAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.recyclerViewLatestMFrag.apply {
            adapter = exploreAdapter
            layoutManager = GridLayoutManager(
                requireContext(),
                2
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
        exploreAdapter.songs = creatingListOfExplores()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewContinueWatchMFrag.adapter = null
        binding.recyclerViewLatestMFrag.adapter = null
        _binding = null
    }
}