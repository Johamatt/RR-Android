package com.example.sport_geo_app.data

import android.content.Context
import com.example.sport_geo_app.utils.ErrorManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideErrorHandler(@ApplicationContext context: Context): ErrorManager {
        return ErrorManager(context)
    }

}
