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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.chrisa.cviz.MainActivity
import com.chrisa.cviz.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface SyncNotification {
    fun showSuccess()
}

class SyncNotificationImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SyncNotification {

    override fun showSuccess() {
        val notificationManager: NotificationManager = notificationManager()
        removeArchivedNotifications(notificationManager)
        createNotificationChannel(
            notificationManager,
            context.getString(R.string.sync_notification_name)
        )
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText(context.getString(R.string.sync_notification_success_content_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Send notification
        notificationManager.notify(0, notification)
    }

    private fun notificationManager(): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun createNotificationChannel(
        notificationManager: NotificationManager,
        name: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun removeArchivedNotifications(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel("cron19.synchronisation")
        }
    }

    companion object {
        private const val CHANNEL_ID = "cviz.synchronisation"
    }
}
