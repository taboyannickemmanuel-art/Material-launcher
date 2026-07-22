package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.GridItem
import com.example.data.GridItemType
import com.example.monet.MonetEngine
import com.example.ui.AppDrawerSheet
import com.example.ui.HomeScreen
import com.example.util.SystemUiUtil
import com.example.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemUiUtil.enableStickyImmersiveMode(this)

        setContent {
            val settings by viewModel.settingsState.collectAsState()
            val installedApps by viewModel.installedApps.collectAsState()
            val filteredApps by viewModel.filteredApps.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val isDrawerOpen by viewModel.isDrawerOpen.collectAsState()
            val desktopItems by viewModel.desktopItems.collectAsState()
            val dockApps by viewModel.dockApps.collectAsState()
            val weatherInfo by viewModel.weatherInfo.collectAsState()
            val batteryInfo by viewModel.batteryInfo.collectAsState()
            val nowPlayingInfo by viewModel.nowPlayingInfo.collectAsState()
            val palette by viewModel.monetPalette

            val colorScheme = MonetEngine.createColorScheme(palette, settings.darkTheme)

            MaterialTheme(colorScheme = colorScheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        settings = settings,
                        desktopItems = desktopItems,
                        dockApps = dockApps,
                        installedApps = installedApps,
                        weatherInfo = weatherInfo,
                        batteryInfo = batteryInfo,
                        nowPlayingInfo = nowPlayingInfo,
                        widgetHostManager = viewModel.widgetHostManager,
                        onAppClick = { pkg, cls ->
                            viewModel.appRepository.launchApp(pkg, cls)
                        },
                        onAppInfoClick = { pkg ->
                            viewModel.appRepository.openAppDetails(pkg)
                        },
                        onRemoveDesktopItem = { item ->
                            viewModel.removeDesktopItem(item)
                        },
                        onTogglePlayPause = {
                            viewModel.toggleNowPlaying()
                        },
                        onOpenDrawer = {
                            viewModel.setDrawerOpen(true)
                        },
                        onOpenSettings = {
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                        },
                        onAddWidgetClick = {
                            val widgetId = viewModel.widgetHostManager.allocateAppWidgetId()
                            viewModel.addAppToDesktop(
                                com.example.data.AppInfo(
                                    packageName = "com.example.widget",
                                    className = "Widget",
                                    label = "Calendar Widget"
                                )
                            )
                        }
                    )

                    AppDrawerSheet(
                        isOpen = isDrawerOpen,
                        apps = filteredApps,
                        searchQuery = searchQuery,
                        settings = settings,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        onClose = { viewModel.setDrawerOpen(false) },
                        onAppClick = { app ->
                            viewModel.setDrawerOpen(false)
                            viewModel.appRepository.launchApp(app.packageName, app.className)
                        },
                        onAppInfoClick = { app ->
                            viewModel.appRepository.openAppDetails(app.packageName)
                        },
                        onHideAppClick = { app ->
                            viewModel.toggleAppHidden(app.packageName)
                        },
                        onAddToDesktopClick = { app ->
                            viewModel.addAppToDesktop(app)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SystemUiUtil.enableStickyImmersiveMode(this)
        viewModel.refreshInstalledApps()
    }

    override fun onBackPressed() {
        if (viewModel.isDrawerOpen.value) {
            viewModel.setDrawerOpen(false)
        } else {
            // Launcher onBackPressed stays on home screen
        }
    }
}
