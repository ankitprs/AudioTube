package tech.apps.music.ui.fragments.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.adapters.PremiumListAdapter
import tech.apps.music.databinding.PremiumFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class PremiumFragment : Fragment() {

    private lateinit var viewModel: PremiumViewModel
    private lateinit var binding: PremiumFragmentBinding
    @Inject
    lateinit var premiumAdapter: PremiumListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[PremiumViewModel::class.java]
        binding = PremiumFragmentBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}