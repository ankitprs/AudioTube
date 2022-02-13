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
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.apps.music.ConnectionLiveData
import tech.apps.music.R
import tech.apps.music.database.network.YoutubeVideoData
import tech.apps.music.databinding.HomeActivityBinding
import tech.apps.music.floatingWindow.ForegroundService
import tech.apps.music.floatingWindow.YoutubeFloatingUI
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.ui.fragments.MainViewModel
import tech.apps.music.util.BasicStorage
import javax.inject.Inject


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

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

        startFloatingService()

        val connection = ConnectionLiveData(this)

        BasicStorage.isNetworkConnected = connection
        connection.observe(this) {
            binding.noInternetConnectionView.isVisible = it != true
        }

        viewModel.getCurrentlyPlayingYTAudioModel.observe(this) {
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
                    else -> {
                        if (viewModel.getCurrentlyPlayingYTAudioModel.value != null) {
                            showOrHideBottomBar(true)
                        } else {
                            binding.materialCardViewHome.isVisible = false
                        }
                    }
                }
            }
        }

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Handle text being sent
                }
            }
            else -> {
                // Handle other intents, such as being started from the home screen
            }
        }

        binding.materialCardViewHome.setOnClickListener {
            navController.navigate(
                R.id.action_homeFragment2_to_songFragment2
            )
        }

        YoutubeFloatingUI.isPlaying.observe(this){
            isPlayingIconToggle(it)
        }
        binding.ivPlayPause.setOnClickListener {
            curPlaying?.mediaId?.let { it1 -> viewModel.playPauseToggleSong(it1) }
        }
    }

    private fun showOrHideBottomBar(boolean: Boolean, isNotSong: Boolean = true) {
        if (boolean) {
            binding.materialCardViewHome.apply {
                animate()
                    .alpha(1f)
                    .setDuration(400)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            visibility = View.VISIBLE
                        }
                    })
            }
        } else {
            binding.materialCardViewHome.apply {
                animate()
                    .alpha(0.0f)
                    .setDuration(100)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            visibility = View.GONE
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
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
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
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
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
                val youtubeVideoData = YoutubeVideoData()
                youtubeVideoData.getVideoData(it) { pair ->
                    if (pair != null) {
                        viewModel.playOrToggleListOfSongs(
                            listOf(
                                YTAudioDataModel(
                                    youtubeVideoData.getVideoIdFromUrl(it) ?: "",
                                    pair.first,
                                    pair.second
                                )
                            )
                        )
                        navController.navigate(
                            R.id.action_homeFragment2_to_songFragment2
                        )
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
        Log.i("HomeActivity", "isPlaying$isPlaying")
        binding.ivPlayPause.setImageResource(
            if (isPlaying)
                R.drawable.ic_round_pause_circle_24
            else
                R.drawable.ic_round_play_circle_24
        )
    }

    private fun startFloatingService(command: String = "") {

        if (isMyServiceRunning(ForegroundService::class.java))
            return

        val intent = Intent(this, ForegroundService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(intent)
        } else {
            this.startService(intent)
        }

    }


    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {

        try {
            val manager =
                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(
                Int.MAX_VALUE
            )) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }
}