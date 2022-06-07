package tech.apps.music.ui.fragments.library

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
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
import tech.apps.music.model.toYtAudioDataModel
import tech.apps.music.others.Constants
import tech.apps.music.ui.fragments.MainViewModel
import tech.apps.music.ui.more.MoreActivity
import javax.inject.Inject

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    @Inject
    lateinit var recentAudioAdapter: SongAdapter

    private var _binding: LibraryFragmentBinding? = null
    private val binding: LibraryFragmentBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LibraryFragmentBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(
            requireActivity(),
            R.color.dark_background
        )

        setUpRecyclerView()

        binding.MaterialToolbarHome.inflateMenu(R.menu.home_menu)
        binding.MaterialToolbarHome.setOnMenuItemClickListener { item ->
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

        recentAudioAdapter.setItemClickListener {it, position ->
            viewModel.playOrToggleListOfSongs(recentAudioAdapter.songs.toYtAudioDataModel(),true,position,it.watchedPosition)
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2)
        }

        recentAudioAdapter.setItemMenuClickListener { pair ->
            val popupMenu = PopupMenu(requireActivity(),pair.second)
            popupMenu.menuInflater.inflate(R.menu.song_detail_menu,popupMenu.menu)
            popupMenu.show()
            val songModel = pair.first
            popupMenu.setOnMenuItemClickListener{
                when(it.itemId){
                    R.id.songPopupMenu_AddPlaylist ->{

                    }
                    R.id.songPopupMenu_Share ->{}
                    R.id.songPopupMenu_Play -> {}
                }
                true
            }

        }

        viewModel.getWatchLaterList {
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
    }

    private fun addingSongIntoRecyclerView() {

        viewModel.getRecentList {
            recentAudioAdapter.songs = it.toSongModelForList()
            if (it.isEmpty()) {
                recentAudioAdapter.songs = listOf(
                    SongModelForList()
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewRecent.adapter = null
        _binding = null
    }

    private fun addingPlaylist(){



    }

}