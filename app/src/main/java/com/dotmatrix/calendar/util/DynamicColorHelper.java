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
 */
public class DynamicColorHelper {
    
    // Default fallback colors
    private static final int DEFAULT_BACKGROUND_LIGHT = 0xFFFAFAFA;
    private static final int DEFAULT_BACKGROUND_DARK = 0xFF121212;
    private static final int DEFAULT_DOT_LIGHT = 0xFF1A1A1A;
    private static final int DEFAULT_DOT_DARK = 0xFFE8E8E8;
    private static final int DEFAULT_ACCENT = 0xFF6750A4; // Material default purple
    
    private final Context context;
    private final boolean isDarkMode;
    
    public DynamicColorHelper(Context context) {
        this.context = context;
        int nightModeFlags = context.getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK;
        this.isDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
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
     */
    public int[] getDynamicHarmonyColors() {
        return new int[] {
            getBackgroundColor(),
            getDotColor(),
            getAccentColor()
        };
    }
    
    /**
     * Apply chameleon pro theme colors (uses full Material You palette).
     * This returns an array: [backgroundColor, dotColor, accentColor]
     */
    public int[] getChameleonProColors() {
        return new int[] {
            getBackgroundColor(),
            getAccentColor(), // Dots use accent color for chameleon
            getSecondaryAccentColor()
        };
    }
}
