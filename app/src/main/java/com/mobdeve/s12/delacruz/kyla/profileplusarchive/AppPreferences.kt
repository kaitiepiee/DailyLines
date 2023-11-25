package com.mobdeve.s12.delacruz.kyla.profileplusarchive

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

/**
 * This class sets the users preference on the Dark Mode setting.
 */
class AppPreferences(context: AppCompatActivity) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)

    var isDarkModeEnabled: Boolean
        get() = preferences.getBoolean("darkMode", false)
        set(value) = preferences.edit().putBoolean("darkMode", value).apply()

    companion object {
        fun applyDarkModeLogic(activity: AppCompatActivity, layoutIdLight: Int, layoutIdDark: Int) {
            val appPreferences = AppPreferences(activity)

            if (appPreferences.isDarkModeEnabled) {
                activity.setContentView(layoutIdDark)
            } else {
                activity.setContentView(layoutIdLight)
            }
        }
    }
}