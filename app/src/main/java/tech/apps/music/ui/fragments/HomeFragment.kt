package tech.apps.music.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_activity.*
import kotlinx.android.synthetic.main.main_fragment.*
import tech.apps.music.R
import tech.apps.music.adapters.ExploreAdapter
import tech.apps.music.adapters.RecentAdapter
import tech.apps.music.model.ExploreModel
import tech.apps.music.others.Constants
import tech.apps.music.ui.bottomsheet.AboutFragment
import tech.apps.music.ui.more.MoreActivity
import tech.apps.music.ui.viewmodels.MainViewModel
import javax.inject.Inject




@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    @Inject
    lateinit var recentAudioAdapter: RecentAdapter

    @Inject
    lateinit var exploreAdapter: ExploreAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setUpRecyclerView()
//        subscribeToObservers()

        val firebaseAnalytics = activity?.let { FirebaseAnalytics.getInstance(it) }

        MaterialToolbarHome.inflateMenu(R.menu.home_menu)

        MaterialToolbarHome.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.app_bar_more -> {
                    startActivity(Intent(activity, MoreActivity::class.java))
                }
                R.id.app_bar_howToUse -> {
                    val fragment = AboutFragment()
                    val bundle = Bundle()
                    bundle.putString(Constants.ABOUT_SENDING_DATA, Constants.HOW_IT_WORKS)
                    fragment.arguments = bundle
                    fragment.show(requireActivity().supportFragmentManager, fragment.tag)
                }
                R.id.app_bar_rate -> {
                    Toast.makeText(
                        activity,
                        "Thank You 😊 We Love Your Support",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data =
                        Uri.parse("https://play.google.com/store/apps/details?id=tech.apps.music")
                    startActivity(intent)
                }
                R.id.app_bar_share -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_SUBJECT, "AudioTube app")
                    intent.putExtra(
                        Intent.EXTRA_TEXT,
                        "https://play.google.com/store/apps/details?id=tech.apps.music"
                    )
                    startActivity(Intent.createChooser(intent, "Share URL"))
                }
                R.id.app_bar_search -> {
                    findNavController().navigate(R.id.action_homeFragment2_to_searchFragment)
                }
                else -> {}
            }
            true
        }

        recentAudioAdapter.setItemClickListener {
            viewModel.playOrToggleSong(it)
            navHostFragment.findNavController().navigate(
                R.id.action_homeFragment2_to_songFragment2
            )
        }

        exploreAdapter.setItemClickListener {
            val bundle = Bundle()
            bundle.putString(Constants.PASS_EXPLORE_KEYWORDS, it.keyword)
            findNavController().navigate(R.id.action_homeFragment2_to_searchFragment,bundle)
        }
    }

    private fun setUpRecyclerView() {

        recyclerViewExploreMainFragment.apply {
            adapter = exploreAdapter
            layoutManager = GridLayoutManager(
                requireContext(),
                2
            )
        }

        recyclerViewRecent.apply {
            adapter = recentAudioAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
        addingSongIntoRecyclerView()
    }

//    private fun subscribeToObservers() {
//        viewModel.mediaItems.observe(viewLifecycleOwner) {
//            when (it.status) {
//                Status.SUCCESS -> {
////                    recentRecyclerViewProgressBar.visibility=View.GONE
//                }
//                Status.ERROR -> Unit
//                Status.LOADING -> {
////                    recentRecyclerViewProgressBar.visibility=View.VISIBLE
//                }
//            }
//        }
//    }

    private fun addingSongIntoRecyclerView() {
        viewModel.getRecentList.observe(viewLifecycleOwner) {
            recentAudioAdapter.songs = it
            if (it.size > 5) {
                viewModel.deleteRecentlyAdded5More(it[5].time)
            }
            recentTextTextView.isVisible = !it.isNullOrEmpty()
        }
        exploreAdapter.songs = creatingListOfExplores()
    }


    private fun creatingListOfExplores(): ArrayList<ExploreModel> {

        val list: ArrayList<ExploreModel> = ArrayList()

        list.add(ExploreModel("Music","Music", R.color.violate,R.drawable.ic_baseline_music_note_24))
        list.add(ExploreModel("Podcast","Podcast", R.color.button_color,R.drawable.ic_baseline_podcasts_24))
        list.add(ExploreModel("AudioBooks","AudioBooks", R.color.dark_green,R.drawable.ic_baseline_menu_book_24))
        list.add(ExploreModel("Focus Music","Focus", R.color.navi_blue,R.drawable.ic_focus_icon))
        list.add(ExploreModel("Workout Music","Workout", R.color.brown,R.drawable.ic_workout_icon))
        list.add(ExploreModel("Songs","Songs", R.color.dark_red,R.drawable.ic_music_icon))
        list.add(ExploreModel("Meditation Music","Meditation", R.color.orange,R.drawable.ic_meditation_icon))
        list.add(ExploreModel("Sleep Music","Sleep", R.color.dark_violate,R.drawable.ic_sleep_icon))
        list.add(ExploreModel("News","News", R.color.diff_green,R.drawable.ic_baseline_newspaper_24))
        list.add(ExploreModel("Biography","Biography", R.color.marune,R.drawable.ic_biography_icon))

        return list
    }
}