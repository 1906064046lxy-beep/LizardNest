package com.lizardnest.app.di

import com.lizardnest.app.data.datasource.EnvironmentDataSource
import com.lizardnest.app.data.datasource.VideoStreamDataSource
import com.lizardnest.app.data.datasource.mock.MockEnvironmentDataSource
import com.lizardnest.app.data.datasource.mock.MockVideoStreamDataSource
import com.lizardnest.app.data.repository.NestRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideEnvironmentDataSource(): EnvironmentDataSource {
        return MockEnvironmentDataSource()
    }

    @Provides
    @Singleton
    fun provideVideoStreamDataSource(): VideoStreamDataSource {
        return MockVideoStreamDataSource()
    }

    @Provides
    @Singleton
    fun provideNestRepository(
        environmentDataSource: EnvironmentDataSource,
        videoStreamDataSource: VideoStreamDataSource
    ): NestRepository {
        return NestRepository(environmentDataSource, videoStreamDataSource)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }
}
