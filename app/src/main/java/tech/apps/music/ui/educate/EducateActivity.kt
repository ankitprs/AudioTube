package tech.apps.music.ui.educate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tech.apps.music.R
import tech.apps.music.others.Constants.WELCOME_PAGE_SHOWED_STATUS
import tech.apps.music.ui.HomeActivity

class EducateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_PlayAudio)
        setContentView(R.layout.educate_activity)

//        floatingActionButton2.setOnClickListener{
//            startActivity(Intent(this,HomeActivity::class.java))
//            finish()
//        }

        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val welcomePageShowed = sharedPref.getBoolean(WELCOME_PAGE_SHOWED_STATUS, false)

        if(welcomePageShowed){
            startActivity(Intent(this,HomeActivity::class.java))
            finish()
        }else{
            savePreferencesData()
        }
    }

    private fun savePreferencesData(){
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean(WELCOME_PAGE_SHOWED_STATUS,true)
            apply()
        }
    }
}