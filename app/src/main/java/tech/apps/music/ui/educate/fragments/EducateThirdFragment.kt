package tech.apps.music.ui.educate.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_educate_third.*
import tech.apps.music.R
import tech.apps.music.others.Constants
import tech.apps.music.ui.HomeActivity

class EducateThirdFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_educate_third, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonEducateThird.setOnClickListener {
            savePreferencesData()
            startActivity(Intent(activity, HomeActivity::class.java))
            activity?.finish()
        }

    }
    private fun savePreferencesData(){
        val sharedPrefEditor = requireActivity().getSharedPreferences(
            Constants.SHARED_PREF_APP_INTRO,
            AppCompatActivity.MODE_PRIVATE
        ).edit()
        sharedPrefEditor.putBoolean(Constants.WELCOME_PAGE_SHOWED_STATUS,true)
        sharedPrefEditor.apply()
    }
}