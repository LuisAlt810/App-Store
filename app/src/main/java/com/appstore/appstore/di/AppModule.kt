package com.appstore.appstore.di

import com.appstore.appstore.data.AppSource
import com.appstore.appstore.data.FakeAppSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppSource(): AppSource = FakeAppSource()
}
