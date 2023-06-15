package tech.apps.music.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tech.apps.music.R
import tech.apps.music.database.Repository
import tech.apps.music.database.network.YoutubeRepository
import tech.apps.music.database.offline.CacheDatabase
import tech.apps.music.database.offline.OfflineDatabase
import tech.apps.music.mediaPlayerYT.MusicServiceConnection
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context
    ) = MusicServiceConnection(context)

    @Singleton
    @Provides
    fun provideMusicDatabase(
        @ApplicationContext context: Context
    ) = Repository(context, provideRoomDatabase(context))

    @Singleton
    @Provides
    fun provideRoomDatabase(
        @ApplicationContext context: Context
    ) = OfflineDatabase.getDatabase(context)

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.color.one_level_up)
            .error(R.color.one_level_up)
            .diskCacheStrategy(DiskCacheStrategy.DATA)

    )

    @Provides
    @Singleton
    fun providerCacheDatabase(application: Application): CacheDatabase =
        Room.databaseBuilder(application, CacheDatabase::class.java, "cache_database")
            .build()

    @Provides
    @Singleton
    fun provideYoutubeRepo(application: Application): YoutubeRepository =
        YoutubeRepository(application)
}
