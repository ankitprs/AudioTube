package tech.apps.music.ui.fragments.song

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.Constants
import tech.apps.music.R
import tech.apps.music.database.offline.WatchLaterSongModel
import tech.apps.music.databinding.FragmentSongBinding
import tech.apps.music.mediaPlayerYT.YoutubeFloatingUI
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.ui.fragments.MainViewModel
import tech.apps.music.util.AdsFunctions
import tech.apps.music.util.secondInFloatToTimeString
import tech.apps.music.util.songDuration
import javax.inject.Inject


@AndroidEntryPoint
class SongFragment : Fragment() {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel
    private var _binding: FragmentSongBinding? = null
    private val binding: FragmentSongBinding get() = _binding!!

    private var shouldUpdateSeekbar: Boolean = true
    private var isWatchLater = false

    private var curPlayingSong: YTAudioDataModel? = null
    private var playbackState: PlaybackStateCompat? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        toggleShimmer(true)

        subscribeToObserver()
        AdsFunctions.showAds(requireActivity())

        if (AdsFunctions.lastTimeForShowingAds == 0L) {
            AdsFunctions.toShowDirect = true
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
            curPlayingSong?.let { it1 -> mainViewModel.playPauseToggleSong(it1.mediaId) }
        }
        binding.ivSkipPreviousSongFragment.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        binding.ivSkipNextSongFragment.setOnClickListener {
            mainViewModel.skipToNextSong()
        }
        binding.ivPreviousReplaySongFragment.setOnClickListener {
            Snackbar.make(it, "Under Development. Coming Soon...", Snackbar.LENGTH_SHORT).show()

            val animator = ObjectAnimator.ofFloat(it, View.ROTATION, 360f, 0f)
            animator.duration = 500
            animator.start()
        }
        binding.ivForwardSongFragment.setOnClickListener {
            Snackbar.make(it, "Under Development. Coming Soon...", Snackbar.LENGTH_SHORT).show()

            val animator = ObjectAnimator.ofFloat(it, View.ROTATION, -360f, 0f)
            animator.duration = 500
            animator.start()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.tvCurTime.text = songDuration(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.setMusicSeekTo(it.progress.toFloat())
                }
                shouldUpdateSeekbar = true
            }
        })
        binding.timerIconImage.setOnClickListener {
            handleSleepTimer(it)
        }

        binding.exitButtonSongFragment.setOnClickListener {
            findNavController().navigateUp()
        }

        checkPlaybackSpeed()
        binding.materialCardViewDisVisible.setOnClickListener {
            handleSpeedController(it)
        }
        binding.imageViewEpisodesListButton.setOnClickListener {
            findNavController().navigate(R.id.action_songFragment_to_episodesListFragment)
        }
        bookMarkToggle()
        toggleRepeatMode()
    }

    private fun setPlaybackSpeed(playSpeed: Float = 1f, playSpeedString: String = "1.00x") {
        val sharedPref = requireActivity().getSharedPreferences(
            Constants.SHARED_PREF_PLAYBACK_SPEED,
            AppCompatActivity.MODE_PRIVATE
        )
        val sharedPrefEditor = sharedPref.edit()
        sharedPrefEditor.putFloat(Constants.SAVE_PLAYBACK_SPEED, playSpeed)
        sharedPrefEditor.apply()

        binding.materialCardViewDisVisible.text = playSpeedString
    }

    private fun checkPlaybackSpeed() {
        val sharedPref = requireActivity().getSharedPreferences(
            Constants.SHARED_PREF_PLAYBACK_SPEED,
            AppCompatActivity.MODE_PRIVATE
        )
        val str: String = when (sharedPref.getFloat(Constants.SAVE_PLAYBACK_SPEED, 1f)) {
            0.25f -> "0.25x"
            0.5f -> "0.50x"
            1f -> "1.00x"
            1.5f -> "1.50x"
            2f -> "2.00x"
            else -> "1.00x"
        }
        binding.materialCardViewDisVisible.text = str
    }

    private fun updateTitleAndSongImage(song: YTAudioDataModel) {
        toggleShimmer(song.title == "null")
        updateSongImage(song.thumbnailUrl)
        binding.songTitleSongFragment.text = song.title
        binding.songAuthorSongFragment.text = song.author
    }

    private fun updateSongImage(thumbnailUrl: String) {
        glide.asBitmap()
            .load(thumbnailUrl)
            .centerCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    binding.songThumbnailSongFragment.setImageBitmap(resource)
                    Palette.from(resource).generate {
                        val gradientDrawable = GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM,
                            intArrayOf(
                                it?.mutedSwatch?.rgb ?: ContextCompat.getColor(
                                    requireActivity(),
                                    R.color.dark_background
                                ),
                                ContextCompat.getColor(
                                    requireActivity(),
                                    R.color.dark_background
                                )
                            )
                        )
                        gradientDrawable.cornerRadius = 0f
                        binding.songFragmentContainerLinearLayout.background = gradientDrawable
                        val window = requireActivity().window
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                        window.statusBarColor = (
                                it?.mutedSwatch?.rgb ?: ContextCompat.getColor(
                                    requireActivity(),
                                    R.color.dark_background
                                    )
                                )
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
    }

    private fun subscribeToObserver() {
        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
//            isPlayingIconToggle(playbackState?.isPlaying == true)
        }
        YoutubeFloatingUI.isPlaying.observe(viewLifecycleOwner) {
            isPlayingIconToggle(it)
        }
        mainViewModel.currentlyPlayingSong.observe(viewLifecycleOwner) {
            it?.let { it1 -> updateTitleAndSongImage(it1) }
            curPlayingSong = it
        }
        mainViewModel.bufferingTime.observe(viewLifecycleOwner) {
            binding.progressBarForBuffering.isVisible = it
        }
        YoutubeFloatingUI.curSongDuration.observe(viewLifecycleOwner) {
            if (it != null)
                binding.tvSongDuration.text = secondInFloatToTimeString(it)
            binding.seekBar.max = it?.toInt() ?: 0
        }
        YoutubeFloatingUI.currentTime.observe(viewLifecycleOwner) {
            if (it != null && shouldUpdateSeekbar) {
                binding.tvCurTime.text = secondInFloatToTimeString(it)
                binding.seekBar.progress = it.toInt()
            }
        }
    }

    private fun bookMarkToggle() {
        mainViewModel.getWatchLaterList { list ->
            isWatchLater = list.find {
                it.videoId == curPlayingSong?.mediaId
            } != null
            watchLaterIconToggle()
        }
        binding.imageViewBookMarkButton.setOnClickListener {
            if (isWatchLater) {
                curPlayingSong?.let { it1 ->
                    mainViewModel.removeSongListenLater(it1.mediaId)
                }
            } else {
                curPlayingSong?.let { it1 ->

                    if (it1.title == "null")
                        return@setOnClickListener

                    mainViewModel.songListenLater(
                        WatchLaterSongModel(
                            it1.mediaId,
                            it1.title,
                            it1.author,
                            it1.duration,
                            System.currentTimeMillis()
                        )
                    )
                }
            }
            isWatchLater = !isWatchLater
            watchLaterIconToggle()
        }
    }

    private fun watchLaterIconToggle() {
        if (isWatchLater) {
            binding.imageViewBookMarkButton.setImageResource(R.drawable.ic_round_bookmark_24)
        } else {
            binding.imageViewBookMarkButton.setImageResource(R.drawable.ic_round_bookmark_border_24)
        }
    }

    private fun handleSpeedController(view: View) {
        val popupMenu = PopupMenu(requireActivity(), view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.speed_control_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.speedControlMenu025 -> {
                    mainViewModel.setMusicPlaybackRate(0.25F)
                    setPlaybackSpeed(0.25f, "0.25x")
                }
                R.id.speedControlMenu05 -> {
                    mainViewModel.setMusicPlaybackRate(0.5F)
                    setPlaybackSpeed(0.5f, "0.50x")
                }
                R.id.speedControlMenu10 -> {
                    mainViewModel.setMusicPlaybackRate(1F)
                    setPlaybackSpeed(1f, "1.00x")
                }
                R.id.speedControlMenu15 -> {
                    mainViewModel.setMusicPlaybackRate(1.5F)
                    setPlaybackSpeed(1.5f, "1.50x")
                }
                R.id.speedControlMenu20 -> {
                    mainViewModel.setMusicPlaybackRate(2F)
                    setPlaybackSpeed(2f, "2.00x")
                }
                else -> {
                    mainViewModel.setMusicPlaybackRate(1F)
                    setPlaybackSpeed(1f, "1x")
                }
            }
            true
        }
    }

    private fun handleSleepTimer(view: View) {
        val popupMenu = PopupMenu(requireActivity(), view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.sleep_timer_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.sleepAfter10m -> {
                    mainViewModel.sleepAfterTimer(10 * 60 * 1000L)
                }
                R.id.sleepAfter15m -> {
                    mainViewModel.sleepAfterTimer(15 * 60 * 1000L)
                }
                R.id.sleepAfter30m -> {
                    mainViewModel.sleepAfterTimer(30 * 60 * 1000L)
                }
                R.id.sleepAfter45m -> {
                    mainViewModel.sleepAfterTimer(45 * 60 * 1000L)
                }
                R.id.sleepAfter1h -> {
                    mainViewModel.sleepAfterTimer(60 * 60 * 1000L)
                }
            }
            true
        }
    }

    private fun toggleShimmer(isShimmer: Boolean) {
        binding.shimmerViewContainerSongFragment.isVisible = isShimmer
        binding.contentContainerSongFragment.isVisible = !isShimmer
    }

    private fun toggleRepeatMode() {
        binding.imageViewRepeatButton.setOnClickListener {
            YoutubeFloatingUI.repeatMode = !YoutubeFloatingUI.repeatMode
            toggleRepeatIcon()
        }
        toggleRepeatIcon()
    }

    private fun toggleRepeatIcon() {
        if (YoutubeFloatingUI.repeatMode) {
            binding.imageViewRepeatButton.setImageResource(R.drawable.ic_baseline_repeat_24)
        } else {
            binding.imageViewRepeatButton.setImageResource(R.drawable.ic_baseline_repeat_off_24)
        }
    }

    private fun isPlayingIconToggle(isPlaying: Boolean) {
        binding.ivPlayPauseSongFragmentImageView.setImageResource(
            if (isPlaying)
                R.drawable.ic_round_pause_circle_24
            else
                R.drawable.ic_round_play_circle_24
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}