package tech.apps.music.ui.fragments.home

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.R
import tech.apps.music.adapters.EpisodeAdapter
import tech.apps.music.databinding.SongDetailFragmentBinding
import tech.apps.music.model.EpisodesListModel
import tech.apps.music.model.ytAudioDataModel
import tech.apps.music.others.Constants
import tech.apps.music.ui.fragments.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SongDetailFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: SongDetailFragmentBinding

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var episodeAdapter: EpisodeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SongDetailFragmentBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val episodesListModelId: String =
            (arguments?.getString(Constants.PASSING_EPISODES_MODEL_ID)
                ?: return)

        var episodesListModel: EpisodesListModel =
            mainViewModel.listOfAudioBooks.value?.find {
            it.id == episodesListModelId
        } ?: return

        glide.asBitmap()
            .load(episodesListModel.thumbnailUrl)
            .centerCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    binding.imageViewAudioBookDetailThumbnail.setImageBitmap(resource)
                    addingBackgroundColorInImage(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })


        binding.textViewAudioBookDetailTitle.text = episodesListModel.title
        binding.textViewAudioBookDetailAuthor.text = episodesListModel.author

        binding.recyclerViewEpisodesListDetailFr.apply {
            adapter = episodeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        episodeAdapter.songs = episodesListModel.EpisodesModel

        episodeAdapter.setItemClickListener {

            Firebase.analytics.logEvent("AudioBook_Played") {
                param("AudioBook_Title", episodesListModel.title)
            }

            mainViewModel.changeIsYoutubeVideoCurSong(false)
            mainViewModel.playOrToggleListOfSongs(
                episodesListModel.ytAudioDataModel(),
                true,
                episodesListModel.EpisodesModel.indexOf(it)
            )
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
            mainViewModel.changeIsYoutubeVideoCurSong(false)

            Firebase.analytics.logEvent("AudioBook_Played") {
                param("AudioBook_Title", episodesListModel.title)
            }

            mainViewModel.playOrToggleListOfSongs(
                episodesListModel.ytAudioDataModel(),
                true,
                episodesListModel.episodePosition
            )
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2)
        }

    }

    private fun addingBackgroundColorInImage(thumbnailBitmap: Bitmap) {
        Palette.from(thumbnailBitmap).generate {
            binding.imageViewAudioBookDetailThumbnail.setBackgroundColor(
                it?.mutedSwatch?.rgb ?: ContextCompat.getColor(
                    requireActivity(),
                    R.color.background
                )
            )
        }
    }

}
