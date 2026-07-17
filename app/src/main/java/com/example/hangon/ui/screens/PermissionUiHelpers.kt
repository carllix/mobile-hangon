package com.example.hangon.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.hangon.data.local.AppPrefs
import com.example.hangon.data.model.Permission
import com.example.hangon.data.util.PermissionRequests

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun rememberPermissionRowHandler(): (Permission) -> Unit {
    val context = LocalContext.current
    val activity = context.findActivity()

    val runtimeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    val intentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    return remember(context) {
        { permission: Permission ->
            when {
                permission.isGranted -> context.startActivity(PermissionRequests.appSettingsIntent(context))
                else -> {
                    val runtimePermissions = PermissionRequests.runtimePermissionsFor(permission.id)
                    when {
                        runtimePermissions.isEmpty() ->
                            PermissionRequests.specialPermissionIntentFor(context, permission.id)
                                ?.let { intentLauncher.launch(it) }

                        isPermanentlyDenied(activity, permission.id, runtimePermissions) ->
                            context.startActivity(PermissionRequests.appSettingsIntent(context))

                        else -> {
                            AppPrefs.markPermissionRequested(permission.id)
                            runtimeLauncher.launch(runtimePermissions)
                        }
                    }
                }
            }
        }
    }
}

private fun isPermanentlyDenied(activity: Activity?, permissionId: String, runtimePermissions: Array<String>): Boolean {
    if (activity == null || !AppPrefs.wasPermissionRequestedBefore(permissionId)) return false
    return runtimePermissions.none { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
}

@Composable
fun RefreshPermissionsOnResume(onResume: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnResume = rememberUpdatedState(onResume)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                latestOnResume.value()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
