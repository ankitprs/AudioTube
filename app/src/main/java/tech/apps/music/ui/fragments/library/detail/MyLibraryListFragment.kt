package tech.apps.music.ui.fragments.library.detail

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
import tech.apps.music.databinding.FragmentMyLibraryListBinding
import tech.apps.music.model.SongModelForList
import tech.apps.music.model.toSongForList
import tech.apps.music.others.Constants
import tech.apps.music.ui.fragments.library.LibraryViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MyLibraryListFragment : Fragment() {


    private lateinit var binding: FragmentMyLibraryListBinding
    private lateinit var libraryViewModel: LibraryViewModel

    @Inject
    lateinit var watchLaterAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyLibraryListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        libraryViewModel = ViewModelProvider(requireActivity())[LibraryViewModel::class.java]

        val myLibraryType = arguments?.getString(Constants.PASSING_MY_LIBRARY_TYPE)

        watchLaterAdapter.isViewHorizontal = true

        binding.backButtonMyLibrary.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbarTextForMyLibrary.text = myLibraryType ?: "MyLibrary"

        binding.recyclerViewWatchLater.apply {
            adapter = watchLaterAdapter
            layoutManager = LinearLayoutManager(
                requireContext()
            )

        }

        if (myLibraryType == Constants.MY_LIBRARY_TYPE_DOWNLOAD) {
            downloaded()
        } else {
            bookmark()
        }
        watchLaterAdapter.setItemClickListener {
            val bundle = Bundle()
            bundle.putString(Constants.SEARCH_FRAGMENT_VIDEO_ID, it.videoId)
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2, bundle)
        }
    }

    private fun downloaded() {
        watchLaterAdapter.songs = listOf(
            SongModelForList()
        )
    }

    private fun bookmark() {
        libraryViewModel.getWatchLaterList.observe(viewLifecycleOwner) {
            println(it)
            if (it.isNullOrEmpty()) {
                watchLaterAdapter.songs = listOf(
                    SongModelForList()
                )
            } else {
                watchLaterAdapter.songs = it.toSongForList()
            }
        }
    }
}