package tech.apps.music.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_activity.*
import tech.apps.music.R
import tech.apps.music.exoplayer.isPlaying
import tech.apps.music.exoplayer.toSong
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.others.Status
import tech.apps.music.ui.viewmodels.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var glide: RequestManager

    private var curPlaying: YTAudioDataModel? = null

    private var playbackState: PlaybackStateCompat? = null

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_PlayAudio)
        setContentView(R.layout.home_activity)

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

        materialCardViewHome.setOnClickListener {
            navHostFragment.findNavController().navigate(
                R.id.action_homeFragment2_to_songFragment2
            )
        }
        subscribeToObserver()

        viewModel.curPlayingSong.observe(this) {
            curPlaying = it?.toSong()
            glide.load(curPlaying?.thumbnailUrl).into(ivCurSongImage)
            vpSong.text = curPlaying?.title ?: ""
        }

        ivPlayPause.setOnClickListener {
            curPlaying?.let {
                viewModel.playOrToggleSong(it, true)
            }
        }

        curPlaying?.let { viewModel.playOrToggleSong(it) }

        val navController = navHostFragment.findNavController()
        NavigationUI.setupWithNavController(bottom_navigation, navController)

        navHostFragment.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            run {
                when (destination.id) {
                    R.id.songFragment -> showOrHideBottomBar(boolean = false, isNotSong = false)
                    R.id.homeFragment -> showOrHideBottomBar(true)
                    R.id.searchFragment -> {
                        showOrHideBottomBar(false)
                    }
                    else -> showOrHideBottomBar(true)
                }
            }
        }
    }

    private fun showOrHideBottomBar(boolean: Boolean, isNotSong: Boolean = true) {
        ivCurSongImage.isVisible = boolean
        vpSong.isVisible = boolean
        ivPlayPause.isVisible = boolean
        bottom_navigation.isVisible = isNotSong
    }


    private fun subscribeToObserver() {

        viewModel.playbackState.observe(this) {
            playbackState = it
            ivPlayPause.setImageResource(
                if (playbackState?.isPlaying == true)
                    R.drawable.exo_icon_pause
                else
                    R.drawable.exo_icon_play
            )
        }
        viewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> Snackbar.make(
                        window.decorView.rootView,
                        result.message ?: "An unKnown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
        viewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> Snackbar.make(
                        window.decorView.rootView,
                        result.message ?: "An unKnown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {

            val bundle = Bundle()
            bundle.putString("SearchFORM_DIRECT_LINK", it)
            firebaseAnalytics.logEvent("MusicRequestFROM_SEARCH_PAGE", bundle)

            val dialog = Dialog(this)

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_card_item)
            dialog.show()

            viewModel.addSongInRecent(it, this) { result ->
                if (result) {
                    navHostFragment.findNavController().navigate(
                        R.id.action_homeFragment2_to_songFragment2
                    )
                } else {
                    Toast.makeText(this, "Enter YT Video link & Play 🥳", Toast.LENGTH_SHORT).show()
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
}