package com.dotmatrix.calendar.chameleon

import android.app.WallpaperManager
import android.app.WallpaperColors
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.palette.graphics.Palette
import androidx.core.graphics.drawable.toBitmap

/**
 * Extracts dominant colors from the system wallpaper.
 * Uses Android 8.1+ native WallpaperColors API when available,
 * falls back to Palette library for older versions.
 */
class WallpaperColorExtractor(private val context: Context) {
    
    /**
     * Extracts colors from current wallpaper.
     * @return WallpaperThemeColors containing primary, secondary, tertiary colors
     */
    fun extractColors(): WallpaperThemeColors {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                extractColorsNative()
            } else {
                extractColorsPalette()
            }
        } catch (e: Exception) {
            android.util.Log.e("WallpaperColorExtractor", "Wallpaper extraction failed", e)
            getDefaultColors()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O_MR1)
    private fun extractColorsNative(): WallpaperThemeColors {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val wallpaperColors = wallpaperManager.getWallpaperColors(
            WallpaperManager.FLAG_SYSTEM
        ) ?: return getDefaultColors()
        
        return WallpaperThemeColors(
            primary = wallpaperColors.primaryColor.toArgb(),
            secondary = wallpaperColors.secondaryColor?.toArgb() 
                ?: deriveSecondaryColor(wallpaperColors.primaryColor.toArgb()),
            tertiary = wallpaperColors.tertiaryColor?.toArgb()
                ?: deriveTertiaryColor(wallpaperColors.primaryColor.toArgb()),
            isDark = (wallpaperColors.colorHints and 
                WallpaperColors.HINT_SUPPORTS_DARK_TEXT) == 0
        )
    }
    
    private fun extractColorsPalette(): WallpaperThemeColors {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val wallpaperDrawable = wallpaperManager.drawable 
            ?: return getDefaultColors()
        
        // Scale down for performance (max 400px on longest side)
        val bitmap = wallpaperDrawable.toBitmap(
            width = 400,
            height = 400,
            config = Bitmap.Config.ARGB_8888
        )
        
        val palette = Palette.from(bitmap).generate()
        
        // Prefer vibrant colors, fall back to muted/dominant
        val primaryColor = palette.vibrantSwatch?.rgb
            ?: palette.dominantSwatch?.rgb
            ?: Color.BLACK
            
        val secondaryColor = palette.lightVibrantSwatch?.rgb
            ?: palette.lightMutedSwatch?.rgb
            ?: deriveSecondaryColor(primaryColor)
            
        val tertiaryColor = palette.darkVibrantSwatch?.rgb
            ?: palette.darkMutedSwatch?.rgb
            ?: deriveTertiaryColor(primaryColor)
        
        // Recycle bitmap to free memory doesn't seem necessary with KTX extension but good practice if managing manually
        // bitmap.recycle() // KTX toBitmap creates new bitmap, safe to recycle if not used elsewhere? Yes.
        
        return WallpaperThemeColors(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = tertiaryColor,
            isDark = calculateLuminance(primaryColor) < 0.5
        )
    }
    
    private fun deriveSecondaryColor(primaryColor: Int): Int {
        // Lighten primary color by 30%
        return adjustLightness(primaryColor, 0.3f)
    }
    
    private fun deriveTertiaryColor(primaryColor: Int): Int {
        // Get complementary color (opposite on color wheel)
        return getComplementaryColor(primaryColor)
    }
    
    private fun getDefaultColors(): WallpaperThemeColors {
        // Return default theme as fallback
        return WallpaperThemeColors(
            primary = 0xFF1A1625.toInt(),
            secondary = 0xFFD4A574.toInt(),
            tertiary = 0xFFE0E0E0.toInt(),
            isDark = true
        )
    }
    
    private fun calculateLuminance(color: Int): Double {
        // Use standard relative luminance formula
        val r = Color.red(color) / 255.0
        val g = Color.green(color) / 255.0
        val b = Color.blue(color) / 255.0
        
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }
    
    private fun adjustLightness(color: Int, amount: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] = (hsv[2] + amount).coerceIn(0f, 1f)
        return Color.HSVToColor(hsv)
    }
    
    private fun getComplementaryColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[0] = (hsv[0] + 180) % 360
        return Color.HSVToColor(hsv)
    }
}

/**
 * Data class holding extracted wallpaper colors
 */
data class WallpaperThemeColors(
    val primary: Int,      // Dominant color from wallpaper
    val secondary: Int,    // Complementary or light variant
    val tertiary: Int,     // Accent or dark variant
    val isDark: Boolean    // Whether wallpaper is predominantly dark
)
