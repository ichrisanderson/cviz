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

package com.chrisa.cron19.core.util

import androidx.room.withTransaction
import com.chrisa.cron19.core.data.db.AppDatabase
import io.mockk.coEvery
import io.mockk.mockkStatic
import io.mockk.slot

fun AppDatabase.mockTransaction() {
    mockkStatic("androidx.room.RoomDatabaseKt")
    val transactionLambda = slot<suspend () -> Unit>()
    coEvery { withTransaction(capture(transactionLambda)) } coAnswers {
        transactionLambda.captured.invoke()
    }
}
