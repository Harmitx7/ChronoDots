package com.dotmatrix.calendar.widget.resize;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles widget resize events with launcher-specific optimizations.
 * Provides smooth, jank-free resizing across all Android devices and launchers.
 */
public class WidgetResizeHandler {
    
    private static final String TAG = "WidgetResizeHandler";
    
    // Singleton instance
    private static WidgetResizeHandler instance;
    
    // Handler for main thread operations
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Pending resize operations (widget ID -> Runnable)
    private final Map<Integer, Runnable> pendingResizes = new ConcurrentHashMap<>();
    
    // Latest resize options (widget ID -> Bundle)
    private final Map<Integer, Bundle> latestOptions = new ConcurrentHashMap<>();
    
    // Last resize dimensions (widget ID -> int[]{width, height})
    private final Map<Integer, int[]> lastDimensions = new ConcurrentHashMap<>();
    
    // Last resize timestamps (widget ID -> timestamp)
    private final Map<Integer, Long> lastResizeTime = new ConcurrentHashMap<>();
    
    // Adaptive throttle values
    private static final long MIN_THROTTLE_MS = 50;   // Fastest: 20 FPS
    private static final long MAX_THROTTLE_MS = 200;  // Slowest: 5 FPS
    private static final long DEFAULT_THROTTLE_MS = 80; // Default: 12.5 FPS
    
    // Dimension change threshold (in dp) to trigger re-render
    private static final int MIN_DIMENSION_CHANGE_DP = 8;
    
    private WidgetResizeHandler() {}
    
    public static synchronized WidgetResizeHandler getInstance() {
        if (instance == null) {
            instance = new WidgetResizeHandler();
        }
        return instance;
    }
    
    /**
     * Callback interface for resize completion.
     */
    public interface ResizeCallback {
        void onResize(int widgetId, int width, int height, Bundle options);
    }
    
    /**
     * Handles a widget resize event with smart throttling and deduplication.
     * 
     * @param context Application context
     * @param widgetId Widget ID being resized
     * @param newOptions New widget options from system
     * @param callback Callback to execute when resize should be applied
     */
    public void handleResize(Context context, int widgetId, Bundle newOptions, ResizeCallback callback) {
        if (newOptions == null || callback == null) {
            return;
        }
        
        final Context appContext = context.getApplicationContext();
        
        // Store latest options
        latestOptions.put(widgetId, newOptions);
        
        // Check if resize is significant enough to warrant re-render
        if (!isSignificantResize(appContext, widgetId, newOptions)) {
            Log.d(TAG, "Resize ignored: dimension change too small for widget " + widgetId);
            return;
        }
        
        // Cancel any pending resize for this widget
        Runnable existingTask = pendingResizes.get(widgetId);
        if (existingTask != null) {
            mainHandler.removeCallbacks(existingTask);
        }
        
        // Calculate adaptive throttle based on resize frequency
        long throttleMs = calculateAdaptiveThrottle(widgetId);
        
        // Create new resize task
        Runnable resizeTask = () -> {
            pendingResizes.remove(widgetId);
            
            // Get freshest options
            Bundle finalOptions = latestOptions.remove(widgetId);
            if (finalOptions == null) {
                finalOptions = newOptions;
            }
            
            // Calculate final dimensions with launcher quirk handling
            int[] dimensions = calculateDimensions(appContext, finalOptions);
            
            // Store for future comparison
            lastDimensions.put(widgetId, dimensions);
            lastResizeTime.put(widgetId, System.currentTimeMillis());
            
            // Execute callback
            callback.onResize(widgetId, dimensions[0], dimensions[1], finalOptions);
        };
        
        pendingResizes.put(widgetId, resizeTask);
        mainHandler.postDelayed(resizeTask, throttleMs);
    }
    
    /**
     * Checks if the resize is significant enough to warrant a re-render.
     */
    private boolean isSignificantResize(Context context, int widgetId, Bundle newOptions) {
        int[] lastDims = lastDimensions.get(widgetId);
        if (lastDims == null) {
            return true; // First resize, always significant
        }
        
        float density = context.getResources().getDisplayMetrics().density;
        int thresholdPx = (int) (MIN_DIMENSION_CHANGE_DP * density);
        
        int newWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0);
        int newHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
        
        int widthDiff = Math.abs((int)(newWidth * density) - lastDims[0]);
        int heightDiff = Math.abs((int)(newHeight * density) - lastDims[1]);
        
