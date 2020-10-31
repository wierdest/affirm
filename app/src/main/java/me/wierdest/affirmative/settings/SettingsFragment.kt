package me.wierdest.affirmative.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import me.wierdest.affirmative.R

class SettingsFragment : PreferenceFragmentCompat() {
    val TAG = javaClass.name
    val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences, s: String ->


        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

    }

}