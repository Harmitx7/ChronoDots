package com.dotmatrix.calendar.widget.glassmorphism

import android.graphics.Color

/**
 * Defines iOS-style liquid glass visual properties.
 */
data class GlassMaterial(
    // Background
    val backgroundColor: Int,           // Base tint color
    val backgroundOpacity: Float,       // 0.12-0.25 for iOS-like
    
    // Blur
    val blurRadius: Float,              // 20-30dp for strong frosting
    val blurQuality: BlurQuality = BlurQuality.HIGH,
    
    // Border/Edge
    val borderColor: Int,               // Subtle edge highlight
    val borderOpacity: Float,           // 0.2-0.3
    val borderWidth: Float,             // 1-2dp
    
    // Highlights (shimmer effect)
    val hasTopHighlight: Boolean,       // Light reflection at top
    val highlightOpacity: Float,        // 0.1-0.2
    
    // Shadows (depth)
    val shadowColor: Int,               // Soft shadow beneath
    val shadowOpacity: Float,           // 0.1-0.15
    val shadowBlur: Float,              // 8-12dp
    val shadowOffsetY: Float,           // 4-8dp downward
    
    // Dynamic effects
    val hasNoise: Boolean,              // Subtle grain texture
    val noiseOpacity: Float             // 0.02-0.05
) {
    companion object {
        /**
         * iOS-style light glass (for light wallpapers)
         */
        @JvmStatic
        fun iosLightGlass(): GlassMaterial = GlassMaterial(
            backgroundColor = Color.WHITE,
            backgroundOpacity = 0.15f,
            blurRadius = 25f,
            blurQuality = BlurQuality.HIGH,
            borderColor = Color.WHITE,
            borderOpacity = 0.45f,
            borderWidth = 1.5f,
            hasTopHighlight = true,
            highlightOpacity = 0.15f,
            shadowColor = Color.BLACK,
            shadowOpacity = 0.12f,
            shadowBlur = 10f,
            shadowOffsetY = 6f,
            hasNoise = true,
            noiseOpacity = 0.03f
        )
        
        /**
         * iOS-style dark glass (for dark wallpapers)
         */
        @JvmStatic
        fun iosDarkGlass(): GlassMaterial = GlassMaterial(
            backgroundColor = Color.BLACK,
            backgroundOpacity = 0.35f,
            blurRadius = 25f,
            blurQuality = BlurQuality.HIGH,
            borderColor = Color.WHITE,
            borderOpacity = 0.15f,
            borderWidth = 1.0f,
            hasTopHighlight = true,
            highlightOpacity = 0.08f,
            shadowColor = Color.BLACK,
            shadowOpacity = 0.35f,
            shadowBlur = 16f,
            shadowOffsetY = 8f,
            hasNoise = true,
            noiseOpacity = 0.04f
        )
    }
}

enum class BlurQuality {
    HIGH,    // Full resolution blur (slow, beautiful)
    MEDIUM,  // 1/2 resolution blur (balanced)
    LOW      // 1/4 resolution blur (fast, acceptable)
}
