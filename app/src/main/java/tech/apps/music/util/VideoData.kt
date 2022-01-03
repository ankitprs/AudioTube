package tech.apps.music.util

import tech.apps.music.R
import tech.apps.music.model.ExploreModel

object VideoData {
    fun getThumbnailFromId(videoId: String): String =
        "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

    fun getThumbnailLowQFromId(videoId: String): String =
        "https://i.ytimg.com/vi/$videoId/mqdefault.jpg"

    fun creatingListOfExplores(): ArrayList<ExploreModel> {

        val list: ArrayList<ExploreModel> = ArrayList()

        list.add(
            ExploreModel(
                "Songs", "Songs", "#ff0057", "#ffb600",
                R.drawable.ic_music_icon
            )
        )
        list.add(
            ExploreModel(
                "Biography", "Biography", "#000eff", "#1eb800",
                R.drawable.ic_biography_icon
            )
        )
        list.add(
            ExploreModel(
                "Podcast", "Podcast", "#0001ff", "#ff0000",
                R.drawable.ic_baseline_podcasts_24
            )
        )
        list.add(
            ExploreModel(
                "AudioBooks", "AudioBooks", "#1f0087", "#00f8ff",
                R.drawable.ic_baseline_menu_book_24
            )
        )
        list.add(
            ExploreModel(
                "Book Summary", "Book Summary", "#CD1818", "#F58840",
                R.drawable.ic_baseline_book_24
            )
        )
        list.add(
            ExploreModel(
                "audio Stories", "Stories", "#700B97", "#FFBB86FC",
                R.drawable.ic_baseline_headphones_24
            )
        )

        list.add(
            ExploreModel(
                "Music", "Music", "#1F1D36", "#0099FF",
                R.drawable.ic_baseline_music_note_24
            )
        )
        list.add(
            ExploreModel(
                "Focus Music", "Focus Music", "#CD1818", "#F58840",
                R.drawable.ic_focus_icon
            )
        )
        list.add(
            ExploreModel(
                "Workout Music", "Workout", "#CD1818", "#F58840",
                R.drawable.ic_workout_icon
            )
        )
        list.add(
            ExploreModel(
                "Meditation Music", "Meditation", "#CD1818", "#F58840",
                R.drawable.ic_meditation_icon
            )
        )
        list.add(
            ExploreModel(
                "Sleep Music", "Sleep Music", "#CD1818", "#F58840",
                R.drawable.ic_sleep_icon
            )
        )

        return list
    }
}