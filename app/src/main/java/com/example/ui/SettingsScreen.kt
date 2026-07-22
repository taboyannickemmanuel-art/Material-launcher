package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Dock
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.AppInfo
import com.example.data.IconPackInfo
import com.example.data.LauncherSettings
import com.example.util.SystemUiUtil

enum class SettingsCategory(val title: String, val icon: ImageVector) {
    GENERAL("Général", Icons.Default.ColorLens),
    HOME_SCREEN("Écran d'accueil", Icons.Default.GridView),
    AT_A_GLANCE("At a Glance", Icons.Default.Widgets),
    DOCK("Dock", Icons.Default.Dock),
    APP_DRAWER("Tiroir d'apps", Icons.Default.AppShortcut),
    GESTURES("Gestes", Icons.Default.Gesture),
    ABOUT("À propos", Icons.Default.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: LauncherSettings,
    installedApps: List<AppInfo>,
    availableIconPacks: List<IconPackInfo> = emptyList(),
    onUpdateSettings: (LauncherSettings) -> Unit,
    onToggleHideApp: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(SettingsCategory.GENERAL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres - Material Launcher") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        // Lawnchair 14/15 2-Pane Master-Detail layout (30% nav / 70% content)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Master Pane (Left 30%)
            Card(
                modifier = Modifier
                    .weight(0.30f)
                    .fillMaxHeight()
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(SettingsCategory.entries.toTypedArray()) { category ->
                        val isSelected = category == selectedCategory
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.title,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Detail Pane (Right 70%)
            Card(
                modifier = Modifier
                    .weight(0.70f)
                    .fillMaxHeight()
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    AnimatedContent(
                        targetState = selectedCategory,
                        label = "SettingsDetail"
                    ) { category ->
                        when (category) {
                            SettingsCategory.GENERAL -> GeneralSettingsDetail(settings, availableIconPacks, onUpdateSettings)
                            SettingsCategory.HOME_SCREEN -> HomeScreenSettingsDetail(settings, onUpdateSettings)
                            SettingsCategory.AT_A_GLANCE -> AtAGlanceSettingsDetail(settings, onUpdateSettings)
                            SettingsCategory.DOCK -> DockSettingsDetail(settings, onUpdateSettings)
                            SettingsCategory.APP_DRAWER -> AppDrawerSettingsDetail(settings, installedApps, onUpdateSettings, onToggleHideApp)
                            SettingsCategory.GESTURES -> GesturesSettingsDetail(settings, onUpdateSettings)
                            SettingsCategory.ABOUT -> AboutSettingsDetail()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeneralSettingsDetail(
    settings: LauncherSettings,
    availableIconPacks: List<IconPackInfo> = emptyList(),
    onUpdateSettings: (LauncherSettings) -> Unit
) {
    var showIconPackDialog by remember { mutableStateOf(false) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Thème & Couleurs Monet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Palette dynamique basée sur le fond d'écran (Monet Backport)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            SettingSwitchCard(
                title = "Thème Sombre Monet",
                description = "Utiliser la palette sombre dynamique extraite du fond d'écran",
                checked = settings.darkTheme,
                onCheckedChange = { onUpdateSettings(settings.copy(darkTheme = it)) }
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showIconPackDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pack d'icônes (Icon Pack)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Actuel : ${if (settings.iconPackName == "system") "Material Default" else settings.iconPackName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.Tune, contentDescription = "Icon Pack")
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Formes d'icônes expressives (Expressive Shapes)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sélectionnez une forme adaptative Material 3 Expressive pour toutes les icônes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ExpressiveIconShapes.ALL_SHAPES) { shapeName ->
                            val isSelected = settings.expressiveIconShape.equals(shapeName, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdateSettings(settings.copy(expressiveIconShape = shapeName)) },
                                label = { Text(shapeName) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(ExpressiveIconShapes.getShape(shapeName))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.primary
                                            )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingSwitchCard(
                title = "Pastilles de Notification (Notification Dots)",
                description = "Afficher les pastilles rouges avec le nombre de notifications sur les icônes",
                checked = settings.enableNotificationDots,
                onCheckedChange = { onUpdateSettings(settings.copy(enableNotificationDots = it)) }
            )
        }
    }

    if (showIconPackDialog) {
        AlertDialog(
            onDismissRequest = { showIconPackDialog = false },
            title = { Text("Choisir un pack d'icônes") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    val packs = availableIconPacks.ifEmpty {
                        listOf(IconPackInfo("system", "Material Default", null, true))
                    }
                    items(packs) { pack ->
                        val isSelected = (pack.packageName == settings.iconPackName) ||
                                (pack.isSystem && settings.iconPackName == "Material Default") ||
                                (pack.isSystem && settings.iconPackName == "system")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                                .clickable {
                                    onUpdateSettings(settings.copy(iconPackName = pack.packageName))
                                    showIconPackDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            if (pack.iconDrawable != null) {
                                Image(
                                    bitmap = pack.iconDrawable.toBitmap().asImageBitmap(),
                                    contentDescription = pack.name,
                                    modifier = Modifier.size(36.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AppShortcut,
                                    contentDescription = pack.name,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = pack.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIconPackDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun HomeScreenSettingsDetail(
    settings: LauncherSettings,
    onUpdateSettings: (LauncherSettings) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Grille & Écran d'accueil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Personnalisation de la grille d'icônes paysage pour tablette 10 pouces", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Lignes de la grille: ${settings.gridRows}", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = settings.gridRows.toFloat(),
                        onValueChange = { onUpdateSettings(settings.copy(gridRows = it.toInt())) },
                        valueRange = 3f..6f,
                        steps = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Colonnes de la grille: ${settings.gridColumns}", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = settings.gridColumns.toFloat(),
                        onValueChange = { onUpdateSettings(settings.copy(gridColumns = it.toInt())) },
                        valueRange = 6f..12f,
                        steps = 5
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Taille du texte des icônes: ${settings.iconTextSizeSp.toInt()} sp", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = settings.iconTextSizeSp,
                        onValueChange = { onUpdateSettings(settings.copy(iconTextSizeSp = it)) },
                        valueRange = 10f..16f,
                        steps = 5
                    )
                }
            }
        }

        item {
            SettingSwitchCard(
                title = "Verrouiller le bureau",
                description = "Empêcher le déplacement ou la suppression accidentelle des raccourcis et widgets",
                checked = settings.isDesktopLocked,
                onCheckedChange = { onUpdateSettings(settings.copy(isDesktopLocked = it)) }
            )
        }
    }
}

@Composable
fun AtAGlanceSettingsDetail(
    settings: LauncherSettings,
    onUpdateSettings: (LauncherSettings) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Widget 'At a Glance' (À un coup d'œil)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Configurez les éléments affichés dans le widget supérieur", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            SettingSwitchCard(
                title = "Activer At a Glance",
                description = "Afficher la carte résumé en haut de l'écran d'accueil",
                checked = settings.showAtAGlance,
                onCheckedChange = { onUpdateSettings(settings.copy(showAtAGlance = it)) }
            )
        }

        item {
            SettingSwitchCard(
                title = "Météo en direct",
                description = "Afficher la température et l'état de la météo",
                checked = settings.showWeather,
                onCheckedChange = { onUpdateSettings(settings.copy(showWeather = it)) }
            )
        }

        item {
            SettingSwitchCard(
                title = "Date & Calendrier",
                description = "Afficher la date actuelle du jour",
                checked = settings.showDate,
                onCheckedChange = { onUpdateSettings(settings.copy(showDate = it)) }
            )
        }

        item {
            SettingSwitchCard(
                title = "Indicateur de Batterie",
                description = "Afficher le pourcentage et l'état de charge de la batterie",
                checked = settings.showBattery,
                onCheckedChange = { onUpdateSettings(settings.copy(showBattery = it)) }
            )
        }

        item {
            SettingSwitchCard(
                title = "Now Playing (Lecteur Musique)",
                description = "Afficher le titre de la musique et les contrôles multimédia",
                checked = settings.showNowPlaying,
                onCheckedChange = { onUpdateSettings(settings.copy(showNowPlaying = it)) }
            )
        }
    }
}

@Composable
fun DockSettingsDetail(
    settings: LauncherSettings,
    onUpdateSettings: (LauncherSettings) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Barre Dock inférieure", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Configuration de la barre d'applications ancrées et de recherche", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            SettingSwitchCard(
                title = "Afficher le Dock",
                description = "Afficher le panneau du dock en bas de l'écran",
                checked = settings.showDock,
                onCheckedChange = { onUpdateSettings(settings.copy(showDock = it)) }
            )
        }

        item {
            SettingSwitchCard(
                title = "Barre de recherche Material 3",
                description = "Intégrer la barre de recherche globale dans le dock",
                checked = settings.showDockSearchBar,
                onCheckedChange = { onUpdateSettings(settings.copy(showDockSearchBar = it)) }
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nombre d'icônes dans le Dock: ${settings.dockIconCount}", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = settings.dockIconCount.toFloat(),
                        onValueChange = { onUpdateSettings(settings.copy(dockIconCount = it.toInt())) },
                        valueRange = 4f..8f,
                        steps = 3
                    )
                }
            }
        }

        item {
            SettingSwitchCard(
                title = "Fond Translucide du Dock",
                description = "Appliquer un fond dépoli translucide Material You sous le dock",
                checked = settings.isDockTranslucent,
                onCheckedChange = { onUpdateSettings(settings.copy(isDockTranslucent = it)) }
            )
        }
    }
}

@Composable
fun AppDrawerSettingsDetail(
    settings: LauncherSettings,
    installedApps: List<AppInfo>,
    onUpdateSettings: (LauncherSettings) -> Unit,
    onToggleHideApp: (String) -> Unit
) {
    var showHideDialog by remember { mutableStateOf(false) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Tiroir d'applications (App Drawer)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Réglages du panneau glissant paysage 10 pouces", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val opacityPercent = (settings.drawerBackgroundOpacity * 100).toInt()
                    Text(
                        text = "Transparence du tiroir (Android 16 Style) : $opacityPercent%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ajustez le niveau de transparence du fond dépoli lors de l'ouverture du tiroir d'applications",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = settings.drawerBackgroundOpacity,
                        onValueChange = { onUpdateSettings(settings.copy(drawerBackgroundOpacity = it)) },
                        valueRange = 0.20f..1.00f,
                        steps = 15
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Colonnes dans le tiroir: ${settings.drawerColumns}", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = settings.drawerColumns.toFloat(),
                        onValueChange = { onUpdateSettings(settings.copy(drawerColumns = it.toInt())) },
                        valueRange = 5f..10f,
                        steps = 4
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showHideDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Masquer des applications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Actuellement masquées: ${settings.hiddenPackages.size} application(s)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.Visibility, contentDescription = "Hide apps")
                }
            }
        }
    }

    if (showHideDialog) {
        AlertDialog(
            onDismissRequest = { showHideDialog = false },
            title = { Text("Masquer des applications") },
            text = {
                var filterText by remember { mutableStateOf("") }
                Column(modifier = Modifier.height(350.dp)) {
                    OutlinedTextField(
                        value = filterText,
                        onValueChange = { filterText = it },
                        placeholder = { Text("Filtrer...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        val filtered = installedApps.filter { it.label.lowercase().contains(filterText.lowercase()) }
                        items(filtered) { app ->
                            val isHidden = settings.hiddenPackages.contains(app.packageName)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleHideApp(app.packageName) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isHidden,
                                    onCheckedChange = { onToggleHideApp(app.packageName) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(app.label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHideDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun GesturesSettingsDetail(
    settings: LauncherSettings,
    onUpdateSettings: (LauncherSettings) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Gestes de navigation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Associez des actions aux balayages et tapotements tactiles", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Glissement vers le bas (Swipe Down)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Action actuelle : " + when(settings.swipeDownGesture) {
                        "notifications" -> "Panneau de notifications"
                        "search" -> "Recherche d'applications"
                        "settings" -> "Paramètres du launcher"
                        else -> "Aucune"
                    }, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun AboutSettingsDetail() {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("À propos - Material Launcher", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Version 1.0.0 (Build Lawnchair 14/15 Expressive)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Spécifications & Optimisations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Appareil cible: Tablette 10 pouces Paysage (2 Go RAM)")
                    Text("• Moteur de fluidité: Physics Spring Animations (Target 60 FPS)")
                    Text("• Rendu mémoire: LRU Icon Caching & Hardware Layers")
                    Text("• Monet Engine: Backport palette dynamique Android 7.0+ (API 24)")
                    Text("• Mode Immersif: Immersive Sticky zéro masque d'angle")
                }
            }
        }
    }
}

@Composable
fun SettingSwitchCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
