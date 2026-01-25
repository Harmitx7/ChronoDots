package com.dotmatrix.calendar.data.model

/**
 * Data class representing a complete widget theme.
 * Used as an intermediate object between Chameleon generation and WidgetConfig.
 */
data class WidgetTheme(
    val themeId: String,
    val name: String,
    val backgroundColor: Int,
    val dotColor: Int,
    val accentColor: Int,
    val dotColorCurrent: Int,
    val dotColorFuture: Int,
    val textPrimaryColor: Int,
    val textSecondaryColor: Int,
    val isChameleonGenerated: Boolean = false,
    val sourceWallpaperHash: String? = null
)
