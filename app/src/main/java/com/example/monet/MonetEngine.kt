package com.example.monet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import com.example.R

/**
 * MonetEngine backports Material You dynamic color palette generation
 * to Android 7.0+ (API 24+) using androidx.palette:palette-ktx.
 */
object MonetEngine {

    data class MonetPalette(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val surface: Color,
        val background: Color,
        val onPrimary: Color,
        val onSecondary: Color,
        val onSurface: Color,
        val accent: Color
    )

    fun getDefaultPalette(darkTheme: Boolean = true): MonetPalette {
        return if (darkTheme) {
            MonetPalette(
                primary = Color(0xFFD0BCFF),
                secondary = Color(0xFFCCC2DC),
                tertiary = Color(0xFFEFB8C8),
                surface = Color(0xFF1D1B20),
                background = Color(0xFF141218),
                onPrimary = Color(0xFF381E72),
                onSecondary = Color(0xFF332D41),
                onSurface = Color(0xFFE6E0E9),
                accent = Color(0xFFD0BCFF)
            )
        } else {
            MonetPalette(
                primary = Color(0xFF6750A4),
                secondary = Color(0xFF625B71),
                tertiary = Color(0xFF7D5260),
                surface = Color(0xFFFEF7FF),
                background = Color(0xFFF7F2FA),
                onPrimary = Color(0xFFFFFFFF),
                onSecondary = Color(0xFFFFFFFF),
                onSurface = Color(0xFF1D1B20),
                accent = Color(0xFF6750A4)
            )
        }
    }

    fun extractPaletteFromBitmap(bitmap: Bitmap, darkTheme: Boolean = true): MonetPalette {
        val palette = Palette.from(bitmap).generate()

        val dominant = palette.getDominantColor(if (darkTheme) 0xFFD0BCFF.toInt() else 0xFF6750A4.toInt())
        val vibrant = palette.getVibrantColor(dominant)
        val darkVibrant = palette.getDarkVibrantColor(dominant)
        val lightVibrant = palette.getLightVibrantColor(dominant)
        val muted = palette.getMutedColor(dominant)
        val darkMuted = palette.getDarkMutedColor(dominant)

        return if (darkTheme) {
            MonetPalette(
                primary = Color(lightVibrant),
                secondary = Color(vibrant),
                tertiary = Color(muted),
                surface = Color(0xFF1C1B1F),
                background = Color(0xFF121212),
                onPrimary = Color(0xFF121212),
                onSecondary = Color(0xFF121212),
                onSurface = Color(0xFFE6E0E9),
                accent = Color(lightVibrant)
            )
        } else {
            MonetPalette(
                primary = Color(darkVibrant),
                secondary = Color(vibrant),
                tertiary = Color(darkMuted),
                surface = Color(0xFFFEF7FF),
                background = Color(0xFFF4F0F6),
                onPrimary = Color(0xFFFFFFFF),
                onSecondary = Color(0xFFFFFFFF),
                onSurface = Color(0xFF1D1B20),
                accent = Color(darkVibrant)
            )
        }
    }

    fun extractPaletteFromResource(context: Context, resId: Int, darkTheme: Boolean = true): MonetPalette {
        return try {
            val bitmap = BitmapFactory.decodeResource(context.resources, resId)
            if (bitmap != null) {
                extractPaletteFromBitmap(bitmap, darkTheme)
            } else {
                getDefaultPalette(darkTheme)
            }
        } catch (e: Exception) {
            getDefaultPalette(darkTheme)
        }
    }

    fun createColorScheme(monetPalette: MonetPalette, darkTheme: Boolean): ColorScheme {
        return if (darkTheme) {
            darkColorScheme(
                primary = monetPalette.primary,
                secondary = monetPalette.secondary,
                tertiary = monetPalette.tertiary,
                surface = monetPalette.surface,
                background = monetPalette.background,
                onPrimary = monetPalette.onPrimary,
                onSecondary = monetPalette.onSecondary,
                onSurface = monetPalette.onSurface
            )
        } else {
            lightColorScheme(
                primary = monetPalette.primary,
                secondary = monetPalette.secondary,
                tertiary = monetPalette.tertiary,
                surface = monetPalette.surface,
                background = monetPalette.background,
                onPrimary = monetPalette.onPrimary,
                onSecondary = monetPalette.onSecondary,
                onSurface = monetPalette.onSurface
            )
        }
    }
}
