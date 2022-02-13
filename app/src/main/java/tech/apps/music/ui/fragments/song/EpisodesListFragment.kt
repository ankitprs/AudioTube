package tech.apps.music.ui.fragments.song

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.adapters.EpisodeAdapter
import tech.apps.music.databinding.FragmentEpisodesListBinding
import tech.apps.music.ui.fragments.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EpisodesListFragment : Fragment() {

    private var _binding: FragmentEpisodesListBinding? = null
    private val binding: FragmentEpisodesListBinding get() = _binding!!

    @Inject
    lateinit var episodeAdapter: EpisodeAdapter
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEpisodesListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        binding.imageViewBackButtonList.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.recyclerViewEpisodesList.apply {
            adapter = episodeAdapter
            layoutManager = LinearLayoutManager(
                requireContext()
            )
        }
//        episodeAdapter.songs = mainViewModel.currentlyPlayingPlaylist.toEpisodes()?:return


//        episodeAdapter.setItemClickListener {
//            mainViewModel.gotoIndex(
//                mainViewModel.currentlyPlayingPlaylist.indexOf(
//                    mainViewModel.currentlyPlayingPlaylist.find { song ->
//                        song.description.mediaId == it.songId
//                    }
//                ).toLong()
//            )
//        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewEpisodesList.adapter = null
        _binding = null
    }

}