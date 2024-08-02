package com.example.sport_geo_app.data.network.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SharedPreferencesModule {

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return EncryptedPreferencesUtil.getEncryptedSharedPreferences(context)
    }
}