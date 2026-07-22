package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.monet.MonetEngine
import com.example.ui.SettingsScreen
import com.example.util.SystemUiUtil
import com.example.viewmodel.LauncherViewModel

class SettingsActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemUiUtil.enableStickyImmersiveMode(this)

        setContent {
            val settings by viewModel.settingsState.collectAsState()
            val installedApps by viewModel.installedApps.collectAsState()
            val availableIconPacks by viewModel.availableIconPacks.collectAsState()
            val palette by viewModel.monetPalette

            val colorScheme = MonetEngine.createColorScheme(palette, settings.darkTheme)

            MaterialTheme(colorScheme = colorScheme) {
                SettingsScreen(
                    settings = settings,
                    installedApps = installedApps,
                    availableIconPacks = availableIconPacks,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onToggleHideApp = { viewModel.toggleAppHidden(it) },
                    onBack = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SystemUiUtil.enableStickyImmersiveMode(this)
    }
}
