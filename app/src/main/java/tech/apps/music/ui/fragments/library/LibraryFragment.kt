package tech.apps.music.ui.fragments.library

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.R
import tech.apps.music.adapters.SongAdapter
import tech.apps.music.databinding.LibraryFragmentBinding
import tech.apps.music.model.SongModelForList
import tech.apps.music.model.toSongModelForList
import tech.apps.music.others.Constants
import tech.apps.music.ui.fragments.MainViewModel
import tech.apps.music.ui.more.MoreActivity
import javax.inject.Inject

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private lateinit var libraryViewModel: LibraryViewModel
    private lateinit var viewModel: MainViewModel
    @Inject
    lateinit var recentAudioAdapter: SongAdapter
    lateinit var binding: LibraryFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LibraryFragmentBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        libraryViewModel = ViewModelProvider(requireActivity())[LibraryViewModel::class.java]

        recentAudioAdapter.isViewHorizontal = true

        setUpRecyclerView()

        binding.MaterialToolbarHome.inflateMenu(R.menu.home_menu)

        binding.MaterialToolbarHome.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.app_bar_more -> {
                    startActivity(Intent(activity, MoreActivity::class.java))
                }
                R.id.app_bar_search -> {
                    findNavController().navigate(R.id.action_libraryFragment_to_searchCatalogFragment)
                }
                else -> {}
            }
            true
        }

        recentAudioAdapter.setItemClickListener {
            val bundle = Bundle()
            bundle.putString(Constants.SEARCH_FRAGMENT_VIDEO_ID, it.videoId)
            bundle.putLong(Constants.PASSING_SONG_LAST_WATCHED_POS, it.watchedPosition)
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2, bundle)
        }

        viewModel.getWatchLaterList.observe(viewLifecycleOwner) {
            val watchLaterItemNumber: Int = it.size
            if (watchLaterItemNumber > 1) {
                binding.bookmarkTextItemLabel.text = "$watchLaterItemNumber items"
            } else {
                binding.bookmarkTextItemLabel.text = "$watchLaterItemNumber item"
            }
        }

    }

    private fun setUpRecyclerView() {

        binding.recyclerViewRecent.apply {
            adapter = recentAudioAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        addingSongIntoRecyclerView()

        binding.bookmarkGoto.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(Constants.PASSING_MY_LIBRARY_TYPE, Constants.MY_LIBRARY_TYPE_BOOKMARK)
            findNavController().navigate(
                R.id.action_libraryFragment_to_myLibraryListFragment,
                bundle
            )
        }
//        DownloadedGoto.setOnClickListener {
//            val bundle = Bundle()
//            bundle.putString(Constants.PASSING_MY_LIBRARY_TYPE, Constants.MY_LIBRARY_TYPE_DOWNLOAD)
//            findNavController().navigate(
//                R.id.action_libraryFragment_to_myLibraryListFragment,
//                bundle
//            )
//        }
    }

    private fun addingSongIntoRecyclerView() {

        viewModel.getRecentList.observe(viewLifecycleOwner) {
            recentAudioAdapter.songs = it.toSongModelForList()
            if (it.isNullOrEmpty()) {
                recentAudioAdapter.songs = listOf(
                    SongModelForList()
                )
            }
        }
    }

}