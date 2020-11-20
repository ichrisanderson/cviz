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

package com.chrisa.cviz.features.home.presentation.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import com.chrisa.cviz.R
import com.chrisa.cviz.core.data.preference.DarkModeValues
import com.chrisa.cviz.core.data.preference.PreferenceValues
import com.chrisa.cviz.core.data.synchronisation.SynchroniseDataWorkManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var synchroniseDataWorkManager: SynchroniseDataWorkManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(
            this
        )
    }

    override fun onPause() {
        preferenceScreen.preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(
            this
        )
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferenceValues.refreshDataInBackground.key -> {
                synchroniseDataWorkManager.toggleRefresh()
            }
            PreferenceValues.darkMode.key -> {
                toggleDarkMode()
            }
        }
    }

    private fun toggleDarkMode() {
        when (preferenceManager.sharedPreferences.getString(PreferenceValues.darkMode.key, "")) {
            DarkModeValues.Automatic.value -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            DarkModeValues.Off.value -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            DarkModeValues.On.value -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }
}
