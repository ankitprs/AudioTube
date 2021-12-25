package tech.apps.music.ui.fragments.song

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.adapters.EpisodeAdapter
import tech.apps.music.databinding.FragmentEpisodesListBinding
import javax.inject.Inject

@AndroidEntryPoint
class EpisodesListFragment : Fragment() {

    private lateinit var binding: FragmentEpisodesListBinding

    @Inject
    lateinit var episodeAdapter: EpisodeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEpisodesListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageViewBackButtonList.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.recyclerViewEpisodesList.apply {
            adapter = episodeAdapter
            layoutManager = LinearLayoutManager(
                requireContext()
            )
        }
    }

}