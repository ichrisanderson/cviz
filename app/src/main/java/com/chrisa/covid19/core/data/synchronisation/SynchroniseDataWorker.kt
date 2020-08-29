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

package com.chrisa.covid19.core.data.synchronisation

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class SynchroniseDataWorker @WorkerInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val areaListSynchroniser: AreaListSynchroniser,
    private val areaSummaryDataSynchroniser: AreaSummaryDataSynchroniser,
    private val savedAreaDataSynchroniser: SavedAreaDataSynchroniser
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(coroutineDispatchers.io) {
        var hasError = false
        val onError: (error: Throwable) -> Unit = { hasError = true }
        val jobs = listOf(
            async { areaListSynchroniser.performSync(onError) },
            async { areaSummaryDataSynchroniser.performSync(onError) },
            async { savedAreaDataSynchroniser.performSync(onError) }
        )
        // awaitAll will throw an exception if a download fails, which CoroutineWorker will treat as a failure
        jobs.awaitAll()

        return@withContext if (hasError) Result.failure() else Result.success()
    }
}
