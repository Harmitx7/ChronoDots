package com.dotmatrix.calendar.widget.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.dotmatrix.calendar.util.PowerModeDetector;

/**
 * LRU cache for widget bitmaps with intelligent sizing.
 * Caches rendered bitmaps to avoid re-rendering on every update.
 * Adapts cache size based on device RAM for optimal performance.
 */
public class WidgetBitmapCache {

    private static final int BASE_CACHE_SIZE = 10 * 1024 * 1024; // 10MB base
    private static volatile WidgetBitmapCache INSTANCE;

    private final LruCache<String, Bitmap> cache;
    private long cacheHits = 0;
    private long cacheMisses = 0;

    private WidgetBitmapCache(Context context) {
        // Adaptive cache sizing based on available RAM
        // Fail-safe: use default if context unavailable (e.g., early boot)
        int cacheSize = BASE_CACHE_SIZE; // Default 10MB
        
        if (context != null) {
            PowerModeDetector detector = new PowerModeDetector(context);
            float multiplier = detector.getCacheSizeMultiplier();
            cacheSize = (int) (BASE_CACHE_SIZE * multiplier);
        }
        
        cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, 
                                        Bitmap oldValue, Bitmap newValue) {
                if (evicted && oldValue != null && !oldValue.isRecycled()) {
                    oldValue.recycle();
                }
            }
        };
    }

    /**
     * Get singleton instance.
     */
    public static WidgetBitmapCache getInstance() {
        return getInstance(null);
    }
    
    /**
     * Get singleton instance with context for initialization.
     * Context can be null - will use default cache size.
     */
    public static WidgetBitmapCache getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (WidgetBitmapCache.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WidgetBitmapCache(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Get cached bitmap for a widget.
     */
    public Bitmap get(int widgetId, String cacheKey) {
        String key = widgetId + "-" + cacheKey;
        Bitmap bitmap = cache.get(key);
        
        if (bitmap != null) {
            cacheHits++;
        } else {
            cacheMisses++;
        }
        
        return bitmap;
    }

    /**
     * Put bitmap in cache.
     */
    public void put(int widgetId, String cacheKey, Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        
        String key = widgetId + "-" + cacheKey;
        cache.put(key, bitmap);
    }

    /**
     * Invalidate all cached bitmaps for a widget.
     */
    public void invalidate(int widgetId) {
        String prefix = widgetId + "-";
        for (String key : cache.snapshot().keySet()) {
            if (key.startsWith(prefix)) {
                Bitmap bitmap = cache.remove(key);
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        }
    }

    /**
     * Clear entire cache.
     */
    public void clear() {
        for (Bitmap bitmap : cache.snapshot().values()) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        cache.evictAll();
        
        // Reset metrics
        cacheHits = 0;
        cacheMisses = 0;
    }

    /**
     * Trim cache to specified size.
     */
    public void trimToSize(int maxSize) {
        cache.trimToSize(maxSize);
    }

    /**
     * Generate cache key from config parameters.
     */
    public static String generateCacheKey(int widgetId, int width, int height, 
                                           String dateKey, long configHash) {
        return widgetId + "-" + width + "x" + height + "-" + dateKey + "-" + configHash;
    }
    
    /**
     * Get cache hit rate for performance monitoring.
     * Returns value between 0.0 and 1.0.
     */
    public float getCacheHitRate() {
        long total = cacheHits + cacheMisses;
        if (total == 0) {
            return 0.0f;
        }
        return (float) cacheHits / total;
    }
    
    /**
     * Get current cache size in bytes.
     */
    public int getCurrentSize() {
        return cache.size();
    }
    
    /**
     * Get maximum cache size in bytes.
     */
    public int getMaxSize() {
        return cache.maxSize();
    }
    
    /**
     * Get cache statistics for debugging.
     */
    public String getStats() {
        return String.format(
            "Cache Stats: Hits=%d, Misses=%d, Hit Rate=%.2f%%, Size=%dKB/%dKB",
            cacheHits, cacheMisses, getCacheHitRate() * 100,
            getCurrentSize() / 1024, getMaxSize() / 1024
        );
    }
}

