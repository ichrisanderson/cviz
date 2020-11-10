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

import com.chrisa.cviz.core.data.time.TimeProvider
import java.time.Duration
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncTimeHelper @Inject constructor(
    private val timeProvider: TimeProvider
) {
    fun timeToNextSyncInMillis(): Long {
        val publishTime = timeProvider.currentDate()
            .atStartOfDay(ZoneId.of("GMT"))
            .plusHours(16)
        val currentTime = timeProvider.currentTime().atZone(ZoneId.of("GMT"))
        return if (currentTime < publishTime) {
            Duration.between(currentTime, publishTime).toMillis()
        } else {
            TimeUnit.HOURS.toMillis(24) - Duration.between(publishTime, currentTime).toMillis()
        }
    }
}
