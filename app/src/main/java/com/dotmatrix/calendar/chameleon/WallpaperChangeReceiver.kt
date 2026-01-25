package com.dotmatrix.calendar.chameleon

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.dotmatrix.calendar.data.repository.WidgetRepository
import com.dotmatrix.calendar.widget.provider.YearViewWidgetProvider
import com.dotmatrix.calendar.widget.provider.MonthViewWidgetProvider
import com.dotmatrix.calendar.widget.provider.WeekViewWidgetProvider
import com.dotmatrix.calendar.widget.cache.WidgetBitmapCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.dotmatrix.calendar.data.model.WidgetConfig

/**
 * Listens for wallpaper changes and updates widgets with Chameleon Mode enabled.
 */
class WallpaperChangeReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_WALLPAPER_CHANGED) {
            return
        }
        
        // Process in background to avoid blocking UI
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleWallpaperChange(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
    
    private suspend fun handleWallpaperChange(context: Context) {
        val repository = WidgetRepository.getInstance(context)
        
        // Get all widgets that have Chameleon enabled
        // Note: We need to expose this method in Repository first
        val chameleonConfigs = repository.getWidgetsWithChameleonEnabled()
        
        if (chameleonConfigs.isEmpty()) {
            return
        }
        
        // Extract new wallpaper colors
        val colorExtractor = WallpaperColorExtractor(context)
        val newColors = try {
            colorExtractor.extractColors()
        } catch (e: Exception) {
            return
        }
        
        // Generate new theme generator
        val themeGenerator = AdaptiveThemeGenerator()
        
        chameleonConfigs.forEach { config ->
            // Generate theme with user's intensity preference
            val newTheme = themeGenerator.generateTheme(
                newColors,
                intensity = config.chameleonIntensity
            )
            
            // Update config with new theme colors
            config.setBackgroundColor(newTheme.backgroundColor)
            config.setDotColor(newTheme.dotColor)
            config.setAccentColor(newTheme.accentColor)
            config.setChameleonGenerated(true)
            
            // Save to database
            repository.saveWidgetConfigSync(config)
            
            // Clear cache for this widget
            // Clear cache for this widget
            // We need to construct cache key or just clear all for this ID? 
            // Better to invalidate all for this ID
            WidgetBitmapCache.getInstance().invalidate(config.widgetId)
            
            // Trigger update based on type
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = intArrayOf(config.getWidgetId())
            
            when (config.getWidgetType()) {
                com.dotmatrix.calendar.data.model.WidgetType.YEAR -> 
                    YearViewWidgetProvider.updateAllWidgets(context, YearViewWidgetProvider::class.java)
                com.dotmatrix.calendar.data.model.WidgetType.MONTH -> 
                     MonthViewWidgetProvider.updateAllWidgets(context, MonthViewWidgetProvider::class.java)
                com.dotmatrix.calendar.data.model.WidgetType.WEEK -> 
                     WeekViewWidgetProvider.updateAllWidgets(context, WeekViewWidgetProvider::class.java)
                else -> {}
            }
        }
    }
}
