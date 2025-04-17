package com.app.bestbrain.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

object PermissionHelper {

    fun registerCameraPermissionLauncher(
        lifecycleOwner: LifecycleOwner,
        registryOwner: ActivityResultRegistryOwner,
        onResult: (Boolean) -> Unit
    ): ActivityResultLauncher<String> {
        val key = "camera_permission_${System.currentTimeMillis()}"
        val launcher = registryOwner.activityResultRegistry.register(
            key,
            lifecycleOwner,
            ActivityResultContracts.RequestPermission(),
            onResult
        )

        // Optional: Unregister on destroy
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                launcher.unregister()
            }
        })

        return launcher
    }

    fun isCameraPermissionGranted(owner: LifecycleOwner): Boolean {
        val context = (owner as? androidx.fragment.app.Fragment)?.requireContext()
            ?: (owner as? android.app.Activity)
            ?: return false

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}