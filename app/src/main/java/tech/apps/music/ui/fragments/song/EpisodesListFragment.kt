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
import tech.apps.music.model.toEpisodes
import tech.apps.music.model.ytAudioDataModel
import tech.apps.music.ui.fragments.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EpisodesListFragment : Fragment() {

    private lateinit var binding: FragmentEpisodesListBinding

    @Inject
    lateinit var episodeAdapter: EpisodeAdapter
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEpisodesListBinding.inflate(layoutInflater, container, false)
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
        episodeAdapter.currentlyPlayingSongId = mainViewModel.curPlayingSong.value?.description?.mediaId

        mainViewModel.playlistItems.observe(viewLifecycleOwner){
            it?.toEpisodes().also {
                if (it != null) {
                    episodeAdapter.songs = it
                }
            }
        }

        episodeAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it.ytAudioDataModel(),true)
            findNavController().navigateUp()
        }
    }

}