package com.dotmatrix.calendar.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;

/**
 * Detects device power state and provides rendering quality recommendations.
 * Adapts widget rendering based on battery saver mode and available RAM.
 * 
 * iOS-inspired: Similar to iOS Low Power Mode optimizations.
 */
public class PowerModeDetector {
    
    private static final long LOW_RAM_THRESHOLD_MB = 2048; // 2GB
    private static final int LOW_BATTERY_THRESHOLD_PERCENT = 20;
    
    private final Context context;
    private final PowerManager powerManager;
    private final ActivityManager activityManager;
    
    public enum RenderingQuality {
        /** Full quality with glassmorphism and all effects */
        PREMIUM,
        /** Standard quality with reduced effects */
        BALANCED,
        /** High performance mode, minimal effects */
        PERFORMANCE
    }
    
    public PowerModeDetector(Context context) {
        this.context = context.getApplicationContext();
        this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }
    
    /**
     * Check if device is in battery save mode.
     */
    public boolean isBatterySaveMode() {
        if (powerManager == null) {
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return powerManager.isPowerSaveMode();
        }
        
        return false;
    }
    
    /**
     * Check if device has low available RAM.
     */
    public boolean isLowRam() {
        if (activityManager == null) {
            return false;
        }
        
        // Use system's built-in low RAM check (API 19+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return activityManager.isLowRamDevice();
        }
        
        // Fallback: Check available memory manually
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        long availableMB = memInfo.availMem / (1024 * 1024);
        
        return availableMB < LOW_RAM_THRESHOLD_MB;
    }
    
    /**
     * Get current battery level percentage (0-100).
     */
    public int getBatteryLevel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) 
                context.getSystemService(Context.BATTERY_SERVICE);
            
            if (batteryManager != null) {
                return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            }
        }
        
        // Fallback for older devices (less accurate)
        return 100;
    }
    
    /**
     * Check if battery is low.
     */
    public boolean isLowBattery() {
        return getBatteryLevel() < LOW_BATTERY_THRESHOLD_PERCENT;
    }
    
    /**
     * Get recommended rendering quality based on current power state.
     */
    public RenderingQuality getRecommendedQuality() {
        // Performance mode: Battery saver OR low RAM
        if (isBatterySaveMode() || isLowRam()) {
            return RenderingQuality.PERFORMANCE;
        }
        
        // Balanced mode: Low battery (but not in save mode yet)
        if (isLowBattery()) {
            return RenderingQuality.BALANCED;
        }
        
        // Premium mode: Normal conditions
        return RenderingQuality.PREMIUM;
    }
    
    /**
     * Check if glassmorphism effects should be enabled.
     * Glassmorphism is expensive; only enable in premium mode.
     */
    public boolean shouldEnableGlassmorphism() {
        return getRecommendedQuality() == RenderingQuality.PREMIUM;
    }
    
    /**
     * Check if animations should be enabled.
     */
    public boolean shouldEnableAnimations() {
        return getRecommendedQuality() != RenderingQuality.PERFORMANCE;
    }
    
    /**
     * Get cache size multiplier based on available resources.
     * Returns 1.0 for normal, 0.5 for low RAM, 1.5 for high RAM devices.
     */
    public float getCacheSizeMultiplier() {
        if (isLowRam()) {
            return 0.5f;
        }
        
        if (activityManager != null) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memInfo);
            long totalMB = memInfo.totalMem / (1024 * 1024);
            
            // High RAM device (8GB+)
            if (totalMB > 8192) {
                return 1.5f;
            }
        }
        
        return 1.0f;
    }
    
    /**
     * Get available memory in megabytes.
     */
    public long getAvailableMemoryMB() {
        if (activityManager != null) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memInfo);
            return memInfo.availMem / (1024 * 1024);
        }
        return 0;
    }
    
    /**
     * Get total memory in megabytes.
     */
    public long getTotalMemoryMB() {
        if (activityManager != null) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memInfo);
            return memInfo.totalMem / (1024 * 1024);
        }
        return 0;
    }
}
