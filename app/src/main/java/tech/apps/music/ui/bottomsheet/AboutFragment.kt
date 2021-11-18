package tech.apps.music.ui.bottomsheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.apps.music.BuildConfig
import tech.apps.music.R
import tech.apps.music.others.Constants.ABOUT
import tech.apps.music.others.Constants.ABOUT_SENDING_DATA
import tech.apps.music.others.Constants.HOW_IT_WORKS
import tech.apps.music.others.Constants.PRIVACY_POLICY

class AboutFragment : BottomSheetDialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        val aboutValue = arguments?.getString(ABOUT_SENDING_DATA)

        val aboutFragmentTitle = view.findViewById<TextView>(R.id.aboutFragmentTitle)
        val aboutFragmentDescription = view.findViewById<TextView>(R.id.aboutFragmentDescription)

        if (aboutValue != null) {

            when (aboutValue) {
                ABOUT -> {
                    aboutFragmentTitle.text = "About"
                    aboutFragmentDescription.text = "AudioTube - Version ${BuildConfig.VERSION_NAME}"
                }
                PRIVACY_POLICY -> {
                    aboutFragmentTitle.text = "Privacy Policy"
                    aboutFragmentDescription.setText(R.string.privacy_policy)
                }
                HOW_IT_WORKS -> {
                    aboutFragmentTitle.text = "How it Works"
                    aboutFragmentDescription.setText(R.string.how_it_works)
                }
                else -> {
                    aboutFragmentTitle.text = "About"
                    aboutFragmentDescription.text = "AudioTube - Version ${BuildConfig.VERSION_NAME}"
                }
            }
        }

        view.findViewById<ImageView>(R.id.aboutFragmentDismiss).setOnClickListener {
            this.dismiss()
        }

        return view
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        val aboutValue = arguments?.getString(ABOUT_SENDING_DATA)
        if(aboutValue!=null){
            if(aboutValue==PRIVACY_POLICY){
                dialog.setOnShowListener { dialogInterface ->

                    val bottomSheetDialog = dialogInterface as BottomSheetDialog
                    val parentLayout =
                        bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

                    parentLayout?.let {
                        val behaviour = BottomSheetBehavior.from(it)
                        setupFullHeight(it)
                        behaviour.state = BottomSheetBehavior.STATE_EXPANDED

                    }
                }
            }
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

}