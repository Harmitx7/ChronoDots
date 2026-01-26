package com.dotmatrix.calendar.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;

/**
 * Utility class for handling device-specific compatibility issues.
 * Provides workarounds for Samsung, Xiaomi, OnePlus, Huawei, and other OEM quirks.
 */
public class DeviceCompatHelper {
    
    // OEM Detection
    private static final String MANUFACTURER_SAMSUNG = "samsung";
    private static final String MANUFACTURER_XIAOMI = "xiaomi";
    private static final String MANUFACTURER_HUAWEI = "huawei";
    private static final String MANUFACTURER_ONEPLUS = "oneplus";
    private static final String MANUFACTURER_OPPO = "oppo";
    private static final String MANUFACTURER_VIVO = "vivo";
    private static final String MANUFACTURER_REALME = "realme";
    
    private final Context context;
    private final String manufacturer;
    private final boolean isLowRamDevice;
    
    public DeviceCompatHelper(Context context) {
        this.context = context.getApplicationContext();
        this.manufacturer = Build.MANUFACTURER.toLowerCase();
        this.isLowRamDevice = checkLowRamDevice();
    }
    
    /**
     * Checks if this is a low-RAM device (< 3GB).
     */
    private boolean checkLowRamDevice() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(memInfo);
            // Threshold: 3GB (3 * 1024 * 1024 * 1024 bytes)
            long threeGB = 3L * 1024L * 1024L * 1024L;
            return memInfo.totalMem < threeGB;
        }
        return false;
    }
    
    /**
     * Returns true if this is a low-RAM device.
     */
    public boolean isLowRamDevice() {
        return isLowRamDevice;
    }
    
    /**
     * Corrects widget dimensions based on OEM quirks.
     * Some manufacturers report incorrect dp values or apply non-standard scaling.
     * 
     * @param widthDp Reported width in dp
     * @param heightDp Reported height in dp
     * @param density Screen density
     * @return Corrected dimensions as int[] {width, height} in pixels
     */
    public int[] correctWidgetDimensions(int widthDp, int heightDp, float density) {
        int width = (int) (widthDp * density);
        int height = (int) (heightDp * density);
        
        // Samsung: Sometimes reports smaller dimensions during resize
        // Apply a minimum floor based on typical widget sizes
        if (isSamsung()) {
            width = Math.max(width, (int) (100 * density));
            height = Math.max(height, (int) (80 * density));
        }
        
        // Xiaomi/MIUI: Dense grid can result in very small widgets
        // Apply floor to prevent unreadable dots
        if (isXiaomi()) {
            width = Math.max(width, (int) (120 * density));
            height = Math.max(height, (int) (100 * density));
        }
        
        // OnePlus: OxygenOS sometimes doubles dp values on certain models
        // Check for suspiciously large values
        if (isOnePlus() && (width > 2000 || height > 2000)) {
            width = width / 2;
            height = height / 2;
        }
        
        // Universal safety clamps
        width = Math.max(100, Math.min(width, 2048));
        height = Math.max(100, Math.min(height, 2048));
        
        return new int[] { width, height };
    }
    
    /**
     * Returns the recommended bitmap quality config based on device capabilities.
     */
    public android.graphics.Bitmap.Config getRecommendedBitmapConfig() {
        if (isLowRamDevice) {
            return android.graphics.Bitmap.Config.RGB_565; // 2 bytes per pixel
        }
        return android.graphics.Bitmap.Config.ARGB_8888; // 4 bytes per pixel
    }
    
    /**
     * Returns the recommended blur radius based on device performance.
     * Low-RAM devices get reduced blur for faster rendering.
     */
    public float getRecommendedBlurRadius(float requestedRadius) {
        if (isLowRamDevice) {
            return Math.min(requestedRadius, 15f); // Cap at 15 for low-end
        }
        return requestedRadius;
    }
    
    /**
     * Returns whether hardware-accelerated blur (RenderEffect) should be used.
     * Only available on API 31+ and not recommended for low-RAM devices.
     */
    public boolean shouldUseHardwareBlur() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return false;
        }
        // Disable on low-RAM devices as RenderEffect can be memory-intensive
        return !isLowRamDevice;
    }
    
    /**
     * Returns the recommended noise texture size for glass effects.
     */
    public int getNoiseTextureSize() {
        if (isLowRamDevice) {
            return 32; // Smaller tile for low-RAM
        }
        return 64; // Standard tile
    }
    
    /**
     * Returns corner radius adjusted for device screen density and size.
     */
    public float getAdjustedCornerRadius(float baseDp) {
        float density = context.getResources().getDisplayMetrics().density;
        float radius = baseDp * density;
        
        // Samsung One UI has rounder corners, match their style
        if (isSamsung() && Build.VERSION.SDK_INT >= 31) {
            radius = Math.max(radius, 24f * density);
        }
        
        return radius;
    }
    
    /**
     * Returns true if glass/blur effects should be disabled.
     * Used on very low-end devices or problematic OEM combinations.
     */
    public boolean shouldDisableGlassEffects() {
        // Disable on devices with less than 2GB RAM
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(memInfo);
            long twoGB = 2L * 1024L * 1024L * 1024L;
            if (memInfo.totalMem < twoGB) {
                return true;
            }
        }
        
        // Also disable on certain problematic device/API combinations
        // Some Huawei devices have issues with BlurMaskFilter
        if (isHuawei() && Build.VERSION.SDK_INT < 28) {
            return true;
        }
        
        return false;
    }
    
    // OEM Detection Methods
    
    public boolean isSamsung() {
        return manufacturer.contains(MANUFACTURER_SAMSUNG);
    }
    
    public boolean isXiaomi() {
        return manufacturer.contains(MANUFACTURER_XIAOMI);
    }
    
    public boolean isHuawei() {
        return manufacturer.contains(MANUFACTURER_HUAWEI);
    }
    
    public boolean isOnePlus() {
        return manufacturer.contains(MANUFACTURER_ONEPLUS);
    }
    
    public boolean isOppo() {
        return manufacturer.contains(MANUFACTURER_OPPO);
    }
    
    public boolean isVivo() {
        return manufacturer.contains(MANUFACTURER_VIVO);
    }
    
    public boolean isRealme() {
        return manufacturer.contains(MANUFACTURER_REALME);
    }
    
    /**
     * Returns the device manufacturer name.
     */
    public String getManufacturer() {
        return Build.MANUFACTURER;
    }
    
    /**
     * Returns the device model name.
     */
    public String getModel() {
        return Build.MODEL;
    }
    
    /**
     * Returns a debug string with device info.
     */
    public String getDeviceInfoString() {
        return String.format(
            "Device: %s %s | Android %s (API %d) | RAM: %s | Density: %.1f",
            Build.MANUFACTURER,
            Build.MODEL,
            Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT,
            isLowRamDevice ? "Low" : "Normal",
            context.getResources().getDisplayMetrics().density
        );
    }
}
