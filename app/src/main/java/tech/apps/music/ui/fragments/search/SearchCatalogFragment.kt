package tech.apps.music.ui.fragments.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import tech.apps.music.others.Constants
import tech.apps.music.util.VideoData

@DelicateCoroutinesApi
@AndroidEntryPoint
class SearchCatalogFragment : Fragment() {

    private lateinit var binding: SearchCatalogFragmentBinding

    lateinit var exploreAdapter: ExploreAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SearchCatalogFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exploreAdapter = ExploreAdapter()

        binding.recyclerViewExploreMainFragment.apply {
            adapter = exploreAdapter
            layoutManager = GridLayoutManager(
                requireContext(),
                2
            )
        }
        exploreAdapter.songs = VideoData.creatingListOfExplores()

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
                isShow = false;
                binding.searchCatalogFragmentSearchBar.isVisible = false
            }
        })

    }
}