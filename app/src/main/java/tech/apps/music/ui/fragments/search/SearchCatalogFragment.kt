package tech.apps.music.ui.fragments.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import tech.apps.music.R
import tech.apps.music.adapters.ExploreAdapter
import tech.apps.music.databinding.SearchCatalogFragmentBinding
import tech.apps.music.Constants
import tech.apps.music.util.creatingListOfExplores

@DelicateCoroutinesApi
@AndroidEntryPoint
class SearchCatalogFragment : Fragment() {

    private var _binding: SearchCatalogFragmentBinding? = null
    private  val binding: SearchCatalogFragmentBinding get() = _binding!!

    private lateinit var exploreAdapter: ExploreAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SearchCatalogFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(
            requireActivity(),
            R.color.dark_background
        )

        exploreAdapter = ExploreAdapter()

        binding.recyclerViewExploreMainFragment.apply {
            adapter = exploreAdapter
            layoutManager = GridLayoutManager(
                requireContext(),
                2
            )
        }
        exploreAdapter.songs = creatingListOfExplores()

        exploreAdapter.setItemClickListener {
            val bundle = Bundle()
            bundle.putString(Constants.PASS_EXPLORE_KEYWORDS, it.keyword)
            findNavController().navigate(R.id.action_searchCatalogFragment_to_searchFragment,bundle)
        }
        var isShow = false
        var scrollRange = -1

        binding.searchCatalogFragmentSearchBar.setOnClickListener{
            findNavController().navigate(R.id.action_searchCatalogFragment_to_searchFragment)
        }
        binding.searchCatalogFragmentSearchBarMaterialCardView.setOnClickListener {
            findNavController().navigate(R.id.action_searchCatalogFragment_to_searchFragment)
        }

        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->

            if (scrollRange == -1) {
                scrollRange = appBarLayout.totalScrollRange
            }
            if (scrollRange + verticalOffset == 0) {
                isShow = true
                binding.searchCatalogFragmentSearchBar.isVisible = true
            } else if (isShow) {
                isShow = false
                binding.searchCatalogFragmentSearchBar.isVisible = false
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewExploreMainFragment.adapter = null
        _binding = null
    }
}