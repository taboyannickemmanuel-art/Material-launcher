package com.example.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import com.example.R
import com.example.data.AppInfo
import com.example.data.BatteryInfo
import com.example.data.GridItem
import com.example.data.GridItemType
import com.example.data.LauncherSettings
import com.example.data.NowPlayingInfo
import com.example.data.WeatherInfo
import com.example.util.SystemUiUtil
import com.example.widget.WidgetHostManager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    settings: LauncherSettings,
    desktopItems: List<GridItem>,
    dockApps: List<AppInfo>,
    installedApps: List<AppInfo>,
    weatherInfo: WeatherInfo,
    batteryInfo: BatteryInfo,
    nowPlayingInfo: NowPlayingInfo,
    widgetHostManager: WidgetHostManager,
    onAppClick: (String, String) -> Unit,
    onAppInfoClick: (String) -> Unit,
    onRemoveDesktopItem: (GridItem) -> Unit,
    onTogglePlayPause: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddWidgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDesktopMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Zero corner masking - rectangular edge-to-edge desktop
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -30) {
                        // Swipe up -> Open Drawer
                        onOpenDrawer()
                    } else if (dragAmount > 30) {
                        // Swipe down -> Open Notifications
                        SystemUiUtil.expandNotificationsShade(context)
                    }
                }
            }
            .combinedClickable(
                onClick = {},
                onDoubleClick = { onOpenSettings() },
                onLongClick = {
                    SystemUiUtil.triggerHapticFeedback(context, 30)
                    showDesktopMenu = true
                }
            )
            .testTag("home_screen_root")
    ) {
        // Desktop Wallpaper Image
        Image(
            painter = painterResource(id = R.drawable.default_wallpaper_1784729342694),
            contentDescription = "Wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Semi-translucent overlay for Monet atmosphere contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surface.copy(
                        alpha = if (settings.darkTheme) 0.30f else 0.15f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: At a Glance Widget
            AtAGlanceWidget(
                settings = settings,
                weatherInfo = weatherInfo,
                batteryInfo = batteryInfo,
                nowPlayingInfo = nowPlayingInfo,
                onTogglePlayPause = onTogglePlayPause
            )

            // Middle Section: Customizable Desktop Grid Layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(settings.gridColumns),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(desktopItems, key = { it.id }) { gridItem ->
                        DesktopGridCell(
                            gridItem = gridItem,
                            settings = settings,
                            installedApps = installedApps,
                            widgetHostManager = widgetHostManager,
                            onAppClick = onAppClick,
                            onAppInfo = onAppInfoClick,
                            onRemove = { onRemoveDesktopItem(gridItem) }
                        )
                    }
                }
            }

            // Bottom Section: Dock with M3 Search Bar & App Shortcuts
            if (settings.showDock) {
                DockBar(
                    settings = settings,
                    dockApps = dockApps,
                    onOpenDrawer = onOpenDrawer,
                    onAppClick = onAppClick
                )
            }
        }

        // Contextual Desktop Popup Menu
        DropdownMenu(
            expanded = showDesktopMenu,
            onDismissRequest = { showDesktopMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Ajouter un widget") },
                leadingIcon = { Icon(Icons.Default.Widgets, contentDescription = "Add Widget") },
                onClick = {
                    showDesktopMenu = false
                    onAddWidgetClick()
                }
            )
            DropdownMenuItem(
                text = { Text("Modifier le fond d'écran") },
                leadingIcon = { Icon(Icons.Default.Wallpaper, contentDescription = "Wallpaper") },
                onClick = {
                    showDesktopMenu = false
                    try {
                        val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                        context.startActivity(Intent.createChooser(intent, "Choisir un fond d'écran"))
                    } catch (e: Exception) {
                        // Fallback
                    }
                }
            )
            DropdownMenuItem(
                text = { Text("Paramètres du Launcher") },
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                onClick = {
                    showDesktopMenu = false
                    onOpenSettings()
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopGridCell(
    gridItem: GridItem,
    settings: LauncherSettings,
    installedApps: List<AppInfo>,
    widgetHostManager: WidgetHostManager,
    onAppClick: (String, String) -> Unit,
    onAppInfo: (String) -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    // Lawnchair Physics Spring Animation on touch press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SpringScale"
    )

    when (gridItem.type) {
        GridItemType.APP -> {
            val appInfo = installedApps.find { it.packageName == gridItem.packageName }

            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .clip(RoundedCornerShape(16.dp))
                        .combinedClickable(
                            onClick = {
                                SystemUiUtil.triggerHapticFeedback(context, 15)
                                onAppClick(gridItem.packageName, gridItem.className)
                            },
                            onLongClick = {
                                SystemUiUtil.triggerHapticFeedback(context, 35)
                                showMenu = true
                            }
                        )
                        .padding(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.TopEnd,
                        modifier = Modifier.size(54.dp)
                    ) {
                        val iconShape = remember(settings.expressiveIconShape) {
                            ExpressiveIconShapes.getShape(settings.expressiveIconShape)
                        }
                        val iconBitmap = remember(appInfo?.icon) {
                            appInfo?.icon?.toBitmap()?.asImageBitmap()
                        }

                        if (iconBitmap != null) {
                            Image(
                                bitmap = iconBitmap,
                                contentDescription = gridItem.label,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(iconShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(iconShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = gridItem.label.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Notification Dot
                        if (settings.enableNotificationDots && (appInfo?.notificationCount ?: 0) > 0) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${appInfo?.notificationCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onError,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = gridItem.label,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = settings.iconTextSizeSp.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Info Application") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = "Info") },
                        onClick = {
                            showMenu = false
                            onAppInfo(gridItem.packageName)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Supprimer du bureau") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete") },
                        onClick = {
                            showMenu = false
                            onRemove()
                        }
                    )
                }
            }
        }

        GridItemType.WIDGET -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            SystemUiUtil.triggerHapticFeedback(context, 35)
                            showMenu = true
                        }
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (gridItem.appWidgetId != -1) {
                        val hostView = remember(gridItem.appWidgetId) {
                            widgetHostManager.createWidgetView(context, gridItem.appWidgetId)
                        }
                        if (hostView != null) {
                            AndroidView(
                                factory = { hostView },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            WidgetPlaceholder(gridItem)
                        }
                    } else {
                        WidgetPlaceholder(gridItem)
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Supprimer le widget") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete") },
                            onClick = {
                                showMenu = false
                                onRemove()
                            }
                        )
                    }
                }
            }
        }

        else -> {}
    }
}

