package tech.apps.music.util

import tech.apps.music.R
import tech.apps.music.model.ExploreModel

object VideoData {
    fun getThumbnailFromId(videoId: String): String =
        "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

    fun creatingListOfExplores(): ArrayList<ExploreModel> {

        val list: ArrayList<ExploreModel> = ArrayList()

        list.add(
            ExploreModel("Biography","Biography", R.color.marune,
                R.drawable.ic_biography_icon)
        )
        list.add(
            ExploreModel("Podcast","Podcast", R.color.button_color,
                R.drawable.ic_baseline_podcasts_24)
        )
        list.add(
            ExploreModel("AudioBooks","AudioBooks", R.color.dark_green,
                R.drawable.ic_baseline_menu_book_24)
        )
        list.add(
            ExploreModel("Book Summary","Book Summary", R.color.violate,
                R.drawable.ic_baseline_book_24)
        )
        list.add(
            ExploreModel("audio Stories","Stories", R.color.violate,
                R.drawable.ic_baseline_headphones_24)
        )

        list.add(
            ExploreModel("Music","Music", R.color.violate,
                R.drawable.ic_baseline_music_note_24)
        )
        list.add(ExploreModel("Focus Music","Focus Music", R.color.navi_blue, R.drawable.ic_focus_icon))
        list.add(ExploreModel("Workout Music","Workout", R.color.brown, R.drawable.ic_workout_icon))
        list.add(
            ExploreModel("Meditation Music","Meditation", R.color.orange,
                R.drawable.ic_meditation_icon)
        )
        list.add(
            ExploreModel("Sleep Music","Sleep Music", R.color.dark_violate,
                R.drawable.ic_sleep_icon)
        )

        return list
    }
}