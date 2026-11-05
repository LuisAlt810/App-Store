package com.appstore.appstore.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallDesktop
import androidx.compose.material.icons.filled.MobileFriendly
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.appstore.appstore.InstalledAppManager
import com.appstore.appstore.data.AppSummary
import com.appstore.appstore.data.FakeAppSource
import com.appstore.appstore.download.DownloadForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppStoreApp() {
    val source = remember { FakeAppSource() }
    var searchText by remember { mutableStateOf("") }
    var featuredApps by remember { mutableStateOf<List<AppSummary>>(emptyList()) }
    var filteredApps by remember { mutableStateOf<List<AppSummary>>(emptyList()) }
    val context = LocalContext.current
    val installedApps = remember(context) { InstalledAppManager(context).getInstalledApps() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            featuredApps = source.search("", 1).take(8)
            filteredApps = featuredApps
        }
    }

    LaunchedEffect(searchText) {
        if (searchText.isBlank()) {
            filteredApps = featuredApps
        } else {
            filteredApps = source.search(searchText, 1)
        }
    }

    val updates = remember(featuredApps, installedApps) {
        val available = featuredApps.associateBy { it.packageName }
        installedApps.mapNotNull { installed ->
            val candidate = available[installed.packageName] ?: return@mapNotNull null
            if (candidate.versionCode > installed.versionCode) {
                candidate to installed
            } else {
                null
            }
        }
    }

    val tabs = listOf("Home", "Search", "Updates", "Installed", "Downloads", "Settings")
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, label ->
                    val icon = when (label) {
                        "Home" -> Icons.Filled.Home
                        "Search" -> Icons.Filled.Search
                        "Updates" -> Icons.Filled.Update
                        "Installed" -> Icons.Filled.MobileFriendly
                        "Downloads" -> Icons.Filled.Download
                        else -> Icons.Filled.Settings
                    }
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                0 -> BrowseScreen(context = context, featuredApps = featuredApps, updates = updates)
                1 -> SearchScreen(context = context, searchText = searchText, onSearchChanged = { searchText = it }, apps = filteredApps)
                2 -> UpdatesScreen(updates = updates)
                3 -> InstalledScreen(installedApps = installedApps)
                4 -> DownloadsScreen()
                else -> SettingsScreen(context = context)
            }
        }
    }
}

private fun startDownloadService(context: Context, appName: String, packageName: String) {
    val intent = Intent(context, DownloadForegroundService::class.java).apply {
        putExtra("app_name", appName)
        putExtra("package_name", packageName)
    }
    context.startForegroundService(intent)
}

@Composable
private fun BrowseScreen(
    context: Context,
    featuredApps: List<AppSummary>,
    updates: List<Pair<AppSummary, com.appstore.appstore.InstalledApp>>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "App-Store",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(featuredApps.take(5)) { app ->
                    AppFeatureCard(app)
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Updates ready", style = MaterialTheme.typography.titleMedium)
                Text(text = "${updates.size} apps", color = MaterialTheme.colorScheme.primary)
            }
        }
        items(featuredApps.take(10)) { app ->
            AppListRow(
                app = app,
                onDownload = {
                    startDownloadService(context, app.name, app.packageName)
                }
            )
        }
    }
}

@Composable
private fun SearchScreen(
    context: Context,
    searchText: String,
    onSearchChanged: (String) -> Unit,
    apps: List<AppSummary>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextField(
            value = searchText,
            onValueChange = onSearchChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search apps") }
        )

        if (apps.isEmpty()) {
            Text("No results found.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(apps) { app ->
                    AppListRow(app = app, onDownload = {
                        startDownloadService(context, app.name, app.packageName)
                    })
                }
            }
        }
    }
}

@Composable
private fun UpdatesScreen(updates: List<Pair<AppSummary, com.appstore.appstore.InstalledApp>>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Updates", style = MaterialTheme.typography.headlineSmall)
        if (updates.isEmpty()) {
            Text("You are up to date. No app updates available.")
        } else {
            updates.forEach { (newVersion, installed) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(newVersion.name, fontWeight = FontWeight.SemiBold)
                            Text("${installed.versionName} → ${newVersion.versionName}")
                            Text("${installed.appName} • ${newVersion.category}")
                        }
                        Button(onClick = { }) {
                            Text("Update")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstalledScreen(installedApps: List<com.appstore.appstore.InstalledApp>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Installed", style = MaterialTheme.typography.headlineSmall) }
        items(installedApps) { app ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(app.appName.take(1))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.appName, fontWeight = FontWeight.SemiBold)
                        Text("Version ${app.versionName} • ${if (app.isSystemApp) "System" else "User"}")
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadsScreen() {
    val downloads = listOf(
        "Nova Launcher" to 84,
        "Aether Mail" to 41,
        "Pulse Radio" to 16
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Downloads", style = MaterialTheme.typography.headlineSmall) }
        items(downloads) { (name, progress) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name)
                        Text("${progress}%")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress / 100f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(context: android.content.Context) {
    val batteryIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Source", style = MaterialTheme.typography.titleMedium)
                Text("Fake local catalog (ready for API backend)")
                Divider()
                Text("Theme", style = MaterialTheme.typography.titleMedium)
                Text("System / Dynamic color enabled")
                Divider()
                Text("Background behavior", style = MaterialTheme.typography.titleMedium)
                Text("Always run in background services is enabled for downloads and update checks.")
                Button(onClick = { context.startActivity(batteryIntent) }) {
                    Text("Stop optimizing battery")
                }
            }
        }
    }
}

@Composable
private fun AppFeatureCard(app: AppSummary) {
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Image(
                painter = rememberAsyncImagePainter(model = app.iconUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )
            Text(app.name, style = MaterialTheme.typography.titleMedium)
            Text(app.shortDescription, style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(app.category)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun AppListRow(app: AppSummary, onDownload: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = app.iconUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(app.name, fontWeight = FontWeight.SemiBold)
                Text(app.developer, style = MaterialTheme.typography.bodySmall)
                Text("${app.versionName} • ${app.category}", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = onDownload) {
                Text("Install")
            }
        }
    }
}
