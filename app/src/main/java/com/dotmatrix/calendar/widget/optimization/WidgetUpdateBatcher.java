package com.dotmatrix.calendar.widget.optimization;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intelligent widget update batcher that groups multiple update requests
 * within a time window to reduce rendering overhead and improve performance.
 * 
 * iOS-inspired optimization: Batch updates similar to UICollectionView's batch updates.
 */
public class WidgetUpdateBatcher {
    
    private static final long BATCH_WINDOW_MS = 500; // 500ms batching window
    private static volatile WidgetUpdateBatcher INSTANCE;
    
    private final Handler mainHandler;
    private final Map<Integer, PendingUpdate> pendingUpdates;
    private final Set<Integer> processingWidgets;
    private Runnable batchRunnable;
    private final java.util.concurrent.ExecutorService executor;
    
    public interface UpdateCallback {
        void onUpdate(int widgetId, int width, int height, Bundle options);
    }
    
    private static class PendingUpdate {
        final int widgetId;
        final int width;
        final int height;
        final Bundle options;
        final UpdateCallback callback;
        final long timestamp;
        
        PendingUpdate(int widgetId, int width, int height, Bundle options, 
                     UpdateCallback callback) {
            this.widgetId = widgetId;
            this.width = width;
            this.height = height;
            this.options = options;
            this.callback = callback;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    private WidgetUpdateBatcher() {
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.pendingUpdates = new ConcurrentHashMap<>();
        // Thread-safe set instead of HashSet
        this.processingWidgets = ConcurrentHashMap.newKeySet();
        this.executor = java.util.concurrent.Executors.newFixedThreadPool(2);
    }
    
    public static WidgetUpdateBatcher getInstance() {
        if (INSTANCE == null) {
            synchronized (WidgetUpdateBatcher.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WidgetUpdateBatcher();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Schedule a widget update with batching.
     * If multiple updates for the same widget arrive within BATCH_WINDOW_MS,
     * only the last one will be processed.
     */
    public void scheduleUpdate(int widgetId, int width, int height, 
                               Bundle options, UpdateCallback callback) {
        
        // Check if this widget is already being processed
        synchronized (processingWidgets) {
            if (processingWidgets.contains(widgetId)) {
                // Queue for next batch
                pendingUpdates.put(widgetId, 
                    new PendingUpdate(widgetId, width, height, options, callback));
                return;
            }
        }
        
        // Add to pending updates (replaces existing if already queued)
        pendingUpdates.put(widgetId, 
            new PendingUpdate(widgetId, width, height, options, callback));
        
        // Cancel existing batch runnable
        if (batchRunnable != null) {
            mainHandler.removeCallbacks(batchRunnable);
        }
        
        // Schedule new batch processing
        batchRunnable = this::processBatch;
        mainHandler.postDelayed(batchRunnable, BATCH_WINDOW_MS);
    }
    
    /**
     * Process all pending updates in the batch.
     * Runs on background thread to avoid blocking main thread.
     */
    private void processBatch() {
        if (pendingUpdates.isEmpty()) {
            return;
        }
        
        // Snapshot current pending updates (fast, on main thread)
        final Map<Integer, PendingUpdate> snapshot = new HashMap<>(pendingUpdates);
        pendingUpdates.clear();
        
        // Mark widgets as processing (thread-safe set, no sync needed)
        processingWidgets.addAll(snapshot.keySet());
        
        // Process each update on background thread (slow rendering)
        executor.execute(() -> {
            for (PendingUpdate update : snapshot.values()) {
                try {
                    update.callback.onUpdate(
                        update.widgetId, 
                        update.width, 
                        update.height, 
                        update.options
                    );
                } catch (Exception e) {
                    android.util.Log.e("WidgetBatcher", "Failed to update widget " + update.widgetId, e);
                } finally {
                    // Remove from processing set (thread-safe)
                    processingWidgets.remove(update.widgetId);
                }
            }
        });
    }
    
    /**
     * Cancel any pending updates for a specific widget.
     * Useful when widget is being deleted.
     */
    public void cancelUpdate(int widgetId) {
        pendingUpdates.remove(widgetId);
        processingWidgets.remove(widgetId); // Thread-safe, no sync needed
    }
    
    /**
     * Force immediate processing of all pending updates.
     * Used for critical updates (e.g., midnight, app going to background).
     */
    public void flushAll() {
        if (batchRunnable != null) {
            mainHandler.removeCallbacks(batchRunnable);
        }
        processBatch();
    }
    
    /**
     * Get the number of pending updates.
     * Useful for debugging and telemetry.
     */
    public int getPendingCount() {
        return pendingUpdates.size();
    }
    
    /**
     * Cleanup resources to prevent memory leaks.
     * Call this on low memory or app termination.
     */
    public void cleanup() {
        if (batchRunnable != null) {
            mainHandler.removeCallbacks(batchRunnable);
            batchRunnable = null;
        }
        pendingUpdates.clear();
        processingWidgets.clear();
        executor.shutdown();
    }
}
