package com.example.sport_geo_app

import com.example.sport_geo_app.ui.viewmodel.AuthViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Singleton
    fun provideMockAuthViewModel(): AuthViewModel = mockk(relaxed = true)
}
