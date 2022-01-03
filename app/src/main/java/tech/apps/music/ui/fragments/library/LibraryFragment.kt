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
import kotlinx.android.synthetic.main.library_fragment.*
import tech.apps.music.R
import tech.apps.music.adapters.SongAdapter
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.library_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        libraryViewModel = ViewModelProvider(requireActivity())[LibraryViewModel::class.java]

        recentAudioAdapter.isViewHorizontal = true

        setUpRecyclerView()

        MaterialToolbarHome.inflateMenu(R.menu.home_menu)

        MaterialToolbarHome.setOnMenuItemClickListener { item ->
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
        bookmarkTextItemLabel.text = "${viewModel.getWatchLaterList.value?.size ?: 0} items"

    }

    private fun setUpRecyclerView() {

        recyclerViewRecent.apply {
            adapter = recentAudioAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        addingSongIntoRecyclerView()

        bookmarkGoto.setOnClickListener {
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
            if (it.size > 20) {
                viewModel.deleteRecentlyAdded5More(it[20].time)
            }
            if (it.isNullOrEmpty()) {
                recentAudioAdapter.songs = listOf(
                    SongModelForList()
                )
            }
        }
    }

}