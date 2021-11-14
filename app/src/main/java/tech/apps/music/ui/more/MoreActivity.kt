package tech.apps.music.ui.more

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_more.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.apps.music.R
import tech.apps.music.database.Repository
import tech.apps.music.others.Constants.ABOUT
import tech.apps.music.others.Constants.ABOUT_SENDING_DATA
import tech.apps.music.others.Constants.HOW_IT_WORKS
import tech.apps.music.others.Constants.PRIVACY_POLICY
import tech.apps.music.ui.bottomsheet.AboutFragment

class MoreActivity : AppCompatActivity() {

    @SuppressLint("UseCompatLoadingForDrawables", "ResourceAsColor")
    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.themeWithActionBar)
        setContentView(R.layout.activity_more)

        supportActionBar?.title = "More"

        val fragment = AboutFragment()
        val bundle = Bundle()

        deleteRecentSongs.setOnClickListener {

            val repository = Repository(this)
            GlobalScope.launch(Dispatchers.IO) {
                repository.deleteAllHistory()
            }
            Snackbar.make(it, "Deleted All the History", Snackbar.LENGTH_LONG).show()
        }

        howToUse.setOnClickListener {
            bundle.putString(ABOUT_SENDING_DATA, HOW_IT_WORKS)
            fragment.arguments = bundle
            fragment.show(supportFragmentManager, fragment.tag)
        }

        notification.setOnClickListener {

        }

        rateUs.setOnClickListener {
            Toast.makeText(this, "Thank You 😊 We Love Your Support", Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=tech.apps.music")
            startActivity(intent)
        }

        checkUpdate.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=tech.apps.music")
            startActivity(intent)
        }

        inviteFriends.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "AudioTube app")
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=tech.apps.music"
            )
            startActivity(Intent.createChooser(intent, "Share URL"))
        }

        PrivacyPolicy.setOnClickListener {
            bundle.putString(ABOUT_SENDING_DATA, PRIVACY_POLICY)
            fragment.arguments = bundle

            fragment.show(supportFragmentManager, fragment.tag)
        }

        about.setOnClickListener {
            bundle.putString(ABOUT_SENDING_DATA, ABOUT)
            fragment.arguments = bundle
            fragment.show(supportFragmentManager, fragment.tag)
        }

        feedback.setOnClickListener {
            Toast.makeText(this, "Thank You 😊 We Love Feedback", Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://forms.gle/A31zzznjCfmHkr6AA")
            startActivity(intent)
        }
    }
}