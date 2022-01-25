package tech.apps.music.ui.fragments.song

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.explore_list_item.view.*
import tech.apps.music.R
import tech.apps.music.database.offline.WatchLaterSongModel
import tech.apps.music.databinding.FragmentSongBinding
import tech.apps.music.exoplayer.isPlaying
import tech.apps.music.exoplayer.toSong
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.others.Constants
import tech.apps.music.others.Status
import tech.apps.music.ui.fragments.MainViewModel
import tech.apps.music.util.TimeFunction
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment() {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel: SongViewModel by viewModels()
    private var _binding: FragmentSongBinding? = null
    private val binding: FragmentSongBinding get() = _binding!!

    private var curPlayingSong: YTAudioDataModel? = null
    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekbar: Boolean = true
    private var isWatchLater = false

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
            mainViewModel.skipToPreviousSong()
        }

        binding.ivSkipNextSongFragment.setOnClickListener {
            mainViewModel.skipToNextSong()

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

        checkPlaybackSpeed()
        binding.materialCardViewDisVisible.setOnClickListener {
            handleSpeedController(it)
        }
        binding.imageViewEpisodesListButton.setOnClickListener {
            findNavController().navigate(R.id.action_songFragment_to_episodesListFragment)
        }
        bookMarkToggle()
        bookMarkToggleSetOnClickListener()
    }

    private fun setPlaybackSpeed(playSpeed: Float = 1f, playSpeedString: String = "1x") {

        val sharedPref = requireActivity().getSharedPreferences(
            Constants.SHARED_PREF_PLAYBACK_SPEED,
            AppCompatActivity.MODE_PRIVATE
        )

        val sharedPrefEditor = sharedPref.edit()
        sharedPrefEditor.putFloat(Constants.SAVE_PLAYBACK_SPEED, playSpeed)
        sharedPrefEditor.apply()

        binding.materialCardViewDisVisible.text = playSpeedString
        changePlaybackSpeedState(playSpeed)
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
            0.25f -> {
                "0.25x"
            }
            0.5f -> {
                "0.5x"
            }
            0.75f -> {
                "0.75x"
            }
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

        binding.materialCardViewDisVisible.text = str
        changePlaybackSpeedState(playbackSpeed)
    }

    private fun updateTitleAndSongImage(song: YTAudioDataModel) {
        toggleShimmer(song.title == "null")
        updateSongImage(song.thumbnailUrl)
        binding.songTitleSongFragment.text = song.title
        binding.songAuthorSongFragment.text = song.author
    }

    private fun updateSongImage(thumbnailUrl: String) {
//        if (mainViewModel.isYoutubeVideoCurSong()) {
//            glide.load(thumbnailUrl)
//                .override(480, 270)
//                .centerCrop()
//                .into(binding.songThumbnailSongFragment)
//        } else {
//            glide.load(thumbnailUrl)
//                .centerCrop()
//                .into(binding.songThumbnailSongFragment)
//        }
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
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
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
            bookMarkToggle()
            updateTitleAndSongImage(curPlayingSong!!)
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
                curPlayingSong?.duration = it / 1000L
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

    private fun bookMarkToggle() {

        val list = mainViewModel.getWatchLaterList.value

        isWatchLater = list?.find {
            it.videoId == curPlayingSong?.mediaId
        } != null
        watchLaterIconToggle()
    }

    private fun bookMarkToggleSetOnClickListener() {
        binding.imageViewBookMarkButton.setOnClickListener {
            if (!mainViewModel.isYoutubeVideoCurSong()) {
                Snackbar.make(
                    it,
                    "Only Youtube video can be Bookmark as of now",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (isWatchLater) {
                isWatchLater = false
                curPlayingSong?.let { it1 ->
                    mainViewModel.removeSongListenLater( it1.mediaId )
                }
            } else {
                isWatchLater = true
                curPlayingSong?.let { it1 ->

                    if (it1.title == "null")
                        return@setOnClickListener

                    mainViewModel.songListenLater(
                        WatchLaterSongModel(
                            it1.mediaId,
                            it1.title,
                            it1.author,
                            it1.duration * 1000,
                            System.currentTimeMillis()
                        )
                    )
                }
            }
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
                    setPlaybackSpeed(0.25f, "0.25x")
                }
                R.id.speedControlMenu05 -> {
                    setPlaybackSpeed(0.5f, "0.5x")
                }
                R.id.speedControlMenu075 -> {
                    setPlaybackSpeed(0.75f, "0.75x")
                }
                R.id.speedControlMenu10 -> {
                    setPlaybackSpeed(1f, "1x")
                }
                R.id.speedControlMenu125 -> {
                    setPlaybackSpeed(1.25f, "1.25x")
                }
                R.id.speedControlMenu15 -> {
                    setPlaybackSpeed(1.5f, "1.5x")
                }
                R.id.speedControlMenu175 -> {
                    setPlaybackSpeed(1.75f, "1.75x")
                }
                R.id.speedControlMenu20 -> {
                    setPlaybackSpeed(2f, "2x")
                }
                else -> {
                    setPlaybackSpeed(1f, "1x")
                }
            }
            true
        }
    }

    private fun toggleShimmer(isShimmer: Boolean) {
        binding.shimmerViewContainerSongFragment.isVisible = isShimmer
        binding.contentContainerSongFragment.isVisible = !isShimmer
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}