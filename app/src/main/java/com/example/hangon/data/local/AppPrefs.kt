package com.example.hangon.data.local

import android.content.Context
import androidx.core.content.edit
import com.example.hangon.HangOnApplication

object AppPrefs {

    private const val PREFS_NAME = "hangon_prefs"
    private const val KEY_APP_ACTIVATED = "is_app_activated"
    private const val KEY_REQUESTED_PERMISSIONS = "requested_permissions"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isAppActivated: Boolean
        get() = prefs(HangOnApplication.instance).getBoolean(KEY_APP_ACTIVATED, true)
        set(value) = prefs(HangOnApplication.instance).edit { putBoolean(KEY_APP_ACTIVATED, value) }

    fun wasPermissionRequestedBefore(permission: String): Boolean =
        prefs(HangOnApplication.instance).getStringSet(KEY_REQUESTED_PERMISSIONS, emptySet())
            ?.contains(permission) ?: false

    fun markPermissionRequested(permission: String) {
        val current = prefs(HangOnApplication.instance).getStringSet(KEY_REQUESTED_PERMISSIONS, emptySet()).orEmpty()
        prefs(HangOnApplication.instance).edit { putStringSet(KEY_REQUESTED_PERMISSIONS, current + permission) }
    }
}
