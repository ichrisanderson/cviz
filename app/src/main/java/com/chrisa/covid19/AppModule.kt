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

package com.chrisa.covid19

import android.content.Context
import android.content.res.AssetManager
import com.chrisa.covid19.core.data.synchronisation.DataSyncCoroutineScope
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchersImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

@InstallIn(ApplicationComponent::class)
@Module
class AppModule {

    @Singleton
    @Provides
    fun provideCoroutineDispatchers(): CoroutineDispatchers {
        return CoroutineDispatchersImpl()
    }

    @Provides
    fun assetManager(@ApplicationContext context: Context): AssetManager {
        return context.assets
    }

    @DataSyncCoroutineScope
    @Provides
    fun coroutineScope(dispatchers: CoroutineDispatchers): CoroutineScope {
        return CoroutineScope(dispatchers.io + Job())
    }

    @Provides
    fun dateTimeFormatter(): DateTimeFormatter {
        return DateTimeFormatter
            .ofPattern("dd-MMM")
            .withLocale(Locale.UK)
            .withZone(ZoneId.of("GMT"))
    }
}
