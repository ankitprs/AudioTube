package tech.apps.music.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

//    @ServiceScoped
//    @Provides
//    fun provideMusicDatabase() = MusicDatabase()

}
