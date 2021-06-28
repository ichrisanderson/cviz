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

package com.chrisa.cviz.core.data.preference

object PreferenceValues {
    val refreshDataInBackground =
        BooleanPreference("refresh_data_in_background", true)
    val showNotificationAfterDataRefresh =
        BooleanPreference("show_notification_after_data_refresh", true)
    val darkMode =
        StringListPreference("dark_mode", DarkModeValues.Automatic.value)
}

sealed class DarkModeValues(val value: String) {
    object Off : DarkModeValues("off")
    object On : DarkModeValues("on")
    object Automatic : DarkModeValues("automatic")
}
