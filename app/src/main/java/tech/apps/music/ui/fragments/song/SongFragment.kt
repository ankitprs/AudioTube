package tech.apps.music.ui.fragments.song

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.R
import tech.apps.music.database.offline.WatchLaterSongModel
import tech.apps.music.databinding.FragmentSongBinding
import tech.apps.music.floatingWindow.YoutubeFloatingUI
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.others.Constants
import tech.apps.music.ui.fragments.MainViewModel
import tech.apps.music.util.BasicStorage
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
    private var curPlayingSong: YTAudioDataModel? = null
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
        setUpNewAdsAndShow()

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
                    YoutubeFloatingUI.youtubePlayer?.seekTo(it.progress.toFloat())
                }
                shouldUpdateSeekbar = true
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
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
    }

    private fun subscribeToObserver() {
        YoutubeFloatingUI.isPlaying.observe(viewLifecycleOwner) {
            isPlayingIconToggle(it)
        }
        mainViewModel.getCurrentlyPlayingYTAudioModel.observe(viewLifecycleOwner) {
            if (it != null) {
                updateTitleAndSongImage(it)
            }
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
                    YoutubeFloatingUI.youtubePlayer?.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_0_25)
                    setPlaybackSpeed(0.25f, "0.25x")
                }
                R.id.speedControlMenu05 -> {
                    YoutubeFloatingUI.youtubePlayer?.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_0_5)
                    setPlaybackSpeed(0.5f, "0.50x")
                }
                R.id.speedControlMenu10 -> {
                    YoutubeFloatingUI.youtubePlayer?.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_1)
                    setPlaybackSpeed(1f, "1.00x")
                }
                R.id.speedControlMenu15 -> {
                    YoutubeFloatingUI.youtubePlayer?.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_1_5)
                    setPlaybackSpeed(1.5f, "1.50x")
                }
                R.id.speedControlMenu20 -> {
                    YoutubeFloatingUI.youtubePlayer?.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_2)
                    setPlaybackSpeed(2f, "2.00x")
                }
                else -> {
                    YoutubeFloatingUI.youtubePlayer?.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_1)
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

    private val PLACEMENT_ID = "672729120837013_692869968822928"
    private lateinit var interstitialAd: InterstitialAd
    private val TAG = "HomeActivityAds"

    private fun setUpNewAdsAndShow() {
        loadAd()

        if ((BasicStorage.lastTimeForShowingAds + 300000L) > System.currentTimeMillis())
            return

        BasicStorage.lastTimeForShowingAds = System.currentTimeMillis()

        interstitialAd = InterstitialAd(activity, PLACEMENT_ID)
        val interstitialAdListener: InterstitialAdListener = object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {
                // Interstitial ad displayed callback
                Log.e(TAG, "Interstitial ad displayed.")
            }

            override fun onInterstitialDismissed(ad: Ad) {
                // Interstitial dismissed callback
                Log.e(TAG, "Interstitial ad dismissed.")
            }

            override fun onError(ad: Ad?, adError: AdError) {
                // Ad error callback
                Log.e(TAG, "Interstitial ad failed to load: " + adError.errorMessage)
                BasicStorage.lastTimeForShowingAds = 0L
            }

            override fun onAdLoaded(ad: Ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!")
                // Show the ad
                showAdWithDelay()
            }

            override fun onAdClicked(ad: Ad) {
                // Ad clicked callback
                Log.d(TAG, "Interstitial ad clicked!")
            }

            override fun onLoggingImpression(ad: Ad) {
                // Ad impression logged callback
                Log.d(TAG, "Interstitial ad impression logged!")
            }
        }

        interstitialAd.loadAd(
            interstitialAd.buildLoadAdConfig()
                .withAdListener(interstitialAdListener)
                .build()
        )
    }

    private fun showAdWithDelay() {
        if (!interstitialAd.isAdLoaded) {
            Log.d(TAG, "interstitialAd.isAdLoaded")
            return
        }
        if (interstitialAd.isAdInvalidated) {
            Log.d(TAG, "interstitialAd.isAdInvalidated")
            return
        }
        Log.d(TAG, "interstitialAd.show()")

        BasicStorage.lastTimeForShowingAds = System.currentTimeMillis()
        interstitialAd.show()
    }

    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private val idApp = "ca-app-pub-8154643218867307/4993051585"
    private val idAppTest = "ca-app-pub-3940256099942544/5354046379"
    val TAG_GOOGLE = "HomeActivityAds_google"

    private fun loadAd() {
        if ((BasicStorage.lastTimeForShowingAds + 300000L) > System.currentTimeMillis())
            return

        RewardedInterstitialAd.load(requireContext(), idApp,
            AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    Log.e(TAG_GOOGLE, "onAdLoaded")
                    rewardedInterstitialAd?.show(requireActivity(), OnUserEarnedRewardListener() {
                        BasicStorage.lastTimeForShowingAds = System.currentTimeMillis()
                        fun onUserEarnedReward(rewardItem: RewardItem) {
                            Log.d(TAG_GOOGLE, "User earned the reward.")
                        }
                    })
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG_GOOGLE, "onAdFailedToLoad")
                    Log.d(TAG_GOOGLE, loadAdError.message)

                    BasicStorage.lastTimeForShowingAds = 0L
                }
            })
    }
}