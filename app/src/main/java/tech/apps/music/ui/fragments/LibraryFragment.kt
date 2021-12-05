package tech.apps.music.ui.fragments

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
import kotlinx.android.synthetic.main.main_fragment.MaterialToolbarHome
import kotlinx.android.synthetic.main.main_fragment.recyclerViewRecent
import tech.apps.music.R
import tech.apps.music.adapters.LikedAdapter
import tech.apps.music.adapters.RecentAdapter
import tech.apps.music.database.offline.YTVideoLink
import tech.apps.music.database.offline.YTVideoLinkLiked
import tech.apps.music.ui.fragments.viewModel.LibraryViewModel
import tech.apps.music.ui.more.MoreActivity
import tech.apps.music.ui.viewmodels.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private lateinit var libraryViewModel: LibraryViewModel

    private lateinit var viewModel: MainViewModel

    @Inject
    lateinit var recentAudioAdapter: RecentAdapter

    @Inject
    lateinit var likedAdapter: LikedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.library_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        libraryViewModel = ViewModelProvider(this)[LibraryViewModel::class.java]

        setUpRecyclerView()

        MaterialToolbarHome.inflateMenu(R.menu.home_menu)

        MaterialToolbarHome.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.app_bar_more -> {
                    startActivity(Intent(activity, MoreActivity::class.java))
                }
                R.id.app_bar_search -> {
                    findNavController().navigate(R.id.action_libraryFragment_to_searchFragment)
                }
                else -> {}
            }
            true
        }

        recentAudioAdapter.setItemClickListener {
            viewModel.playOrToggleSong(it)
            findNavController().navigate(
                R.id.action_homeFragment2_to_songFragment2
            )
        }

        likedAdapter.setItemClickListener {
            viewModel.playOrToggleSong(it)
            findNavController().navigate(
                R.id.action_homeFragment2_to_songFragment2
            )
        }
    }

    private fun setUpRecyclerView() {
        recyclerViewLiked.apply {
            adapter = likedAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }

        recyclerViewRecent.apply {
            adapter = recentAudioAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
        addingSongIntoRecyclerView()
    }
    private fun addingSongIntoRecyclerView() {
        viewModel.getLikedList.observe(viewLifecycleOwner) {
            likedAdapter.songs = it
            if(it.isNullOrEmpty()){
                likedAdapter.songs = listOf(
                    YTVideoLinkLiked()
                )
            }
        }
        viewModel.getRecentList.observe(viewLifecycleOwner) {
            recentAudioAdapter.songs = it
            if(it.size > 5){
                viewModel.deleteRecentlyAdded5More(it[5].time)
            }
            if(it.isNullOrEmpty()){
                recentAudioAdapter.songs = listOf(
                    YTVideoLink()
                )
            }
        }
    }

}