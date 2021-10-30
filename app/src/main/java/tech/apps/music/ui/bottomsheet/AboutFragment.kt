package tech.apps.music.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.apps.music.R
import tech.apps.music.others.Constants.ABOUT
import tech.apps.music.others.Constants.ABOUT_SENDING_DATA
import tech.apps.music.others.Constants.HOW_IT_WORKS
import tech.apps.music.others.Constants.PRIVACY_POLICY

class AboutFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_about, container, false)
        val aboutValue=arguments?.getString(ABOUT_SENDING_DATA)
        println(aboutValue)
        val aboutFragmentTitle=view.findViewById<TextView>(R.id.aboutFragmentTitle)
        val aboutFragmentDescription=view.findViewById<TextView>(R.id.aboutFragmentDescription)

        if(aboutValue != null){

            when(aboutValue){
                ABOUT->{
                    aboutFragmentTitle.text = "About"
                    aboutFragmentDescription.setText(R.string.about_des)
                }
                PRIVACY_POLICY->{
                    aboutFragmentTitle.text = "PRIVACY POLICY"
                    aboutFragmentDescription.setText(R.string.privacy_policy)
                }
                HOW_IT_WORKS->{
                    aboutFragmentTitle.setText("How it Works")
                    aboutFragmentDescription.setText(R.string.how_it_works)
                }
                else->{
                    aboutFragmentTitle.text = "About"
                    aboutFragmentDescription.setText(R.string.about_des)
                }
            }
        }

        view.findViewById<ImageView>(R.id.aboutFragmentDismiss).setOnClickListener {
            this.dismiss()
        }

        return view
    }
}