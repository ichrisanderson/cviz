package com.chrisa.covid19.core.data

import android.content.Context
import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.CaseDao
import com.chrisa.covid19.core.data.db.DailyRecordDao
import com.chrisa.covid19.core.data.db.DeathDao
import com.chrisa.covid19.core.data.db.MetadataDao
import com.chrisa.covid19.core.data.network.CovidApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
internal object DataModule {

    @Provides
    fun moshi(): Moshi {
        return Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .build()
    }

    @Singleton
    @Provides
    fun okHttpClient(): OkHttpClient {

        val httpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.HEADERS)

        val builder = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)

        return builder.build()
    }

    @Provides
    fun retrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://coronavirus.data.gov.uk/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    fun covidApi(retrofit: Retrofit): CovidApi {
        return retrofit.create(CovidApi::class.java)
    }
}

@InstallIn(ApplicationComponent::class)
@Module
internal object DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.buildDatabase(
            context
        )
    }

    @Provides
    fun provideCasesDao(appDatabase: AppDatabase): CaseDao {
        return appDatabase.casesDao()
    }

    @Provides
    fun provideDailyRecordsDao(appDatabase: AppDatabase): DailyRecordDao {
        return appDatabase.dailyRecordsDao()
    }

    @Provides
    fun provideDeathsDao(appDatabase: AppDatabase): DeathDao {
        return appDatabase.deathsDao()
    }

    @Provides
    fun provideMetadataDao(appDatabase: AppDatabase): MetadataDao {
        return appDatabase.metadataDao()
    }
}

@InstallIn(ApplicationComponent::class)
@Module
abstract class BootstrapperModule {
    @Binds
    internal abstract fun bindAssetBootstrapper(assetBootstrapper: AssetBootstrapper): Bootstrapper
}


