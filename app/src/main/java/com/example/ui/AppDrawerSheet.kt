package com.example.ui

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.AppInfo
import com.example.data.LauncherSettings
import com.example.util.SystemUiUtil

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerSheet(
    isOpen: Boolean,
    apps: List<AppInfo>,
    searchQuery: String,
    settings: LauncherSettings,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppInfoClick: (AppInfo) -> Unit,
    onHideAppClick: (AppInfo) -> Unit,
    onAddToDesktopClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = settings.drawerBackgroundOpacity)
                )
                .combinedClickable(
                    onClick = { onClose() },
                    onLongClick = {}
                )
                .testTag("app_drawer_sheet")
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.88f)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .combinedClickable(
                        enabled = true,
                        onClick = {},
                        onLongClick = {}
                    ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                        alpha = settings.drawerBackgroundOpacity.coerceIn(0.20f, 1.0f)
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drawer_search_input"),
                        placeholder = {
                            Text("Rechercher des applications...")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Toutes les applications (${apps.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(settings.drawerColumns),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = apps,
                            key = { it.packageName + "_" + it.className },
                            contentType = { "app_drawer_item" }
                        ) { app ->
                            AppDrawerItem(
                                app = app,
                                settings = settings,
                                onClick = {
                                    SystemUiUtil.triggerHapticFeedback(context, 15)
                                    onAppClick(app)
                                },
                                onAppInfo = { onAppInfoClick(app) },
                                onHideApp = { onHideAppClick(app) },
                                onAddToDesktop = { onAddToDesktopClick(app) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerItem(
    app: AppInfo,
    settings: LauncherSettings,
    onClick: () -> Unit,
    onAppInfo: () -> Unit,
    onHideApp: () -> Unit,
    onAddToDesktop: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.TopEnd
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        SystemUiUtil.triggerHapticFeedback(context, 35)
                        showMenu = true
                    }
                )
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier.size(52.dp)
            ) {
                val iconShape = remember(settings.expressiveIconShape) {
                    ExpressiveIconShapes.getShape(settings.expressiveIconShape)
                }
                val iconBitmap = remember(app.icon) {
                    app.icon?.toBitmap()?.asImageBitmap()
                }

                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = app.label,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(iconShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(iconShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.label.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (settings.enableNotificationDots && app.notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (app.notificationCount > 9) "9+" else "${app.notificationCount}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = app.label,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = settings.iconTextSizeSp.sp),
                color = MaterialTheme.colorScheme.onSurface,
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
                text = { Text("Ajouter à l'écran d'accueil") },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                onClick = {
                    showMenu = false
                    onAddToDesktop()
                }
            )
            DropdownMenuItem(
                text = { Text("Info Application") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = "Info") },
                onClick = {
                    showMenu = false
                    onAppInfo()
                }
            )
            DropdownMenuItem(
                text = { Text("Masquer l'application") },
                leadingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = "Hide") },
                onClick = {
                    showMenu = false
                    onHideApp()
                }
            )
        }
    }
}
