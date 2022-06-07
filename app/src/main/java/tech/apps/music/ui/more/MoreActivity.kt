package tech.apps.music.ui.more

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.apps.music.BuildConfig
import tech.apps.music.R
import tech.apps.music.database.Repository
import tech.apps.music.databinding.ActivityMoreBinding
import tech.apps.music.others.Constants.ABOUT
import tech.apps.music.others.Constants.ABOUT_SENDING_DATA
import tech.apps.music.others.Constants.HOW_IT_WORKS
import tech.apps.music.others.Constants.PRIVACY_POLICY
import javax.inject.Inject

@AndroidEntryPoint
class MoreActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: Repository
    private var _binding: ActivityMoreBinding? = null
    private val binding: ActivityMoreBinding get() = _binding!!

    @SuppressLint("UseCompatLoadingForDrawables", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButtonMoreAct.setOnClickListener {
            finish()
        }

        val fragment = AboutFragment()
        val bundle = Bundle()

        binding.deleteRecentSongs.setOnClickListener {

            MaterialAlertDialogBuilder(this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete all the history songs")
                .setIcon(R.drawable.ic_baseline_delete_24)
                .setBackground(ColorDrawable(Color.parseColor("#FFFFFF")))
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton("Delete") { dialog, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.deleteAllHistory()
                    }
                    Snackbar.make(it, "Deleted all the history", Snackbar.LENGTH_LONG).show()
                    dialog.cancel()
                }
                .show()

        }

        binding.deleteSearchSuggest.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                repository.deleteAllSearchHistory()
                Snackbar.make(it, "Deleted all search history", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.howToUse.setOnClickListener {
            bundle.putString(ABOUT_SENDING_DATA, HOW_IT_WORKS)
            fragment.arguments = bundle
            fragment.show(supportFragmentManager, fragment.tag)
        }

        binding.notification.setOnClickListener {

        }

        binding.rateUs.setOnClickListener {
            Toast.makeText(this, "Thank You 😊 We Love Your Support", Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=tech.apps.music")
            startActivity(intent)
        }

        binding.checkUpdate.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=tech.apps.music")
            startActivity(intent)
        }

        binding.inviteFriends.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "AudioTube app")
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=tech.apps.music"
            )
            startActivity(Intent.createChooser(intent, "Share URL"))
        }

        binding.PrivacyPolicy.setOnClickListener {
            bundle.putString(ABOUT_SENDING_DATA, PRIVACY_POLICY)
            fragment.arguments = bundle

            fragment.show(supportFragmentManager, fragment.tag)
        }

        binding.about.setOnClickListener {
            bundle.putString(ABOUT_SENDING_DATA, ABOUT)
            fragment.arguments = bundle
            fragment.show(supportFragmentManager, fragment.tag)
        }

        binding.feedback.setOnClickListener {
            Toast.makeText(this, "Thank You 😊 We Love Feedback", Toast.LENGTH_SHORT).show()
            val text =
                "App Version - ${BuildConfig.VERSION_NAME}\n" +
                        "SDK Version - ${Build.VERSION.SDK_INT}\n" +
                        "\n Feedback -\n\n   "
            composeEmail(arrayOf("ankitpr2001@gmail.com"), "FeedBack - AudioTube", text)
        }

        binding.notificationSwitch.setOnClickListener {
            Toast.makeText(this, "Coming soon...", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun composeEmail(addresses: Array<String>, subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}