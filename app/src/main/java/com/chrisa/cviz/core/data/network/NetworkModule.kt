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

package com.chrisa.cviz.core.data.network

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@InstallIn(ApplicationComponent::class)
@Module
internal object NetworkModule {

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
            .addInterceptor(CommonHeaderInterceptor())
            .addInterceptor(UrlDecodeInterceptor())
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
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
    }

    @Provides
    fun covidApi(retrofit: Retrofit): CovidApi {
        return retrofit.create(CovidApi::class.java)
    }
}
