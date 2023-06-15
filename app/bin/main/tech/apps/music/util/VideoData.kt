package tech.apps.music.util

import tech.apps.music.R
import tech.apps.music.model.ExploreModel


fun getThumbnailFromId(videoId: String): String =
    "https://i.ytimg.com/vi/$videoId/mqdefault.jpg"


fun creatingListOfExplores(): ArrayList<ExploreModel> {

    val list: ArrayList<ExploreModel> = ArrayList()

    list.add(
        ExploreModel(
            "Songs", "Songs", "#ff0000", "#ff6500",
            R.drawable.ic_music_icon
        )
    )
    list.add(
        ExploreModel(
            "Biography", "Biography", "#0800ff", "#00dbff",
            R.drawable.ic_biography_icon
        )
    )
    list.add(
        ExploreModel(
            "Podcast", "Podcast", "#700B97", "#FFBB86FC",
            R.drawable.ic_baseline_podcasts_24
        )
    )
    list.add(
        ExploreModel(
            "AudioBooks", "AudioBooks", "#006502", "#0cff00",
            R.drawable.ic_baseline_menu_book_24
        )
    )
    list.add(
        ExploreModel(
            "Book Summary", "Book Summary", "#FF0000", "#F58840",
            R.drawable.ic_baseline_book_24
        )
    )
    list.add(
        ExploreModel(
            "audio Stories", "Stories", "#b3541e", "#ffe200",
            R.drawable.ic_baseline_headphones_24
        )
    )

    list.add(
        ExploreModel(
            "Music", "Music", "#e000ff", "#ff0060",
            R.drawable.ic_baseline_music_note_24
        )
    )
    list.add(
        ExploreModel(
            "Focus Music", "Focus Music", "#1F1D36", "#2E4C6D",
            R.drawable.ic_focus_icon
        )
    )
    list.add(
        ExploreModel(
            "Workout Music", "Workout", "#006502", "#0cff00",
            R.drawable.ic_workout_icon
        )
    )
    list.add(
        ExploreModel(
            "Meditation Music", "Meditation", "#781d42", "#ff0075",
            R.drawable.ic_meditation_icon
        )
    )
    return list
}
