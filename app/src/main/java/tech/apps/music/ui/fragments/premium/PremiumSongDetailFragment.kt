package tech.apps.music.ui.fragments.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.R
import tech.apps.music.adapters.EpisodeAdapter
import tech.apps.music.databinding.PremiumSongDetailFragmentBinding
import tech.apps.music.model.EpisodesListModel
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.others.Constants
import tech.apps.music.ui.fragments.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PremiumSongDetailFragment : Fragment() {

    private lateinit var viewModel: PremiumSongDetailViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: PremiumSongDetailFragmentBinding

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var episodeAdapter: EpisodeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = PremiumSongDetailFragmentBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this)[PremiumSongDetailViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val episodesListModel: EpisodesListModel =
            (arguments?.getSerializable(Constants.PASSING_EPISODES_MODEL)
                ?: return binding.root) as EpisodesListModel
        glide.load(episodesListModel.thumbnailUrl)
            .centerCrop()
            .into(binding.imageViewAudioBookDetailThumbnail)


        binding.textViewAudioBookDetailTitle.text = episodesListModel.title
        binding.textViewAudioBookDetailAuthor.text = episodesListModel.author

        episodeAdapter.iconUri = episodesListModel.thumbnailUrl

        binding.recyclerViewEpisodesListDetailFr.apply {
            adapter = episodeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        episodeAdapter.songs = episodesListModel.EpisodesModel

        episodeAdapter.setItemClickListener {
            mainViewModel.playOrToggleListOfSongs(episodesListModel.ytAudioDataModel(), true, it)
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2)
        }

        episodesListModel.episodePosition = episodesListModel.EpisodesModel.indexOf(
            episodesListModel.EpisodesModel.find {
                it.watchedPosition == 0L
            }
        )

        if (episodesListModel.episodePosition >= 1) {
            binding.buttonAudioBookDetail.text = "Resume"
        }

        binding.buttonAudioBookDetail.setOnClickListener {
            mainViewModel.playOrToggleListOfSongs(episodesListModel.ytAudioDataModel(), true, episodesListModel.episodePosition)
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2)
        }

        return binding.root
    }

}

private fun EpisodesListModel.ytAudioDataModel(): List<YTAudioDataModel> {
    val list: ArrayList<YTAudioDataModel> = ArrayList()
    EpisodesModel.forEach {
        list.add(
            YTAudioDataModel(
                it.songId,
                it.title,
                author,
                it.songUrl,
                thumbnailUrl,
                it.duration
            )
        )
    }
    return list
}
