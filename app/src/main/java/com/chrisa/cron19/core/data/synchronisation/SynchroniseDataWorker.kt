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

package com.chrisa.cron19.core.data.synchronisation

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chrisa.cron19.core.util.coroutines.CoroutineDispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import timber.log.Timber

class SynchroniseDataWorker @WorkerInject constructor(
    @Assisted context: Context,
    @Assisted private val params: WorkerParameters,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val areaListSynchroniser: AreaListSynchroniser,
    private val areaSummaryDataSynchroniser: AreaSummaryDataSynchroniser,
    private val savedAreaDataSynchroniser: SavedAreaDataSynchroniser,
    private val syncNotification: SyncNotification
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(coroutineDispatchers.io) {
        val jobs = listOf(
            async { syncAreaSummaries() },
            async { syncSavedAreas() },
            async { syncAreaList() }
        )
        // awaitAll will throw an exception if a download fails, which CoroutineWorker will treat as a failure
        val results = jobs.awaitAll()
        if (results.all { true } && showNotification()) {
            syncNotification.showSuccess()
        }
        return@withContext Result.success()
    }

    private fun showNotification(): Boolean {
        return params.inputData.getBoolean(SHOW_NOTIFICATION_KEY, false)
    }

    private suspend fun syncAreaList(): Boolean {
        var result = true
        try {
            areaListSynchroniser.performSync()
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            result = false
        }
        return result
    }

    private suspend fun syncSavedAreas(): Boolean {
        var result = true
        try {
            savedAreaDataSynchroniser.performSync()
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            result = false
        }
        return result
    }

    private suspend fun syncAreaSummaries(): Boolean {
        var result = true
        try {
            areaSummaryDataSynchroniser.performSync()
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            result = false
        }
        return result
    }

    companion object {
        const val SHOW_NOTIFICATION_KEY = "ShowNotification"
    }
}
