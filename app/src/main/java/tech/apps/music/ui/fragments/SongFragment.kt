package tech.apps.music.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.*
import kotlinx.android.synthetic.main.home_activity.*
import tech.apps.music.R
import tech.apps.music.exoplayer.isPlaying
import tech.apps.music.exoplayer.toSong
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.others.Constants
import tech.apps.music.others.Status
import tech.apps.music.ui.viewmodels.MainViewModel
import tech.apps.music.ui.viewmodels.SongViewModel
import tech.apps.music.util.TimeFunction
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment() {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel: SongViewModel by viewModels()

    private var curPlayingSong: YTAudioDataModel? = null
    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekbar: Boolean = true

    private var liked: Boolean = false

    private var playbackSpeed: Float = 1f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_song, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        subscribeToObserver()
        val videoId = arguments?.getString(Constants.SEARCH_FRAGMENT_VIDEO_ID)

        if (videoId != null) {
            val videoLink = "https://www.youtube.com/watch?v=$videoId"
            val dialog = Dialog(requireActivity())

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_card_item)
            dialog.show()

            mainViewModel.addSongInRecent(videoLink, requireActivity()) {
                if (!it) {
                    Toast.makeText(activity, "Try Again Later", Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
        }

        shareButtonSongFragment.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "AudioTube app")
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=tech.apps.music"
            )
            startActivity(Intent.createChooser(intent, "Share URL"))
        }

        ivPlayPauseSongFragment.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        ivSkipPreviousSongFragment.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        ivSkipNextSongFragment.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setCurPLayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }

        })

        exitButtonSongFragment.setOnClickListener {
            findNavController().navigateUp()
        }

        if (curPlayingSong != null) {
            likedButtonFinder()
        }

        imageViewLikeButton.setOnClickListener {
            if (liked) {
                liked = false
                curPlayingSong?.let { it1 ->
                    mainViewModel.songDisLiked(
                        "https://www.youtube.com/watch?v=${it1.mediaId}",
                        it1.mediaId
                    )
                }
            } else {
                liked = true
                curPlayingSong?.let { it1 ->
                    mainViewModel.songLiked(
                        "https://www.youtube.com/watch?v=${it1.mediaId}",
                        it1.mediaId
                    )
                }
            }
            likeButtonManager()
        }
        setPlaybackSpeed()
        materialCardViewDisVisible.setOnClickListener {
            constraintLayout.isVisible = !constraintLayout.isVisible
        }
        materialCardView10.setOnClickListener {
            setPlaybackSpeed(1f, "1.0")
        }
        materialCardView125.setOnClickListener {
            setPlaybackSpeed(1.25f, "1.25")
        }
        materialCardView15.setOnClickListener {
            setPlaybackSpeed(1.5f, "1.5")
        }
        materialCardView175.setOnClickListener {
            setPlaybackSpeed(1.75f, "1.75")
        }
        materialCardView20.setOnClickListener {
            setPlaybackSpeed(2f, "2.0")
        }
    }

    private fun setPlaybackSpeed(playSpeed: Float = 1f, playSpeedString: String = "1.0") {

//        val sharedPref = requireActivity().getSharedPreferences(
//            Constants.SHARED_PREF_PLAYBACK_SPEED,
//            AppCompatActivity.MODE_PRIVATE
//        )
//        playbackSpeed = sharedPref.getFloat(Constants.SAVE_PLAYBACK_SPEED, 1f)
//
//        print(playbackSpeed)
//
//        if (playbackSpeed != playSpeed) {
//            playbackSpeed = playSpeed
//            val sharedPrefEditor = sharedPref.edit()
//            sharedPrefEditor.putFloat(Constants.SAVE_PLAYBACK_SPEED, playbackSpeed)
//            sharedPrefEditor.apply()
//        }
        Toast.makeText(activity,"playSpeed",Toast.LENGTH_LONG).show()
        speedControllerTextView.text = playSpeedString
        constraintLayout.isVisible = false
        mainViewModel.setPlaybackSpeed(playSpeed)
    }

    private fun likedButtonFinder() {
        mainViewModel.getLikedList.observe(viewLifecycleOwner) {
            liked = it.find { songLiked ->
                songLiked.videoId == curPlayingSong?.mediaId
            } != null
            likeButtonManager()
        }
    }

    private fun likeButtonManager() {
        if (liked) {
            imageViewLikeButton.setImageResource(R.drawable.ic_baseline_favorite_24)
        } else {
            imageViewLikeButton.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        }
    }

    private fun updateTitleAndSongImage(song: YTAudioDataModel) {
        glide.load(song.thumbnailUrl).into(songThumbnailSongFragment)
        songTitleSongFragment.text = song.title
        songAuthorSongFragment.text = song.author
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeToObserver() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if (curPlayingSong == null && songs.isNotEmpty()) {
                                curPlayingSong = songs[0]
                                updateTitleAndSongImage(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
        mainViewModel.curPlayingSong.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            curPlayingSong = it.toSong()
            updateTitleAndSongImage(curPlayingSong!!)
            likedButtonFinder()
        }
        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            ivPlayPauseSongFragmentImageView.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.exo_icon_pause else R.drawable.exo_icon_play
            )
            seekBar.progress = it?.position?.toInt() ?: 0
        }
        songViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            if (shouldUpdateSeekbar) {
                seekBar.progress = it.toInt()
                setCurPLayerTimeToTextView(it)
            }
        }
        songViewModel.curSongDuration.observe(viewLifecycleOwner) {
            seekBar.max = it.toInt()
            if (it > 0) {
                tvSongDuration.text = TimeFunction.songDuration(it / 1000L)
            } else {
                tvSongDuration.text = "00:00"
            }
        }
    }

    private fun setCurPLayerTimeToTextView(ms: Long) {
        tvCurTime.text = TimeFunction.songDuration(ms / 1000L)
    }
}