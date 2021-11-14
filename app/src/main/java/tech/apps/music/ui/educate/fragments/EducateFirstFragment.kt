package tech.apps.music.ui.educate.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.educate_first_fragment.*
import tech.apps.music.R

class EducateFirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.educate_first_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        extendedFloatingActionButtonEducateFirst.setOnClickListener{
            findNavController().navigate(R.id.action_educateFirstFragment_to_educateSecondFragment)
        }
    }
}