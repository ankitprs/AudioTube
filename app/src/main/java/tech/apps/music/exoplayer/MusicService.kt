package tech.apps.music.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tech.apps.music.exoplayer.callbacks.MusicPlaybackPreparer
import tech.apps.music.exoplayer.callbacks.MusicPlayerEventListener
import tech.apps.music.exoplayer.callbacks.MusicPlayerNotificationListener
import tech.apps.music.others.Constants.MEDIA_ROOT_ID
import javax.inject.Inject

private const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService: MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFormat: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var ytVideoMusicSource: YTVideoMusicSource

    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    private lateinit var musicNotificationManager: MusicNotificationManager

    private val serviceJob= Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main+serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService=false

    private var curPlayingSong: MediaMetadataCompat?=null

//    private var isInitialized=false

    companion object{
        var curSongDuration=0L
            private set

    }

    override fun onCreate() {
        super.onCreate()

        val activityIntent=packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this,0,it,0)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply{
            setSessionActivity(activityIntent)
            isActive=true
        }
        serviceScope.launch{
            ytVideoMusicSource.fetchSong()
        }


        sessionToken = mediaSession.sessionToken

        musicNotificationManager= MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ){
            curSongDuration = exoPlayer.duration
        }
        val musicPlaybackPreparer=MusicPlaybackPreparer(ytVideoMusicSource){

            curPlayingSong = it
            preparePlayer(
                ytVideoMusicSource.songs,
                it,
                true
            )
        }

        mediaSessionConnector=MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerEventListener = MusicPlayerEventListener(this)

//        exoPlayer.repeatMode=Player.REPEAT_MODE_ALL

        exoPlayer.addListener(musicPlayerEventListener)

        musicNotificationManager.showNotification(exoPlayer)
    }

    private inner class MusicQueueNavigator: TimelineQueueNavigator(mediaSession){
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return ytVideoMusicSource.songs[windowIndex].description
        }
    }

    private fun preparePlayer(
        songs:List<MediaMetadataCompat>,
        itemToPlay:MediaMetadataCompat?,
        playNow: Boolean
    ){
        val curSongIndex= if(curPlayingSong==null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.prepare(ytVideoMusicSource.asMediaSource(dataSourceFormat))
        exoPlayer.seekTo(curSongIndex,0L)
        exoPlayer.playWhenReady=playNow
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID,null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId){
            MEDIA_ROOT_ID->{
//                val resultsSent=ytVideoMusicSource.whenReady {
//                    if(it){
//                        result.sendResult(ytVideoMusicSource.asMediaItems().toMutableList())
////                        if(!isInitialized && ytVideoMusicSource.songs.isNotEmpty()){
////                            preparePlayer(ytVideoMusicSource.songs,ytVideoMusicSource.songs[0],false)
////                            isInitialized=true
////                        }
//                    }else{
//                        mediaSession.sendSessionEvent(NETWORK_ERROR,null)
//                        result.sendResult(null)
//                    }
//                }
//                if(!resultsSent){
//                    result.detach()
//                }
                result.detach()
            }
        }
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()

        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }
}