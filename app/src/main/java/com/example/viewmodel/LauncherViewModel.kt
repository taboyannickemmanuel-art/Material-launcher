package com.example.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.AppInfo
import com.example.data.AppRepository
import com.example.data.BatteryInfo
import com.example.data.GridItem
import com.example.data.GridItemType
import com.example.data.LauncherPreferences
import com.example.data.LauncherSettings
import com.example.data.NowPlayingInfo
import com.example.data.WeatherInfo
import com.example.monet.MonetEngine
import com.example.widget.WidgetHostManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    val appRepository = AppRepository(application)
    val preferences = LauncherPreferences(application)
    val widgetHostManager = WidgetHostManager(application)

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _filteredApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val filteredApps: StateFlow<List<AppInfo>> = _filteredApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    private val _desktopItems = MutableStateFlow<List<GridItem>>(emptyList())
    val desktopItems: StateFlow<List<GridItem>> = _desktopItems.asStateFlow()

    private val _dockApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val dockApps: StateFlow<List<AppInfo>> = _dockApps.asStateFlow()

    val settingsState: StateFlow<LauncherSettings> = preferences.settingsState

    private val _weatherInfo = MutableStateFlow(WeatherInfo(22, "Ensoleillé", "Paris", "☀️"))
    val weatherInfo: StateFlow<WeatherInfo> = _weatherInfo.asStateFlow()

    private val _batteryInfo = MutableStateFlow(BatteryInfo(88, false))
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _nowPlayingInfo = MutableStateFlow(NowPlayingInfo("Lofi Tablet Session", "Material Beats", true, 0.40f))
    val nowPlayingInfo: StateFlow<NowPlayingInfo> = _nowPlayingInfo.asStateFlow()

    private val _monetPalette = mutableStateOf(MonetEngine.getDefaultPalette(true))
    val monetPalette: State<MonetEngine.MonetPalette> = _monetPalette

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 85
                _batteryInfo.value = BatteryInfo(pct, isCharging)
            }
        }
    }

    private val _availableIconPacks = MutableStateFlow<List<com.example.data.IconPackInfo>>(emptyList())
    val availableIconPacks: StateFlow<List<com.example.data.IconPackInfo>> = _availableIconPacks.asStateFlow()

    init {
        widgetHostManager.startListening()
        loadMonetPalette()
        loadIconPacks()
        refreshInstalledApps()
        registerBatteryReceiver()
    }

    fun loadIconPacks() {
        viewModelScope.launch {
            _availableIconPacks.value = appRepository.iconPackManager.getInstalledIconPacks()
        }
    }

    private fun registerBatteryReceiver() {
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            getApplication<Application>().registerReceiver(batteryReceiver, filter)
        } catch (e: Exception) {
            // Fallback
        }
    }

    fun loadMonetPalette() {
        viewModelScope.launch {
            val palette = MonetEngine.extractPaletteFromResource(
                getApplication(),
                R.drawable.default_wallpaper_1784729342694,
                settingsState.value.darkTheme
            )
            _monetPalette.value = palette
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            val apps = appRepository.loadInstalledApps(
                hiddenPackages = settingsState.value.hiddenPackages,
                iconPackPackageName = settingsState.value.iconPackName
            )
            _installedApps.value = apps
            filterApps(_searchQuery.value)

            // Auto-populate dock with popular installed apps or top apps
            if (apps.isNotEmpty()) {
                val dockCount = settingsState.value.dockIconCount
                _dockApps.value = apps.take(dockCount)

                // Setup initial desktop shortcuts if empty
                if (_desktopItems.value.isEmpty()) {
                    setupInitialDesktopItems(apps)
                }
            }
        }
    }

    private fun setupInitialDesktopItems(apps: List<AppInfo>) {
        val initialGrid = mutableListOf<GridItem>()
        var x = 0
        var y = 0
        val maxCols = settingsState.value.gridColumns

        // Add 6 desktop app shortcuts across rows
        apps.filter { !it.isHidden }.take(6).forEachIndexed { idx, app ->
            initialGrid.add(
                GridItem(
                    id = "app_$idx",
                    type = GridItemType.APP,
                    packageName = app.packageName,
                    className = app.className,
                    label = app.label,
                    cellX = x,
                    cellY = y,
                    spanX = 1,
                    spanY = 1
                )
            )
            x += 1
            if (x >= maxCols) {
                x = 0
                y += 1
            }
        }

        _desktopItems.value = initialGrid
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        filterApps(query)
    }

    private fun filterApps(query: String) {
        val hidden = settingsState.value.hiddenPackages
        if (query.isBlank()) {
            _filteredApps.value = _installedApps.value.filter { !hidden.contains(it.packageName) }
        } else {
            val trimmed = query.trim().lowercase()
            _filteredApps.value = _installedApps.value.filter { app ->
                !hidden.contains(app.packageName) &&
                    (app.label.lowercase().contains(trimmed) || app.packageName.lowercase().contains(trimmed))
            }
        }
    }

    fun setDrawerOpen(isOpen: Boolean) {
        _isDrawerOpen.value = isOpen
        if (!isOpen) {
            onSearchQueryChanged("")
        }
    }

    fun addAppToDesktop(app: AppInfo) {
        val current = _desktopItems.value.toMutableList()
        val nextId = "app_${System.currentTimeMillis()}"
        val cols = settingsState.value.gridColumns
        val rows = settingsState.value.gridRows

        // Find open cell
        val occupied = current.map { Pair(it.cellX, it.cellY) }.toSet()
        var foundX = 0
        var foundY = 0
        var found = false

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (!occupied.contains(Pair(c, r))) {
                    foundX = c
                    foundY = r
                    found = true
                    break
                }
            }
            if (found) break
        }

        current.add(
            GridItem(
                id = nextId,
                type = GridItemType.APP,
                packageName = app.packageName,
                className = app.className,
                label = app.label,
                cellX = foundX,
                cellY = foundY,
                spanX = 1,
                spanY = 1
            )
        )
        _desktopItems.value = current
    }

    fun removeDesktopItem(item: GridItem) {
        val current = _desktopItems.value.toMutableList()
        current.removeIf { it.id == item.id }
        _desktopItems.value = current
        if (item.type == GridItemType.WIDGET && item.appWidgetId != -1) {
            widgetHostManager.deleteAppWidgetId(item.appWidgetId)
        }
    }

    fun toggleNowPlaying() {
        val current = _nowPlayingInfo.value
        _nowPlayingInfo.value = current.copy(isPlaying = !current.isPlaying)
    }

    fun updateSettings(newSettings: LauncherSettings) {
        preferences.updateSettings(newSettings)
        refreshInstalledApps()
        loadMonetPalette()
    }

    fun toggleAppHidden(packageName: String) {
        preferences.toggleAppHidden(packageName)
        refreshInstalledApps()
    }

    override fun onCleared() {
        super.onCleared()
        widgetHostManager.stopListening()
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Fallback
        }
    }
}
