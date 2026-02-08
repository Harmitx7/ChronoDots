package com.dotmatrix.calendar;

import android.app.Application;

import com.dotmatrix.calendar.widget.cache.WidgetBitmapCache;

/**
 * Application class for ChronoDots.
 * Initializes singletons and performs app-wide setup.
 */
public class DotMatrixApp extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize bitmap cache with context early
        // This allows proper adaptive sizing based on device RAM
        WidgetBitmapCache.getInstance(this);
    }
}