        return widthDiff >= thresholdPx || heightDiff >= thresholdPx;
    }
    
    /**
     * Calculates adaptive throttle based on resize frequency.
     * More frequent resizes = faster updates (smoother)
     * Sporadic resizes = slower updates (more efficient)
     */
    private long calculateAdaptiveThrottle(int widgetId) {
        Long lastTime = lastResizeTime.get(widgetId);
        if (lastTime == null) {
            return DEFAULT_THROTTLE_MS;
        }
        
        long timeSinceLastResize = System.currentTimeMillis() - lastTime;
        
        if (timeSinceLastResize < 100) {
            // User is actively dragging - use fastest throttle
            return MIN_THROTTLE_MS;
        } else if (timeSinceLastResize < 500) {
            // Recent resize - use medium throttle
            return DEFAULT_THROTTLE_MS;
        } else {
            // Sporadic resize - use slower throttle for efficiency
            return MAX_THROTTLE_MS;
        }
    }
    
    /**
     * Calculates widget dimensions with launcher-specific quirk handling.
     */
    private int[] calculateDimensions(Context context, Bundle options) {
        float density = context.getResources().getDisplayMetrics().density;
        
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
        int maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0);
        int maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0);
        
        // Use DeviceCompatHelper for OEM-specific corrections
        com.dotmatrix.calendar.util.DeviceCompatHelper compatHelper = 
            new com.dotmatrix.calendar.util.DeviceCompatHelper(context);
        
        int width, height;
        
        // Launcher-specific handling
        String launcher = detectLauncher(context);
        
        switch (launcher) {
            case "nova":
                // Nova Launcher: Use max dimensions when available (better for grid-snapping)
                width = maxWidth > 0 ? maxWidth : minWidth;
                height = maxHeight > 0 ? maxHeight : minHeight;
                break;
                
            case "oneui":
                // Samsung OneUI: Sometimes reports 0 for max dimensions
                // Also has unique handling for foldable devices
                width = minWidth > 0 ? minWidth : 200;
                height = minHeight > 0 ? minHeight : 200;
                
                // Check for foldable mode
                if (isSamsungFoldable(context)) {
                    // Foldables report different dimensions - use larger of the two
                    width = Math.max(minWidth, maxWidth);
                    height = Math.max(minHeight, maxHeight);
                }
                break;
                
            case "pixel":
                // Pixel Launcher: Well-behaved, use min dimensions
                width = minWidth;
                height = minHeight;
                break;
                
            case "miui":
                // MIUI Launcher: Can report very small values
                width = Math.max(minWidth, 100);
                height = Math.max(minHeight, 80);
                break;
                
            default:
                // Default: Use min dimensions
                width = minWidth > 0 ? minWidth : 200;
                height = minHeight > 0 ? minHeight : 200;
                break;
        }
        
        // Apply OEM-specific corrections
        int[] corrected = compatHelper.correctWidgetDimensions(width, height, density);
        
        return corrected;
    }
    
    /**
     * Detects the current launcher for launcher-specific handling.
     */
    private String detectLauncher(Context context) {
        try {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_MAIN);
            intent.addCategory(android.content.Intent.CATEGORY_HOME);
            android.content.pm.ResolveInfo resolveInfo = context.getPackageManager()
                .resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
            
            if (resolveInfo != null && resolveInfo.activityInfo != null) {
                String packageName = resolveInfo.activityInfo.packageName.toLowerCase();
                
                if (packageName.contains("nova")) {
                    return "nova";
                } else if (packageName.contains("sec.android.app.launcher") || 
                           packageName.contains("samsung")) {
                    return "oneui";
                } else if (packageName.contains("google.android.apps.nexuslauncher") ||
                           packageName.contains("pixel")) {
                    return "pixel";
                } else if (packageName.contains("miui") || packageName.contains("xiaomi")) {
                    return "miui";
                } else if (packageName.contains("oneplus")) {
                    return "oneplus";
                } else if (packageName.contains("huawei") || packageName.contains("emui")) {
                    return "emui";
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not detect launcher", e);
        }
        
        return "default";
    }
    
    /**
     * Checks if this is a Samsung foldable device.
     */
    private boolean isSamsungFoldable(Context context) {
        String model = Build.MODEL.toLowerCase();
        return Build.MANUFACTURER.toLowerCase().contains("samsung") &&
               (model.contains("fold") || model.contains("flip"));
    }
    
    /**
     * Clears all pending resizes for a widget (call on widget delete).
     */
    public void clearPendingResizes(int widgetId) {
        Runnable pending = pendingResizes.remove(widgetId);
        if (pending != null) {
            mainHandler.removeCallbacks(pending);
        }
        latestOptions.remove(widgetId);
        lastDimensions.remove(widgetId);
        lastResizeTime.remove(widgetId);
    }
    
    /**
     * Gets the last known dimensions for a widget.
     */
    public int[] getLastDimensions(int widgetId) {
        return lastDimensions.get(widgetId);
    }
    
    /**
     * Checks if there are pending resizes for a widget.
     */
    public boolean hasPendingResize(int widgetId) {
        return pendingResizes.containsKey(widgetId);
    }
}
