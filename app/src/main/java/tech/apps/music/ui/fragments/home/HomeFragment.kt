package tech.apps.music.ui.fragments.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT
import com.google.android.gms.ads.nativead.NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE
import dagger.hilt.android.AndroidEntryPoint
import tech.apps.music.R
import tech.apps.music.adapters.ExploreAdapter
import tech.apps.music.adapters.SongAdapter
import tech.apps.music.databinding.MainFragmentBinding
import tech.apps.music.model.SongModelForList
import tech.apps.music.model.toSongModelForList
import tech.apps.music.model.toYtAudioDataModel
import tech.apps.music.others.Constants
import tech.apps.music.ui.fragments.MainViewModel
import tech.apps.music.ui.more.MoreActivity
import tech.apps.music.util.VideoData
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    @Inject
    lateinit var recentAudioAdapter: SongAdapter

    private lateinit var exploreAdapter: ExploreAdapter
    private var _binding: MainFragmentBinding? = null
    private val binding: MainFragmentBinding get() = _binding!!
    private var nativeAdGlobal: NativeAd? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        binding.MaterialToolbarHome.inflateMenu(R.menu.home_menu)
        binding.MaterialToolbarHome.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.app_bar_more -> {
                    startActivity(Intent(activity, MoreActivity::class.java))
                }
                R.id.app_bar_search -> {
                    findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
                }
                else -> {}
            }
            true
        }
        setUpRecyclerView()
        addingSongIntoRecyclerView()
        adsHandling()

        recentAudioAdapter.setItemClickListener {
            viewModel.changeIsYoutubeVideoCurSong(true)
            viewModel.playOrToggleListOfSongs(
                (listOf(it)).toYtAudioDataModel(),
                true,
                0,
                it.watchedPosition
            )
            findNavController().navigate(R.id.action_homeFragment2_to_songFragment2)
        }

        exploreAdapter.setItemClickListener {
            val bundle = Bundle()
            bundle.putString(Constants.PASS_EXPLORE_KEYWORDS, it.keyword)
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment, bundle)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun adsHandling() {
        val testAd = "ca-app-pub-3940256099942544/1044960115"
        val adUnitID = "ca-app-pub-8154643218867307/2178194367"
        val adLoader = AdLoader.Builder(requireActivity(), adUnitID)

            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT)
                    .setMediaAspectRatio(NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                    .build()
            )
            .forNativeAd { nativeAd ->

                nativeAdGlobal = nativeAd
                addingViewIntoNativeAds(nativeAd)

            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
//                    binding.nativeAdTemplateMainFrg.visibility = View.GONE
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    @SuppressLint("ResourceAsColor")
    private fun addingViewIntoNativeAds(nativeAd: NativeAd) {
        if(_binding == null)  return

        binding.nativeAdsMediumTemplateView.apply {

            binding.nativeAdTemplateMainFrg.mediaView = mediaView
            binding.nativeAdTemplateMainFrg.callToActionView = cta
            binding.nativeAdTemplateMainFrg.iconView = icon
            binding.nativeAdTemplateMainFrg.headlineView = headline
            binding.nativeAdTemplateMainFrg.advertiserView = advertiserName
            binding.nativeAdTemplateMainFrg.bodyView = description

            icon.setImageDrawable(nativeAd.icon?.drawable)
            headline.text = nativeAd.headline
            description.text = nativeAd.body
            cta.text = nativeAd.callToAction
            advertiserName.text = nativeAd.advertiser
        }
//        binding.nativeAdTemplateMainFrg.removeAllViews()
        binding.nativeAdTemplateMainFrg.setNativeAd(nativeAd)

    }

    private fun setUpRecyclerView() {
        exploreAdapter = ExploreAdapter()

        binding.recyclerViewContinueWatchMFrag.apply {
            adapter = recentAudioAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.recyclerViewLatestMFrag.apply {
            adapter = exploreAdapter
            layoutManager = GridLayoutManager(
                requireContext(),
                2
            )
        }
    }

    private fun addingSongIntoRecyclerView() {
        viewModel.getLast5RecentList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                recentAudioAdapter.songs = it.toSongModelForList()
            } else {
                recentAudioAdapter.songs = listOf(
                    SongModelForList()
                )
            }
        }
        exploreAdapter.songs = VideoData.creatingListOfExplores()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        nativeAdGlobal?.destroy()
        binding.recyclerViewContinueWatchMFrag.adapter = null
        binding.recyclerViewLatestMFrag.adapter = null
        _binding = null
    }
}