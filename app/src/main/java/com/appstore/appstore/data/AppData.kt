package com.appstore.appstore.data

import kotlin.math.max

interface AppSource {
    suspend fun search(query: String, page: Int): List<AppSummary>
    suspend fun details(packageName: String): AppDetails
    suspend fun latestVersion(packageName: String): RemoteVersion?
    suspend fun downloadInfo(packageName: String, versionCode: Long): DownloadInfo
}

data class AppSummary(
    val packageName: String,
    val name: String,
    val developer: String,
    val shortDescription: String,
    val category: String,
    val versionCode: Long,
    val versionName: String,
    val sizeBytes: Long,
    val iconUrl: String,
    val screenshots: List<String> = emptyList(),
    val rating: Float = 4.5f,
    val downloads: String = "1M+"
)

data class AppDetails(
    val packageName: String,
    val name: String,
    val developer: String,
    val shortDescription: String,
    val description: String,
    val versionCode: Long,
    val versionName: String,
    val sizeBytes: Long,
    val iconUrl: String,
    val screenshots: List<String>,
    val permissions: List<String>,
    val changelog: List<String>,
    val category: String,
    val rating: Float,
    val downloads: String
)

data class RemoteVersion(
    val packageName: String,
    val versionCode: Long,
    val versionName: String,
    val sha256: String,
    val downloadUrl: String,
    val sizeBytes: Long
)

data class DownloadInfo(
    val packageName: String,
    val versionCode: Long,
    val url: String,
    val sha256: String,
    val sizeBytes: Long,
    val fileName: String
)

class FakeAppSource : AppSource {

    private val catalog: List<AppSummary> = buildList {
        val names = listOf(
            "Aether Mail", "Alpha Maps", "Aurora Notes", "Beacon Camera", "Blend Studio",
            "Bolt Browser", "Brisk Tracker", "Canvas Paint", "Castle Quest", "Cloud Files",
            "Code Reader", "Color Picker", "Comet TV", "Courier Mail", "Crisp Voice",
            "Dash Wallet", "Deep Focus", "Drift Music", "Echo Recorder", "EverGreen",
            "File Vault", "Flashlight Pro", "Flow Planner", "Forge Tools", "GameHub",
            "Glide Reader", "Glimmer Weather", "Grid Docs", "Heart Monitor", "Hive Drive",
            "Ink Journal", "Kite VPN", "Luma Photos", "MapPilot", "Mellow Music",
            "Momentum Workout", "Moon Reader", "Navio GPS", "Nova Launcher", "Orbit Finance",
            "Pace Runner", "Paper Notes", "Pixel Editor", "Plexus Chat", "Pocket Tasks",
            "Pulse Radio", "Quick Sync", "Radius Browser", "Rift Games", "Signal Labs"
        )

        val developers = listOf(
            "Northwind Labs", "Kite Studio", "Signal Forge", "Maple Works", "Sunset Soft",
            "Pine Valley", "BlueQuartz", "Lavender Studio", "Oakline", "NovaPeak",
            "Lattice Apps", "Cloud Atlas", "Echo Systems", "Orbit Media", "Helio Labs"
        )

        val categories = listOf(
            "Productivity", "Communication", "Media", "Tools", "Games", "Finance",
            "Health", "Travel", "Photos", "Education"
        )

        names.forEachIndexed { index, name ->
            val packageName = "com.appstore.${name.lowercase().replace(" ", "").replace("-", "")}"
            val image = "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=80"
            val prop = index % 10
            add(
                AppSummary(
                    packageName = packageName,
                    name = name,
                    developer = developers[index % developers.size],
                    shortDescription = "A polished app for ${categories[prop % categories.size].lowercase()} workflows.",
                    category = categories[prop % categories.size],
                    versionCode = 1200L + index,
                    versionName = "${(index % 8) + 2}.${(index % 9) + 1}.${(index % 6) + 1}",
                    sizeBytes = (12L + (index * 1_500_000L)) * 1024L,
                    iconUrl = image,
                    screenshots = List(3) { image },
                    rating = 4.1f + (index % 5) * 0.2f,
                    downloads = "${(200_000 + index * 17_500).toString().replace("\\B(?=(\\d{3})+(?!\\d))", ",").toRegex()}+"
                )
            )
        }
    }

    override suspend fun search(query: String, page: Int): List<AppSummary> {
        val trimmed = query.trim()
        val offset = (page - 1).coerceAtLeast(0) * 20
        val filtered = if (trimmed.isEmpty()) {
            catalog
        } else {
            catalog.filter { app ->
                app.name.contains(trimmed, ignoreCase = true) ||
                    app.packageName.contains(trimmed, ignoreCase = true) ||
                    app.category.contains(trimmed, ignoreCase = true)
            }
        }
        return filtered.drop(offset).take(20)
    }

    override suspend fun details(packageName: String): AppDetails {
        val app = catalog.first { it.packageName == packageName }
        return AppDetails(
            packageName = app.packageName,
            name = app.name,
            developer = app.developer,
            shortDescription = app.shortDescription,
            description = "${app.name} delivers a focused experience for modern users. It offers reliable offline support, clean performance, and smart automation designed to fit both work and personal routines.",
            versionCode = app.versionCode,
            versionName = app.versionName,
            sizeBytes = app.sizeBytes,
            iconUrl = app.iconUrl,
            screenshots = app.screenshots,
            permissions = listOf(
                "Storage",
                "Internet access",
                "Notifications",
                "Camera",
                "Background sync"
            ),
            changelog = listOf(
                "Improved startup time and app responsiveness.",
                "Fixed edge-case sync issues on Android 14+.",
                "Added better dark mode tuning and accessibility support."
            ),
            category = app.category,
            rating = app.rating,
            downloads = app.downloads
        )
    }

    override suspend fun latestVersion(packageName: String): RemoteVersion? {
        return catalog.firstOrNull { it.packageName == packageName }?.let { app ->
            RemoteVersion(
                packageName = app.packageName,
                versionCode = app.versionCode + 1,
                versionName = app.versionName,
                sha256 = "${app.packageName.reversed()}${app.versionCode}sha256placeholder",
                downloadUrl = "https://example.com/download/${app.packageName}.apk",
                sizeBytes = max(1L, app.sizeBytes)
            )
        }
    }

    override suspend fun downloadInfo(packageName: String, versionCode: Long): DownloadInfo {
        val app = catalog.first { it.packageName == packageName }
        return DownloadInfo(
            packageName = app.packageName,
            versionCode = versionCode,
            url = "https://example.com/download/${app.packageName}_${versionCode}.apk",
            sha256 = "${app.packageName.reversed()}${versionCode}sha256placeholder",
            sizeBytes = app.sizeBytes,
            fileName = "${app.name.lowercase().replace(" ", "_")}_${versionCode}.apk"
        )
    }
}
