package tech.apps.music.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.RequestManager
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.apps.music.ConnectionLiveData
import tech.apps.music.R
import tech.apps.music.databinding.HomeActivityBinding
import tech.apps.music.mediaPlayerYT.MusicService
import tech.apps.music.mediaPlayerYT.YoutubeFloatingUI
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.ui.fragments.MainViewModel
import tech.apps.music.util.AdsFunctions
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    companion object {
        // Facebook Audience Network
        const val PLACEMENT_ID = "672729120837013_692869968822928"
        var interstitialAd: InterstitialAd? = null

        // Google Admob
        var rewardedInterstitialAd: RewardedInterstitialAd? = null
        const val APP_ID = "ca-app-pub-8154643218867307/4993051585"
    }

    val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var glide: RequestManager
    private var curPlaying: YTAudioDataModel? = null

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    private var _binding: HomeActivityBinding? = null
    private val binding: HomeActivityBinding get() = _binding!!
    private lateinit var navController: NavController

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_PlayAudio)
        _binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val connection = ConnectionLiveData(this)
        connection.observe(this) {
            binding.noInternetConnectionView.isVisible = it != true
        }
        AudienceNetworkAds.initialize(this)
        MobileAds.initialize(this)
        startFloatingService()

        viewModel.currentlyPlayingSong.observe(this) {
            curPlaying = it
            glide.load(curPlaying?.thumbnailUrl).into(binding.ivCurSongImage)
            binding.vpSong.text = curPlaying?.title ?: ""
        }

        binding.ivPlayPause.setOnClickListener {
            curPlaying?.let {
                viewModel.playPauseToggleSong(it.mediaId)
            }
        }

        curPlaying?.let { viewModel.playPauseToggleSong(it.mediaId) }

        navController = findNavController(R.id.navHostFragmentContainerHAct)
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            run {
                when (destination.id) {
                    R.id.songFragment -> {
                        showOrHideBottomBar(boolean = false, isNotSong = false)
                    }
                    R.id.episodesListFragment -> showOrHideBottomBar(
                        boolean = false,
                        isNotSong = false
                    )
                    R.id.searchFragment -> {
                        showOrHideBottomBar(false, isNotSong = false)
                    }
                    else -> {
                        if (viewModel.currentlyPlayingSong.value != null) {
                            showOrHideBottomBar(true)
                        } else {
                            showOrHideBottomBar(false)
                        }
                    }
                }
            }
        }

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent)
                }
            }
            else -> {}
        }

        binding.materialCardViewHome.setOnClickListener {
            if (!binding.materialCardViewHome.isVisible)
                return@setOnClickListener
            navController.navigate(
                R.id.action_homeFragment2_to_songFragment2
            )
            AdsFunctions.showAds(this)
        }

        YoutubeFloatingUI.isPlaying.observe(this) {
            isPlayingIconToggle(it)
        }
        AdsFunctions.loadAds(this)
    }

    private fun showOrHideBottomBar(boolean: Boolean, isNotSong: Boolean = true) {
        if (boolean) {
            binding.materialCardViewHome.apply {
                animate()
                    .alpha(1f)
                    .setDuration(400)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            isVisible = true
                        }
                    })
            }
        } else {
            binding.materialCardViewHome.apply {
                animate()
                    .alpha(0.0f)
                    .setDuration(100)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            isVisible = false
                        }
                    })
            }
        }
        if (isNotSong) {
            binding.bottomNavigation.apply {
                isVisible = isNotSong
                animate()
                    .translationYBy(-this.height.toFloat())
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                            super.onAnimationEnd(animation, isReverse)
                            isVisible = isNotSong
                        }
                    })
            }
        } else {
            binding.bottomNavigation.apply {
                animate()
                    .translationY(0f)
                    .translationYBy(this.height.toFloat())
                    .alpha(0.0f)
                    .setDuration(400)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                            super.onAnimationEnd(animation, isReverse)
                            isVisible = isNotSong
                        }
                    })
            }
        }
//        bottom_navigation.isVisible = isNotSong
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {

            val bundle = Bundle()
            bundle.putString("SearchFORM_DIRECT_LINK", it)
            FirebaseAnalytics.Event.SEARCH
            firebaseAnalytics.logEvent("MusicRequestFROM_SEARCH_PAGE", bundle)

            val dialog = Dialog(this)

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_card_item)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            CoroutineScope(Dispatchers.IO).launch {

                val pair = viewModel.getVideoData(it)
                if (pair != null) {
                    viewModel.playListOfSongs(
                        listOf(
                            YTAudioDataModel(
                                viewModel.getVideoIdFromUrl(it) ?: "",
                                pair.first,
                                pair.second
                            )
                        )
                    )
                    withContext(Dispatchers.Main) {
                        navController.navigate(
                            R.id.action_homeFragment2_to_songFragment2
                        )
                    }
                } else {
                    Snackbar.make(
                        binding.root,
                        "Only Works with Youtube video Url",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                dialog.dismiss()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (this.intent != intent)
            intent?.let { handleSendText(it) }
    }

    override fun onStop() {
        super.onStop()
        intent.data = null
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun isPlayingIconToggle(isPlaying: Boolean) {
        binding.ivPlayPause.setImageResource(
            if (isPlaying)
                R.drawable.ic_round_pause_circle_24
            else
                R.drawable.ic_round_play_circle_24
        )
    }

    private fun startFloatingService() {

        if (isMyServiceRunning(MusicService::class.java))
            return

        val intent = Intent(this, MusicService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(intent)
        } else {
            applicationContext.startService(intent)
        }
    }


    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {

        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.runningAppProcesses) {
            if (serviceClass.name == service.processName) {
                return true
            }
        }
        return false
    }
}