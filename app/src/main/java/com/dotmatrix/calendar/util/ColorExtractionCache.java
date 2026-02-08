package com.dotmatrix.calendar.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Persistent cache for wallpaper color extraction results.
 * Reduces expensive color computation by caching results for 24 hours.
 * 
 * iOS-inspired: Similar to how iOS caches wallpaper tint for widgets.
 */
public class ColorExtractionCache {
    
    private static final String PREFS_NAME = "color_extraction_cache";
    private static final String KEY_CACHE_DATA = "cache_data";
    private static final long CACHE_TTL_MS = 24 * 60 * 60 * 1000; // 24 hours
    
    private final SharedPreferences prefs;
    private final Gson gson;
    
    private static class CacheEntry {
        @SerializedName("timestamp")
        long timestamp;
        
        @SerializedName("background")
        int backgroundColor;
        
        @SerializedName("dot")
        int dotColor;
        
        @SerializedName("accent")
        int accentColor;
        
        @SerializedName("secondary")
        int secondaryColor;
        
        @SerializedName("tertiary")
        int tertiaryColor;
        
        @SerializedName("is_dark")
        boolean isDark;
        
        CacheEntry(int bg, int dot, int accent, int secondary, int tertiary, boolean isDark) {
            this.timestamp = System.currentTimeMillis();
            this.backgroundColor = bg;
            this.dotColor = dot;
            this.accentColor = accent;
            this.secondaryColor = secondary;
            this.tertiaryColor = tertiary;
            this.isDark = isDark;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }
    
    public ColorExtractionCache(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        // Use application context to avoid memory leaks
        this.prefs = context.getApplicationContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    /**
     * Get cached colors if available and not expired.
     * Returns null if cache miss or expired.
     */
    public int[] getCachedColors(String themeId) {
        String json = prefs.getString(KEY_CACHE_DATA + "_" + themeId, null);
        if (json == null) {
            return null;
        }
        
        try {
            CacheEntry entry = gson.fromJson(json, CacheEntry.class);
            if (entry == null || entry.isExpired()) {
                return null;
            }
            
            return new int[] {
                entry.backgroundColor,
                entry.dotColor,
                entry.accentColor
            };
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Cache extracted colors with timestamp.
     */
    public void cacheColors(String themeId, int bg, int dot, int accent, 
                           int secondary, int tertiary, boolean isDark) {
        CacheEntry entry = new CacheEntry(bg, dot, accent, secondary, tertiary, isDark);
        String json = gson.toJson(entry);
        
        prefs.edit()
            .putString(KEY_CACHE_DATA + "_" + themeId, json)
            .apply();
    }
    
    /**
     * Invalidate cache for a specific theme.
     */
    public void invalidate(String themeId) {
        prefs.edit()
            .remove(KEY_CACHE_DATA + "_" + themeId)
            .apply();
    }
    
    /**
     * Clear all cached colors.
     */
    public void clearAll() {
        prefs.edit().clear().apply();
    }
    
    /**
     * Check if cache exists for a theme and is not expired.
     */
    public boolean isCacheValid(String themeId) {
        String json = prefs.getString(KEY_CACHE_DATA + "_" + themeId, null);
        if (json == null) {
            return false;
        }
        
        try {
            CacheEntry entry = gson.fromJson(json, CacheEntry.class);
            return entry != null && !entry.isExpired();
        } catch (Exception e) {
            return false;
        }
    }
}
