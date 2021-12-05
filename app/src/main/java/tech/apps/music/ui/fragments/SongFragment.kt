package tech.apps.music.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.R
import tech.apps.music.databinding.FragmentSongBinding
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
    private lateinit var binding: FragmentSongBinding

    private var curPlayingSong: YTAudioDataModel? = null
    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekbar: Boolean = true

    private var liked: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSongBinding.inflate(layoutInflater,container,false)
        return binding.root
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

        binding.shareButtonSongFragment.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "AudioTube app")
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=tech.apps.music"
            )
            startActivity(Intent.createChooser(intent, "Share URL"))
        }

        binding.ivPlayPauseSongFragmentImageView.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }
        binding.ivSkipPreviousSongFragment.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        binding.ivSkipNextSongFragment.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }
        binding.ivPreviousReplaySongFragment.setOnClickListener {
            mainViewModel.replayBackSong()

            val animator = ObjectAnimator.ofFloat(it, View.ROTATION, 360f, 0f)
            animator.duration = 500
            animator.start()
        }
        binding.ivForwardSongFragment.setOnClickListener {
            mainViewModel.fastForwardSong()
            val animator = ObjectAnimator.ofFloat(it, View.ROTATION, -360f, 0f)
            animator.duration = 500
            animator.start()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

        binding.exitButtonSongFragment.setOnClickListener {
            findNavController().navigateUp()
        }

        if (curPlayingSong != null) {
            likedButtonFinder()
        }

        binding.imageViewLikeButton.setOnClickListener {
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
        checkPlaybackSpeed()
        binding.materialCardViewDisVisible.setOnClickListener {
            hideAndShowSpeed(!binding.constraintLayout.isVisible)
        }
        binding.materialCardView10.setOnClickListener {
            setPlaybackSpeed(1f, "1x")
        }
        binding.materialCardView125.setOnClickListener {
            setPlaybackSpeed(1.25f, "1.25x")
        }
        binding.materialCardView15.setOnClickListener {
            setPlaybackSpeed(1.5f, "1.5x")
        }
        binding.materialCardView175.setOnClickListener {
            setPlaybackSpeed(1.75f, "1.75x")
        }
        binding.materialCardView20.setOnClickListener {
            setPlaybackSpeed(2f, "2x")
        }
    }

    private fun setPlaybackSpeed(playSpeed: Float = 1f, playSpeedString: String = "1x") {

        val sharedPref = requireActivity().getSharedPreferences(
            Constants.SHARED_PREF_PLAYBACK_SPEED,
            AppCompatActivity.MODE_PRIVATE
        )

        val sharedPrefEditor = sharedPref.edit()
        sharedPrefEditor.putFloat(Constants.SAVE_PLAYBACK_SPEED, playSpeed)
        sharedPrefEditor.apply()

        binding.speedControllerTextView.text = playSpeedString
        changePlaybackSpeedState(playSpeed)
        hideAndShowSpeed(false)
    }

    private fun changePlaybackSpeedState(speed: Float) {
        mainViewModel.setPlaybackSpeed(speed)
    }

    private fun checkPlaybackSpeed() {

        val sharedPref = requireActivity().getSharedPreferences(
            Constants.SHARED_PREF_PLAYBACK_SPEED,
            AppCompatActivity.MODE_PRIVATE
        )
        val playbackSpeed: Float = sharedPref.getFloat(Constants.SAVE_PLAYBACK_SPEED, 1f)
        val str: String = when (playbackSpeed) {
            1f -> {
                "1x"
            }
            1.25f -> {
                "1.25x"
            }
            1.5f -> {
                "1.5x"
            }
            1.75f -> {
                "1.75x"
            }
            2f -> {
                "2x"
            }
            else -> {
                "1x"
            }
        }
        binding.speedControllerTextView.text = str
        changePlaybackSpeedState(playbackSpeed)
    }

    private fun hideAndShowSpeed(speedStatus: Boolean) {
        if (speedStatus) {
            binding.coordinatorLayout.apply {
                animate()
                    .alphaBy(1f)
                    .alpha(0f)
                    .setDuration(400)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            visibility = View.INVISIBLE
                        }
                    })
            }
            binding.constraintLayout.apply {
                isVisible = speedStatus
                animate()
                    .alphaBy(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                        }
                    })

            }
        } else {
            binding.constraintLayout.apply {
                animate()
                    .alphaBy(1f)
                    .alpha(0.0f)
                    .setDuration(400)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            visibility = View.INVISIBLE
                        }
                    })
            }
            binding.coordinatorLayout.apply {
                isVisible = !speedStatus
                animate()
                    .alphaBy(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                        }
                    })
            }
        }
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
            binding.imageViewLikeButton.setImageResource(R.drawable.ic_baseline_favorite_24)
        } else {
            binding.imageViewLikeButton.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        }
    }

    private fun updateTitleAndSongImage(song: YTAudioDataModel) {
        glide.load(song.thumbnailUrl).into(binding.songThumbnailSongFragment)
        binding.songTitleSongFragment.text = song.title
        binding.songAuthorSongFragment.text = song.author
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
            binding.ivPlayPauseSongFragmentImageView.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_round_pause_circle_24 else R.drawable.ic_round_play_circle_24
            )
            binding.seekBar.progress = it?.position?.toInt() ?: 0
        }
        songViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            if (shouldUpdateSeekbar) {
                binding.seekBar.progress = it.toInt()
                setCurPLayerTimeToTextView(it)
            }
        }
        songViewModel.curSongDuration.observe(viewLifecycleOwner) {
            binding.seekBar.max = it.toInt()
            if (it > 0) {
                binding.tvSongDuration.text = TimeFunction.songDuration(it / 1000L)
            } else {
                binding.tvSongDuration.text = "00:00"
            }
        }
        songViewModel.isBuffering.observe(viewLifecycleOwner) {
            binding.progressBarForBuffering.isVisible = it
        }
    }

    private fun setCurPLayerTimeToTextView(ms: Long) {
        binding.tvCurTime.text = TimeFunction.songDuration(ms / 1000L)
    }
}