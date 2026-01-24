package com.dotmatrix.calendar.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences wrapper for app-level settings.
 * Stores user preferences that are not widget-specific.
 */
public class AppPreferences {

    private static final String PREFS_NAME = "app_prefs";

    // Keys
    private static final String KEY_DEFAULT_THEME = "default_theme";
    private static final String KEY_AUTO_DARK_MODE = "auto_dark_mode";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_PRO_UNLOCKED = "pro_unlocked";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String KEY_EXACT_ALARM_REQUESTED = "exact_alarm_requested";

    private final SharedPreferences prefs;

    private static volatile AppPreferences INSTANCE;

    private AppPreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get singleton instance.
     */
    public static AppPreferences getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppPreferences.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppPreferences(context);
                }
            }
        }
        return INSTANCE;
    }

    // Default Theme
    public String getDefaultTheme() {
        return prefs.getString(KEY_DEFAULT_THEME, "classic_light");
    }

    public void setDefaultTheme(String themeId) {
        prefs.edit().putString(KEY_DEFAULT_THEME, themeId).apply();
    }

    // Auto Dark Mode
    public boolean isAutoDarkMode() {
        return prefs.getBoolean(KEY_AUTO_DARK_MODE, true);
    }

    public void setAutoDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_DARK_MODE, enabled).apply();
    }

    // First Launch
    public boolean isFirstLaunch() {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunchCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }

    // Pro Unlocked
    public boolean isProUnlocked() {
        return true; // Bypassed for USER request
    }

    public void setProUnlocked(boolean unlocked) {
        prefs.edit().putBoolean(KEY_PRO_UNLOCKED, unlocked).apply();
    }

    // Onboarding Completed
    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    public void setOnboardingCompleted(boolean completed) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply();
    }

    // Exact Alarm Permission Requested
    public boolean isExactAlarmRequested() {
        return prefs.getBoolean(KEY_EXACT_ALARM_REQUESTED, false);
    }

    public void setExactAlarmRequested(boolean requested) {
        prefs.edit().putBoolean(KEY_EXACT_ALARM_REQUESTED, requested).apply();
    }

    /**
     * Clear all preferences (for testing or reset).
     */
    public void clear() {
        prefs.edit().clear().apply();
    }
}
