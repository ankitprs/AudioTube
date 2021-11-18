package tech.apps.music.ui.educate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tech.apps.music.R

class EducateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_PlayAudio)
        setContentView(R.layout.educate_activity)
    }
}