package com.appstore.appstore

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean
)

class InstalledAppManager(private val context: Context) {

    fun getInstalledApps(): List<InstalledApp> {
        val packageManager = context.packageManager
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != context.packageName }
            .mapNotNull { applicationInfo ->
                val packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, PackageManager.GET_META_DATA)
                val appName = applicationInfo.loadLabel(packageManager).toString()
                val versionName = packageInfo.versionName ?: "Unknown"
                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    packageInfo.versionCode.toLong()
                }
                InstalledApp(
                    packageName = applicationInfo.packageName,
                    appName = appName,
                    versionName = versionName,
                    versionCode = versionCode,
                    isSystemApp = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .sortedBy { it.appName.lowercase() }
    }
}
