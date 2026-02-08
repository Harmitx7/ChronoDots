package com.dotmatrix.calendar.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;

/**
 * Helper class for extracting Material You dynamic colors from the system.
 * For devices running Android 12+ (API 31+), this extracts colors from the 
 * user's wallpaper. For older devices, it falls back to default colors.
 * 
 * Enhanced with persistent caching to reduce color extraction overhead.
 */
public class DynamicColorHelper {
    
    // Default fallback colors - premium brand identity
    private static final int DEFAULT_BACKGROUND_LIGHT = 0xFFFAFAFA;
    private static final int DEFAULT_BACKGROUND_DARK = 0xFF0F0F14; // Deeper black for OLED
    private static final int DEFAULT_DOT_LIGHT = 0xFF1A1A1A;
    private static final int DEFAULT_DOT_DARK = 0xFFE8E8E8;
    private static final int DEFAULT_ACCENT = 0xFF6366F1; // Indigo - modern, premium
    
    private final Context context;
    private final boolean isDarkMode;
    private final ColorExtractionCache cache;
    
    public DynamicColorHelper(Context context) {
        this.context = context;
        int nightModeFlags = context.getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK;
        this.isDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        this.cache = new ColorExtractionCache(context);
    }
    
    /**
     * Check if Material You dynamic colors are available.
     */
    public static boolean isDynamicColorAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && DynamicColors.isDynamicColorAvailable();
    }
    
    /**
     * Get the primary background color based on system theme.
     */
    public int getBackgroundColor() {
        if (isDynamicColorAvailable()) {
            try {
                Context dynamicContext = DynamicColors.wrapContextIfAvailable(context);
                int colorResId = isDarkMode ? 
                    com.google.android.material.R.attr.colorSurface :
                    com.google.android.material.R.attr.colorSurface;
                return MaterialColors.getColor(dynamicContext, colorResId, 
                        isDarkMode ? DEFAULT_BACKGROUND_DARK : DEFAULT_BACKGROUND_LIGHT);
            } catch (Exception e) {
                // Fallback
            }
        }
        return isDarkMode ? DEFAULT_BACKGROUND_DARK : DEFAULT_BACKGROUND_LIGHT;
    }
    
    /**
     * Get the dot color based on system theme.
     */
    public int getDotColor() {
        if (isDynamicColorAvailable()) {
            try {
                Context dynamicContext = DynamicColors.wrapContextIfAvailable(context);
                int colorResId = com.google.android.material.R.attr.colorOnSurface;
                return MaterialColors.getColor(dynamicContext, colorResId, 
                        isDarkMode ? DEFAULT_DOT_DARK : DEFAULT_DOT_LIGHT);
            } catch (Exception e) {
                // Fallback
            }
        }
        return isDarkMode ? DEFAULT_DOT_DARK : DEFAULT_DOT_LIGHT;
    }
    
    /**
     * Get the accent color based on system theme (Material You primary).
     */
    public int getAccentColor() {
        if (isDynamicColorAvailable()) {
            try {
                Context dynamicContext = DynamicColors.wrapContextIfAvailable(context);
                int colorResId = com.google.android.material.R.attr.colorPrimary;
                return MaterialColors.getColor(dynamicContext, colorResId, DEFAULT_ACCENT);
            } catch (Exception e) {
                // Fallback
            }
        }
        return DEFAULT_ACCENT;
    }
    
    /**
     * Get complementary/secondary accent for "Chameleon Pro" advanced theming.
     */
    public int getSecondaryAccentColor() {
        if (isDynamicColorAvailable()) {
            try {
                Context dynamicContext = DynamicColors.wrapContextIfAvailable(context);
                int colorResId = com.google.android.material.R.attr.colorSecondary;
                return MaterialColors.getColor(dynamicContext, colorResId, DEFAULT_ACCENT);
            } catch (Exception e) {
                // Fallback
            }
        }
        return adjustHue(DEFAULT_ACCENT, 30); // Shift hue by 30 degrees
    }
    
    /**
     * Get tertiary color for "Chameleon Pro" advanced theming.
     */
    public int getTertiaryAccentColor() {
        if (isDynamicColorAvailable()) {
            try {
                Context dynamicContext = DynamicColors.wrapContextIfAvailable(context);
                int colorResId = com.google.android.material.R.attr.colorTertiary;
                return MaterialColors.getColor(dynamicContext, colorResId, DEFAULT_ACCENT);
            } catch (Exception e) {
                // Fallback
            }
        }
        return adjustHue(DEFAULT_ACCENT, -30); // Shift hue by -30 degrees
    }
    
    /**
     * Check if the current system theme is dark mode.
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }
    
    /**
     * Adjust the hue of a color by a given degree.
     */
    private int adjustHue(int color, float degrees) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[0] = (hsv[0] + degrees) % 360;
        if (hsv[0] < 0) hsv[0] += 360;
        return Color.HSVToColor(Color.alpha(color), hsv);
    }
    
    /**
     * Apply dynamic theme colors to standard theme representation.
     * This returns an array: [backgroundColor, dotColor, accentColor]
     * Uses persistent cache to reduce expensive extraction operations.
     */
    public int[] getDynamicHarmonyColors() {
        String themeId = "dynamic_harmony_" + (isDarkMode ? "dark" : "light");
        
        // Check cache first
        int[] cached = cache.getCachedColors(themeId);
        if (cached != null) {
            return cached;
        }
        
        // Extract fresh colors
        int bg = getBackgroundColor();
        int dot = getDotColor();
        int accent = getAccentColor();
        
        // Optimize for glassmorphism - enhance contrast
        bg = optimizeForGlassmorphism(bg, isDarkMode);
        dot = optimizeForGlassmorphism(dot, !isDarkMode);
       
        cache.cacheColors(themeId, bg, dot, accent, 
                         getSecondaryAccentColor(), getTertiaryAccentColor(), isDarkMode);
        
        return new int[] { bg, dot, accent };
    }
    
    /**
     * Apply chameleon pro theme colors (uses full Material You palette).
     * This returns an array: [backgroundColor, dotColor, accentColor]
     * Uses persistent cache to reduce expensive extraction operations.
     */
    public int[] getChameleonProColors() {
        String themeId = "chameleon_pro_" + (isDarkMode ? "dark" : "light");
        
        // Check cache first
        int[] cached = cache.getCachedColors(themeId);
        if (cached != null) {
            return cached;
        }
        
        // Extract fresh colors
        int bg = getBackgroundColor();
        int accent = getAccentColor();
        int secondary = getSecondaryAccentColor();
        
        // Optimize for glassmorphism
        bg = optimizeForGlassmorphism(bg, isDarkMode);
        accent = optimizeForGlassmorphism(accent, !isDarkMode);
        
        cache.cacheColors(themeId, bg, accent, secondary, 
                         getTertiaryAccentColor(), getTertiaryAccentColor(), isDarkMode);
        
        return new int[] { bg, accent, secondary };
    }
    
    /**
     * Optimize color for glassmorphism backgrounds.
     * Enhances saturation and adjusts lightness for better visibility on translucent surfaces.
     */
    private int optimizeForGlassmorphism(int color, boolean shouldLighten) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        
        // Boost saturation slightly for vibrancy
        hsv[1] = Math.min(1.0f, hsv[1] * 1.15f);
        
        // Adjust lightness for optimal contrast
        if (shouldLighten) {
            // Lighten for visibility on dark glass
            hsv[2] = Math.max(0.65f, Math.min(0.95f, hsv[2] * 1.25f));
        } else {
            // Darken for visibility on light glass
            hsv[2] = Math.max(0.15f, Math.min(0.45f, hsv[2] * 0.75f));
        }
        
        return Color.HSVToColor(Color.alpha(color), hsv);
    }
    
    /**
     * Invalidate color cache. Call when wallpaper changes.
     */
    public void invalidateCache() {
        cache.clearAll();
    }
}
