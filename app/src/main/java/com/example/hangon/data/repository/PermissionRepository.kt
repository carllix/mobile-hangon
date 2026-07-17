package com.example.hangon.data.repository

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.hangon.HangOnApplication
import com.example.hangon.data.model.Permission

interface PermissionRepository {
    fun getHomePermissions(): List<Permission>
    fun getOnboardingPermissions(): List<Permission>
    fun isCallScreeningGranted(): Boolean
}

class AndroidPermissionRepository(
    private val context: Context = HangOnApplication.instance
) : PermissionRepository {

    private fun isGranted(id: String): Boolean = when (id) {
        "call_screening" -> isCallScreeningRoleHeld()
        "audio" -> hasRuntimePermission(Manifest.permission.RECORD_AUDIO)
        "overlay" -> Settings.canDrawOverlays(context)
        "contacts" -> hasRuntimePermission(Manifest.permission.READ_CONTACTS)
        else -> false
    }

    private fun hasRuntimePermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun isCallScreeningRoleHeld(): Boolean {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            ?: return false
        if (!roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) return false
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    override fun isCallScreeningGranted(): Boolean = isCallScreeningRoleHeld()

    override fun getHomePermissions(): List<Permission> = listOf(
        Permission(
            id = "audio",
            title = "Call Audio Access",
            description = "Required to record audio during calls.",
            isRequired = true,
            isGranted = isGranted("audio")
        ),
        Permission(
            id = "overlay",
            title = "Display Over Other Apps",
            description = "Required to show warning overlays.",
            isRequired = true,
            isGranted = isGranted("overlay")
        ),
        Permission(
            id = "contacts",
            title = "Contact Access",
            description = "Matches incoming numbers against your contacts.",
            isRequired = false,
            isGranted = isGranted("contacts")
        )
    )

    override fun getOnboardingPermissions(): List<Permission> = listOf(
        Permission(
            id = "call_screening",
            title = "Call Screening",
            description = "Required to detect incoming calls and unrecognized numbers.",
            isRequired = true,
            isGranted = isGranted("call_screening")
        ),
        Permission(
            id = "audio",
            title = "Call Audio Access",
            description = "Required to listen to and analyze audio during a call.",
            isRequired = true,
            isGranted = isGranted("audio")
        ),
        Permission(
            id = "overlay",
            title = "Display Over Other Apps",
            description = "Required to show warning overlays while a call is active.",
            isRequired = true,
            isGranted = isGranted("overlay")
        ),
        Permission(
            id = "contacts",
            title = "Contact Access",
            description = "Optional. Used to match incoming numbers against your contact list.",
            isRequired = false,
            isGranted = isGranted("contacts")
        )
    )
}
