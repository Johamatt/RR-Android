package com.example.sport_geo_app.modules

import com.example.sport_geo_app.di.ToastModule
import com.example.sport_geo_app.di.Toaster
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ToastModule::class],
)
object OverrideToastModule {
    @Singleton
    @Provides
    fun providesToaster(): Toaster = FakeToaster
}

object FakeToaster : Toaster {
    val toasts = mutableListOf<String> ()
    override fun showToast(text: String) {
        toasts.add(text)
    }
}