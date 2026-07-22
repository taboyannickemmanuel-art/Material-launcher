package com.example.data

import android.graphics.drawable.Drawable

/**
 * Data class representing an installed Android application.
 */
data class AppInfo(
    val packageName: String,
    val className: String,
    val label: String,
    val icon: Drawable? = null,
    val notificationCount: Int = 0,
    val isHidden: Boolean = false
)

/**
 * Types of items that can be placed on the Home Screen grid.
 */
enum class GridItemType {
    APP,
    WIDGET,
    SHORTCUT
}

/**
 * Item placed on the desktop grid.
 */
data class GridItem(
    val id: String,
    val type: GridItemType,
    val packageName: String = "",
    val className: String = "",
    val label: String = "",
    val cellX: Int = 0,
    val cellY: Int = 0,
    val spanX: Int = 1,
    val spanY: Int = 1,
    val appWidgetId: Int = -1
)

/**
 * Weather info state for At a Glance widget.
 */
data class WeatherInfo(
    val temperatureCelsius: Int = 22,
    val condition: String = "Ensoleillé",
    val location: String = "Paris",
    val iconSymbol: String = "☀️"
)

/**
 * Battery info state.
 */
data class BatteryInfo(
    val percentage: Int = 85,
    val isCharging: Boolean = false
)

/**
 * Now Playing media state.
 */
data class NowPlayingInfo(
    val title: String = "Lofi Study Beats",
    val artist: String = "Chillhop Music",
    val isPlaying: Boolean = true,
    val progressFraction: Float = 0.45f
)
