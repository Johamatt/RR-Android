package com.example.sport_geo_app.data.network

import android.content.Context
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.network.auth.AuthInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(@ApplicationContext context: Context): Retrofit {
        val baseUrl = context.getString(R.string.EC2_PUBLIC_IP)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthInterface(retrofit: Retrofit): AuthInterface {
        return retrofit.create(AuthInterface::class.java)
    }
}