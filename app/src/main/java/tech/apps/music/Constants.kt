package tech.apps.music

object Constants {
    const val NOTIFICATION_ID = 12
    const val NOTIFICATION_CHANNEL_ID = "SONG"

    const val ABOUT = "About"
    const val HOW_IT_WORKS = "HowItWorks"
    const val PRIVACY_POLICY = "PrivacyPolicy"
    const val ABOUT_SENDING_DATA = "AboutSendingData"

    const val SAVE_PLAYBACK_SPEED = "savePlaybackSpeed"
    const val SHARED_PREF_PLAYBACK_SPEED = "sharedPreferPlaybackSpeed"

    //bundle KeyWords
    const val PASS_EXPLORE_KEYWORDS = "passExploreKeywords"

    //bundle My Library
    const val PASSING_MY_LIBRARY_TYPE = "passingMyLibrary_type"
    const val MY_LIBRARY_TYPE_BOOKMARK = "Bookmark"

    const val  ACTION_STOP = "${BuildConfig.APPLICATION_ID}.stop"
    const val ACTION_PLAY_PAUSE_TOGGLE = "actionPlayPauseToggle"

    const val SERVICE_TAG = "MusicService"
    const val CHANNEL_NAME  = "Media_Background_Service"

        const val MEDIA_ROOT_ID = "media_root_id"
    const val NETWORK_ERROR = "NETWORK_ERROR"
//    const val UPDATE_PLAYER_POSITION_INTERVAL = 100L

    //    const val LOOP_STATUS = "loop_status"
    //    const val LIVE_VIDEO_DURATION = -1L
    //passing Position
    const val PASSING_SONG_LAST_WATCHED_POS = "passingSongLastWatchedPos"

    //    const val SEARCH_FRAGMENT_VIDEO_ID = "searchFragmentVideoID"

    const val ACTION_TIMER_SONG = "ACTION_TIMER_SONG"
    const val TIMER_IN_LONG = "TIMER_IN_LONG"

//    //last song data
//    const val LAST_SONG_DATA = "lastSongData"
//    const val LAST_SONG_DATA_MEDIA_ID = "lastSongMediaId"
//    const val LAST_SONG_DATA_POSITION = "lastSongPosition"
    //    const val WELCOME_PAGE_SHOWED_STATUS = "welcomePageShowedStatus"
//    const val SHARED_PREF_APP_INTRO = "sharedPreferAppIntro"
}