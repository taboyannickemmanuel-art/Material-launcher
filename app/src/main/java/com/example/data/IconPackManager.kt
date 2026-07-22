package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.LruCache
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import kotlin.math.min

data class IconPackInfo(
    val packageName: String,
    val name: String,
    val iconDrawable: Drawable? = null,
    val isSystem: Boolean = false
)

class IconPackManager(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    // Memory-conscious LRU Cache for resolved icons (Max 64 icons cached in memory ~4MB)
    private val iconCache = object : LruCache<String, Drawable>(64) {}

    // In-memory mapping of (Component/Package -> Icon Resource Name) per Icon Pack
    private val appFilterMap = mutableMapOf<String, Map<String, String>>()

    companion object {
        private const val TAG = "IconPackManager"
        const val SYSTEM_DEFAULT = "Material Default"

        private val ICON_PACK_INTENTS = listOf(
            "org.adw.launcher.THEMES",
            "com.novalauncher.THEME",
            "com.gau.go.launcherex.theme",
            "com.dlto.atom.launcher.THEME",
            "com.fede.launcher.THEME_ICONPACK",
            "com.anddoes.launcher.THEME"
        )
    }

    /**
     * Detects all installed icon packs on the device.
     */
    fun getInstalledIconPacks(): List<IconPackInfo> {
        val list = mutableListOf<IconPackInfo>()

        // Add System / Material Default
        list.add(
            IconPackInfo(
                packageName = "system",
                name = SYSTEM_DEFAULT,
                iconDrawable = null,
                isSystem = true
            )
        )

        val iconPackPackages = mutableSetOf<String>()

        for (action in ICON_PACK_INTENTS) {
            val intent = Intent(action)
            val resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (!iconPackPackages.contains(pkg) && pkg != context.packageName) {
                    iconPackPackages.add(pkg)
                    val label = info.loadLabel(packageManager).toString()
                    val icon = info.loadIcon(packageManager)
                    list.add(
                        IconPackInfo(
                            packageName = pkg,
                            name = label,
                            iconDrawable = icon,
                            isSystem = false
                        )
                    )
                }
            }
        }

        return list
    }

    /**
     * Loads the icon for a specific app package using the chosen icon pack.
     */
    fun loadAppIcon(
        appPackageName: String,
        className: String = "",
        iconPackPackageName: String = "system",
        defaultDrawable: Drawable? = null
    ): Drawable {
        val cacheKey = "$iconPackPackageName:$appPackageName:$className"
        val cached = iconCache.get(cacheKey)
        if (cached != null) {
            return cached
        }

        val systemIcon = defaultDrawable ?: try {
            packageManager.getApplicationIcon(appPackageName)
        } catch (e: Exception) {
            null
        }

        if (iconPackPackageName == "system" || iconPackPackageName == SYSTEM_DEFAULT || iconPackPackageName.isBlank()) {
            if (systemIcon != null) {
                iconCache.put(cacheKey, systemIcon)
                return systemIcon
            }
            return createPlaceholderIcon(appPackageName)
        }

        // Try loading from Custom Icon Pack
        try {
            val iconPackRes = packageManager.getResourcesForApplication(iconPackPackageName)
            val drawableName = getIconDrawableName(iconPackPackageName, iconPackRes, appPackageName, className)

            if (drawableName != null) {
                val resId = iconPackRes.getIdentifier(drawableName, "drawable", iconPackPackageName)
                if (resId != 0) {
                    val customDrawable = iconPackRes.getDrawable(resId, null)
                    if (customDrawable != null) {
                        iconCache.put(cacheKey, customDrawable)
                        return customDrawable
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading icon from icon pack $iconPackPackageName", e)
        }

        // Fallback to system icon if custom pack doesn't have a matching icon
        val fallback = systemIcon ?: createPlaceholderIcon(appPackageName)
        iconCache.put(cacheKey, fallback)
        return fallback
    }

    /**
     * Parse appfilter.xml from the icon pack if present to map app components to drawable names.
     */
    private fun getIconDrawableName(
        iconPackPkg: String,
        iconPackRes: Resources,
        appPkg: String,
        className: String
    ): String? {
        val filterMap = appFilterMap[iconPackPkg] ?: parseAppFilterXml(iconPackPkg, iconPackRes).also {
            appFilterMap[iconPackPkg] = it
        }

        // Try exact ComponentName match: "ComponentInfo{com.example/com.example.MainActivity}"
        val componentKey = if (className.isNotEmpty()) "ComponentInfo{$appPkg/$className}" else "ComponentInfo{$appPkg}"
        filterMap[componentKey]?.let { return it }

        // Try package fallback match
        filterMap[appPkg]?.let { return it }

        // Fallback naming conventions: "com_example_app" or "com_example"
        val sanitizedPkg = appPkg.replace(".", "_").lowercase()
        val sanitizedClass = if (className.isNotEmpty()) className.replace(".", "_").lowercase() else ""

        if (sanitizedClass.isNotEmpty()) {
            val resId = iconPackRes.getIdentifier("${sanitizedPkg}_$sanitizedClass", "drawable", iconPackPkg)
            if (resId != 0) return "${sanitizedPkg}_$sanitizedClass"
        }

        val resIdPkg = iconPackRes.getIdentifier(sanitizedPkg, "drawable", iconPackPkg)
        if (resIdPkg != 0) return sanitizedPkg

        return null
    }

    private fun parseAppFilterXml(iconPackPkg: String, iconPackRes: Resources): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val appFilterResId = iconPackRes.getIdentifier("appfilter", "xml", iconPackPkg)
            val stream: InputStream? = if (appFilterResId != 0) {
                iconPackRes.openRawResource(appFilterResId)
            } else {
                try {
                    val assets = context.createPackageContext(iconPackPkg, 0).assets
                    assets.open("appfilter.xml")
                } catch (e: Exception) {
                    null
                }
            }

            if (stream != null) {
                val factory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = true
                val xpp = factory.newPullParser()
                xpp.setInput(stream, "UTF-8")

                var eventType = xpp.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && xpp.name == "item") {
                        val component = xpp.getAttributeValue(null, "component")
                        val drawable = xpp.getAttributeValue(null, "drawable")
                        if (!component.isNullOrEmpty() && !drawable.isNullOrEmpty()) {
                            map[component] = drawable
                            // Also map raw package name if component matches format
                            if (component.startsWith("ComponentInfo{") && component.contains("/")) {
                                val pkgName = component.substringAfter("ComponentInfo{").substringBefore("/")
                                map[pkgName] = drawable
                            }
                        }
                    }
                    eventType = xpp.next()
                }
                stream.close()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse appfilter.xml for $iconPackPkg", e)
        }
        return map
    }

    private fun createPlaceholderIcon(label: String): Drawable {
        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF381E72.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFEADDFF.toInt()
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
        val text = label.take(1).uppercase()
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        canvas.drawText(text, size / 2f, size / 2f - textBounds.exactCenterY(), textPaint)

        return BitmapDrawable(context.resources, bitmap)
    }

    fun clearCache() {
        iconCache.evictAll()
    }
}
