package com.dotmatrix.calendar.widget.glassmorphism

import android.graphics.Bitmap
import android.util.LruCache

object GlassEffectCache {
    private val cache = object : LruCache<String, Bitmap>(15 * 1024 * 1024) { // 15MB Cache
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }
    
    /**
     * Generates cache key from glass parameters.
     */
    fun getCacheKey(
        widgetId: Int,
        width: Int,
        height: Int,
        blurRadius: Float,
        isDark: Boolean
    ): String {
        return "glass_${widgetId}_${width}x${height}_${blurRadius}_$isDark"
    }
    
    /**
     * Gets cached glass background or null.
     */
    fun get(key: String): Bitmap? = cache.get(key)
    
    /**
     * Caches rendered glass background.
     */
    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }
    
    /**
     * Invalidates cache for specific widget.
     */
    fun invalidate(widgetId: Int) {
        val snapshot = cache.snapshot()
        for (key in snapshot.keys) {
            if (key.startsWith("glass_$widgetId")) {
                cache.remove(key)
            }
        }
    }

    fun clear() {
        cache.evictAll()
    }
}
