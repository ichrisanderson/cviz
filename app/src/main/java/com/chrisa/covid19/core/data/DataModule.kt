/*
 * Copyright 2020 Chris Anderson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chrisa.covid19.core.data

import android.content.Context
import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.MetadataDao
import com.chrisa.covid19.core.data.network.CovidApi
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@InstallIn(ApplicationComponent::class)
@Module
internal object DataModule {

    @Provides
    fun moshi(): Moshi {
        return Moshi.Builder()
            .add(LocalDateJsonAdapter())
            .add(LocalDateTimeJsonAdapter())
            .build()
    }

    @Singleton
    @Provides
    fun okHttpClient(): OkHttpClient {

        val httpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

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
            .baseUrl("https://api.coronavirus.data.gov.uk/")
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

class LocalDateJsonAdapter {
    @ToJson
    fun toJson(localDate: LocalDate): String {
        return localDate.format(FORMATTER)
    }

    @FromJson
    fun fromJson(json: String): LocalDate {
        return FORMATTER.parse(json, LocalDate::from)
    }

    companion object {
        private val FORMATTER = DateTimeFormatter.ISO_DATE
    }
}

class LocalDateTimeJsonAdapter {
    @ToJson
    fun toJson(localDateTime: LocalDateTime): String {
        return localDateTime.atZone(ZoneId.of("UTC"))
            .format(FORMATTER)
    }

    @FromJson
    fun fromJson(json: String): LocalDateTime {
        return FORMATTER.parse(json, LocalDateTime::from)
    }

    companion object {
        private val FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }
}
