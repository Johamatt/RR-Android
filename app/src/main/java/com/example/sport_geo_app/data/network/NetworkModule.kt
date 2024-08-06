package com.example.sport_geo_app.data.network

import android.content.Context
import android.content.SharedPreferences
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.network.auth.AuthInterface
import com.example.sport_geo_app.data.network.map.GeoDataInterface
import com.example.sport_geo_app.data.network.utils.AuthInterceptor
import com.example.sport_geo_app.data.network.workouts.WorkoutInterface
import com.example.sport_geo_app.utils.Constants.JWT_TOKEN_KEY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Named("EC2_PUBLIC_IP")
    fun provideEC2PublicIp(@ApplicationContext context: Context): String {
        return context.getString(R.string.EC2_PUBLIC_IP)
    }

    @Provides
    @Named("jwt_retrofit")
    fun provideJwtRetrofit(
        @Named("EC2_PUBLIC_IP") EC2_PUBLIC_IP: String,
        encryptedSharedPreferences: SharedPreferences
    ): Retrofit {
        val token = encryptedSharedPreferences.getString(JWT_TOKEN_KEY, null)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(token))
            .build()

        return Retrofit.Builder()
            .baseUrl(EC2_PUBLIC_IP)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideRetrofit(@Named("EC2_PUBLIC_IP") EC2_PUBLIC_IP: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(EC2_PUBLIC_IP)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthInterface(retrofit: Retrofit): AuthInterface {
        return retrofit.create(AuthInterface::class.java)
    }

    @Provides
    @Singleton
    fun provideGeoDataInterface(@Named("jwt_retrofit") retrofit: Retrofit): GeoDataInterface {
        return retrofit.create(GeoDataInterface::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkoutInterface(@Named("jwt_retrofit") retrofit: Retrofit): WorkoutInterface {
        return retrofit.create(WorkoutInterface::class.java)
    }
}
