package com.dotmatrix.calendar.widget.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * LRU cache for widget bitmaps.
 * Caches rendered bitmaps to avoid re-rendering on every update.
 */
public class WidgetBitmapCache {

    private static final int MAX_CACHE_SIZE = 10 * 1024 * 1024; // 10MB (optimized)
    private static volatile WidgetBitmapCache INSTANCE;

    private final LruCache<String, Bitmap> cache;

    private WidgetBitmapCache() {
        cache = new LruCache<String, Bitmap>(MAX_CACHE_SIZE) {
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
        if (INSTANCE == null) {
            synchronized (WidgetBitmapCache.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WidgetBitmapCache();
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
        return cache.get(key);
    }

    /**
     * Put bitmap in cache.
     */
    public void put(int widgetId, String cacheKey, Bitmap bitmap) {
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
}
