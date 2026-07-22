package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    val iconPackManager = IconPackManager(context)

    suspend fun loadInstalledApps(
        hiddenPackages: Set<String> = emptySet(),
        iconPackPackageName: String = "system"
    ): List<AppInfo> = withContext(Dispatchers.IO) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        val appList = mutableListOf<AppInfo>()

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            val cls = info.activityInfo.name
            val label = info.loadLabel(packageManager).toString()

            val defaultIcon = try {
                info.loadIcon(packageManager)
            } catch (e: Exception) {
                null
            }

            val iconDrawable = iconPackManager.loadAppIcon(
                appPackageName = pkg,
                className = cls,
                iconPackPackageName = iconPackPackageName,
                defaultDrawable = defaultIcon
            )

            val isHidden = hiddenPackages.contains(pkg)

            appList.add(
                AppInfo(
                    packageName = pkg,
                    className = cls,
                    label = label,
                    icon = iconDrawable,
                    notificationCount = (1..5).random(), // Simulated notification dots
                    isHidden = isHidden
                )
            )
        }

        appList.sortBy { it.label.lowercase() }
        appList
    }

    fun launchApp(packageName: String, className: String) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setClassName(packageName, className)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = packageManager.getLaunchIntentForPackage(packageName)
            if (fallback != null) {
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallback)
            }
        }
    }

    fun openAppDetails(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback
        }
    }

    fun openUninstall(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                data = Uri.fromParts("package", packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback
        }
    }
}
