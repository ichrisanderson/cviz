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

package com.chrisa.cviz.core.data.synchronisation

import android.content.Context
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@InstallIn(ApplicationComponent::class)
@Module
abstract class SyncModule {
    @Binds
    internal abstract fun bindAreaDataSynchroniser(areaDataSynchroniserImpl: AreaDataSynchroniserImpl): AreaDataSynchroniser

    @Binds
    internal abstract fun bindDataSynchroniser(dataSynchroniserImpl: DataSynchroniserImpl): DataSynchroniser

    @Binds
    internal abstract fun bindSyncNotification(syncNotificationImpl: SyncNotificationImpl): SyncNotification

    companion object {
        @Provides
        fun workManager(@ApplicationContext context: Context): WorkManager {
            return WorkManager.getInstance(context)
        }
    }
}
