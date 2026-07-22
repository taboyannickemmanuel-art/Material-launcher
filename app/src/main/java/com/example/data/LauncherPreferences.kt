package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LauncherSettings(
    // Général
    val darkTheme: Boolean = true,
    val enableNotificationDots: Boolean = true,
    val iconPackName: String = "Material Default",
    val expressiveIconShape: String = "Squircle", // "System", "Circle", "Squircle", "Expressive Star", "Pill", "Teardrop", "Hexagon"
    
    // Écran d'accueil
    val gridRows: Int = 4,
    val gridColumns: Int = 8,
    val iconTextSizeSp: Float = 12f,
    val isDesktopLocked: Boolean = false,

    // At a Glance
    val showAtAGlance: Boolean = true,
    val showWeather: Boolean = true,
    val showDate: Boolean = true,
    val showBattery: Boolean = true,
    val showNowPlaying: Boolean = true,

    // Dock
    val showDock: Boolean = true,
    val showDockSearchBar: Boolean = true,
    val dockIconCount: Int = 6,
    val isDockTranslucent: Boolean = true,

    // Tiroir d'apps
    val drawerColumns: Int = 8,
    val hiddenPackages: Set<String> = emptySet(),
    val drawerBackgroundOpacity: Float = 0.85f,

    // Gestes
    val swipeDownGesture: String = "notifications", // "notifications", "search", "settings", "none"
    val doubleTapGesture: String = "settings",     // "settings", "lock", "none"
    val pinchGesture: String = "settings"          // "settings", "none"
)

class LauncherPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("material_launcher_prefs", Context.MODE_PRIVATE)

    private val _settingsState = MutableStateFlow(loadSettings())
    val settingsState: StateFlow<LauncherSettings> = _settingsState.asStateFlow()

    fun loadSettings(): LauncherSettings {
        return LauncherSettings(
            darkTheme = prefs.getBoolean("dark_theme", true),
            enableNotificationDots = prefs.getBoolean("enable_notification_dots", true),
            iconPackName = prefs.getString("icon_pack_name", "Material Default") ?: "Material Default",
            expressiveIconShape = prefs.getString("expressive_icon_shape", "Squircle") ?: "Squircle",
            
            gridRows = prefs.getInt("grid_rows", 4),
            gridColumns = prefs.getInt("grid_columns", 8),
            iconTextSizeSp = prefs.getFloat("icon_text_size_sp", 12f),
            isDesktopLocked = prefs.getBoolean("is_desktop_locked", false),

            showAtAGlance = prefs.getBoolean("show_at_a_glance", true),
            showWeather = prefs.getBoolean("show_weather", true),
            showDate = prefs.getBoolean("show_date", true),
            showBattery = prefs.getBoolean("show_battery", true),
            showNowPlaying = prefs.getBoolean("show_now_playing", true),

            showDock = prefs.getBoolean("show_dock", true),
            showDockSearchBar = prefs.getBoolean("show_dock_search_bar", true),
            dockIconCount = prefs.getInt("dock_icon_count", 6),
            isDockTranslucent = prefs.getBoolean("is_dock_translucent", true),

            drawerColumns = prefs.getInt("drawer_columns", 8),
            hiddenPackages = prefs.getStringSet("hidden_packages", emptySet()) ?: emptySet(),
            drawerBackgroundOpacity = prefs.getFloat("drawer_bg_opacity", 0.85f),

            swipeDownGesture = prefs.getString("swipe_down_gesture", "notifications") ?: "notifications",
            doubleTapGesture = prefs.getString("double_tap_gesture", "settings") ?: "settings",
            pinchGesture = prefs.getString("pinch_gesture", "settings") ?: "settings"
        )
    }

    fun updateSettings(newSettings: LauncherSettings) {
        prefs.edit().apply {
            putBoolean("dark_theme", newSettings.darkTheme)
            putBoolean("enable_notification_dots", newSettings.enableNotificationDots)
            putString("icon_pack_name", newSettings.iconPackName)
            putString("expressive_icon_shape", newSettings.expressiveIconShape)

            putInt("grid_rows", newSettings.gridRows)
            putInt("grid_columns", newSettings.gridColumns)
            putFloat("icon_text_size_sp", newSettings.iconTextSizeSp)
            putBoolean("is_desktop_locked", newSettings.isDesktopLocked)

            putBoolean("show_at_a_glance", newSettings.showAtAGlance)
            putBoolean("show_weather", newSettings.showWeather)
            putBoolean("show_date", newSettings.showDate)
            putBoolean("show_battery", newSettings.showBattery)
            putBoolean("show_now_playing", newSettings.showNowPlaying)

            putBoolean("show_dock", newSettings.showDock)
            putBoolean("show_dock_search_bar", newSettings.showDockSearchBar)
            putInt("dock_icon_count", newSettings.dockIconCount)
            putBoolean("is_dock_translucent", newSettings.isDockTranslucent)

            putInt("drawer_columns", newSettings.drawerColumns)
            putStringSet("hidden_packages", newSettings.hiddenPackages)
            putFloat("drawer_bg_opacity", newSettings.drawerBackgroundOpacity)

            putString("swipe_down_gesture", newSettings.swipeDownGesture)
            putString("double_tap_gesture", newSettings.doubleTapGesture)
            putString("pinch_gesture", newSettings.pinchGesture)
            apply()
        }
        _settingsState.value = newSettings
    }

    fun toggleAppHidden(packageName: String) {
        val current = _settingsState.value.hiddenPackages.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        val updated = _settingsState.value.copy(hiddenPackages = current)
        updateSettings(updated)
    }
}
