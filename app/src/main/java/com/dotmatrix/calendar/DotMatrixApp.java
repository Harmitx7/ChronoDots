package com.dotmatrix.calendar;

import android.app.Application;
import android.content.ComponentCallbacks2;

import com.dotmatrix.calendar.receiver.MidnightUpdateReceiver;
import com.dotmatrix.calendar.widget.cache.WidgetBitmapCache;

/**
 * Application class for Dot Matrix Calendar.
 * Handles initialization and memory management.
 */
public class DotMatrixApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Schedule midnight updates
        MidnightUpdateReceiver.scheduleMidnightUpdate(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // Clear bitmap cache to free memory
        WidgetBitmapCache.getInstance().clear();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            // Critical memory - clear all cache
            WidgetBitmapCache.getInstance().clear();
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            // UI hidden - trim cache
            WidgetBitmapCache.getInstance().trimToSize(10 * 1024 * 1024); // 10MB
        }
    }
}