@Composable
fun WidgetPlaceholder(gridItem: GridItem) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Widgets,
                contentDescription = "Widget",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = gridItem.label.ifEmpty { "Widget App" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DockBar(
    settings: LauncherSettings,
    dockApps: List<AppInfo>,
    onOpenDrawer: () -> Unit,
    onAppClick: (String, String) -> Unit
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .testTag("dock_bar_surface"),
        shape = RoundedCornerShape(32.dp),
        color = if (settings.isDockTranslucent) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.80f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Material 3 Search Bar in Dock
            if (settings.showDockSearchBar) {
                Card(
                    modifier = Modifier
                        .weight(0.35f)
                        .clip(RoundedCornerShape(24.dp))
                        .combinedClickable(
                            onClick = { onOpenDrawer() },
                            onLongClick = {}
                        )
                        .testTag("dock_search_bar"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rechercher des apps ou le web...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
            }

            // Dock Pinned App Shortcuts
            Row(
                modifier = Modifier.weight(0.55f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconShape = remember(settings.expressiveIconShape) {
                    ExpressiveIconShapes.getShape(settings.expressiveIconShape)
                }
                dockApps.take(settings.dockIconCount).forEach { app ->
                    val iconBitmap = remember(app.icon) {
                        app.icon?.toBitmap()?.asImageBitmap()
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(CircleShape)
                            .combinedClickable(
                                onClick = {
                                    SystemUiUtil.triggerHapticFeedback(context, 15)
                                    onAppClick(app.packageName, app.className)
                                },
                                onLongClick = {}
                            )
                            .padding(4.dp)
                    ) {
                        if (iconBitmap != null) {
                            Image(
                                bitmap = iconBitmap,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(iconShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(iconShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = app.label.take(1),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // App Drawer Launch Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .combinedClickable(
                        onClick = {
                            SystemUiUtil.triggerHapticFeedback(context, 20)
                            onOpenDrawer()
                        },
                        onLongClick = {}
                    )
                    .testTag("app_drawer_dock_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "App Drawer",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
