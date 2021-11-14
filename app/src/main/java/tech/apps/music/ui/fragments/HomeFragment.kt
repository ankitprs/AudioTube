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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_activity.*
import kotlinx.android.synthetic.main.main_fragment.*
import tech.apps.music.R
import tech.apps.music.adapters.LikedAdapter
import tech.apps.music.adapters.RecentAdapter
import tech.apps.music.others.Constants
import tech.apps.music.others.Status
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
    lateinit var likedAdapter: LikedAdapter

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
        subscribeToObservers()

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
                        Toast.makeText(activity, "Thank You 😊 We Love Your Support", Toast.LENGTH_SHORT).show()
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("https://play.google.com/store/apps/details?id=tech.apps.music")
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
                    else -> {}
                }
            true
        }

        youtubeLinkVideoButton.setOnClickListener {
            val fragment = AboutFragment()
            val bundle = Bundle()
            bundle.putString(Constants.ABOUT_SENDING_DATA, Constants.HOW_IT_WORKS)
            fragment.arguments = bundle
            fragment.show(requireActivity().supportFragmentManager, fragment.tag)
        }

        recentAudioAdapter.setItemClickListener {
            viewModel.playOrToggleSong(it)
            navHostFragment.findNavController().navigate(
                R.id.globalActionToSongFragment
            )
        }
        likedAdapter.setItemClickListener {
            viewModel.playOrToggleSong(it)
            navHostFragment.findNavController().navigate(
                R.id.globalActionToSongFragment
            )
        }

        searchButton.setOnClickListener {
            val text = editTextTextLink.text
            if (text.isNotEmpty()) {
                searchProgressBar.visibility = View.VISIBLE
                searchTextView.visibility = View.GONE
                val bundle = Bundle()
                bundle.putString("SearchFORM_EDIT_TEXT", text.toString())
                firebaseAnalytics?.logEvent("MusicRequestFROM_SEARCH_PAGE", bundle)

                activity?.let { it1 ->
                    viewModel.addSongInRecent(text.toString(), it1) { status ->
                        if (status) {
                            searchProgressBar.visibility = View.GONE
                            searchTextView.visibility = View.VISIBLE

                            navHostFragment.findNavController().navigate(
                                R.id.globalActionToSongFragment
                            )
                            editTextTextLink.text.clear()

                        } else {
                            searchProgressBar.visibility = View.GONE
                            searchTextView.visibility = View.VISIBLE
                            Toast.makeText(
                                activity,
                                "Enter YT Video link & Play 🥳",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(activity, "Enter YT Video link & Play 🥳", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpRecyclerView() {
        recyclerViewLiked.apply {
            adapter = likedAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
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

    private fun subscribeToObservers() {
        viewModel.mediaItems.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
//                    recentRecyclerViewProgressBar.visibility=View.GONE
                }
                Status.ERROR -> Unit
                Status.LOADING -> {
//                    recentRecyclerViewProgressBar.visibility=View.VISIBLE
                }
            }
        }
    }

    private fun addingSongIntoRecyclerView() {
        viewModel.getLikedList.observe(viewLifecycleOwner) {
            likedAdapter.songs = it
            likedTextTextView.isVisible = !it.isNullOrEmpty()
        }
        viewModel.getRecentList.observe(viewLifecycleOwner) {
            recentAudioAdapter.songs = it
            recentTextTextView.isVisible = !it.isNullOrEmpty()
        }
    }
}