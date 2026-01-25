package com.dotmatrix.calendar.data.repository;

import android.content.Context;

import com.dotmatrix.calendar.data.db.AppDatabase;
import com.dotmatrix.calendar.data.db.EmojiRuleDao;
import com.dotmatrix.calendar.data.db.WidgetConfigDao;
import com.dotmatrix.calendar.data.model.EmojiRule;
import com.dotmatrix.calendar.data.model.WidgetConfig;
import com.dotmatrix.calendar.data.model.WidgetType;
import com.dotmatrix.calendar.data.preferences.AppPreferences;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for widget data operations.
 * Provides a clean API for data access, abstracting the data sources.
 */
public class WidgetRepository {

    private static volatile WidgetRepository INSTANCE;

    private final WidgetConfigDao configDao;
    private final EmojiRuleDao emojiRuleDao;
    private final AppPreferences preferences;
    private final ExecutorService executor;

    // Free tier limits
    private static final int FREE_WIDGET_LIMIT = 1;
    private static final int FREE_EMOJI_RULE_LIMIT = 3;

    // Memory cache for rapid access (e.g. during resize)
    private final java.util.Map<Integer, WidgetConfig> configCache = new java.util.concurrent.ConcurrentHashMap<>();

    private WidgetRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.configDao = db.widgetConfigDao();
        this.emojiRuleDao = db.emojiRuleDao();
        this.preferences = AppPreferences.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Get singleton instance.
     */
    public static WidgetRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (WidgetRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WidgetRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    // ==================== Widget Config Operations ====================

    /**
     * Get widget configuration by ID.
     */
    public WidgetConfig getWidgetConfig(int widgetId) {
        if (configCache.containsKey(widgetId)) {
            return configCache.get(widgetId);
        }
        WidgetConfig config = configDao.getConfig(widgetId);
        if (config != null) configCache.put(widgetId, config);
        return config;
    }

    /**
     * Get all widget configurations.
     */
    public List<WidgetConfig> getAllWidgetConfigs() {
        return configDao.getAllConfigs();
    }

    /**
     * Save widget configuration.
     */
    /**
     * Save widget configuration.
     */
    public void saveWidgetConfig(WidgetConfig config) {
        config.markUpdated();
        configCache.put(config.getWidgetId(), config);
        executor.execute(() -> configDao.saveConfig(config));
    }

    /**
     * Save widget configuration synchronously.
     */
    public void saveWidgetConfigSync(WidgetConfig config) {
        config.markUpdated();
        configCache.put(config.getWidgetId(), config);
        configDao.saveConfig(config);
    }

    /**
     * Delete widget configuration.
     */
    public void deleteWidgetConfig(int widgetId) {
        configCache.remove(widgetId);
        executor.execute(() -> configDao.deleteById(widgetId));
    }

    /**
     * Get or create widget configuration.
     */
    public WidgetConfig getOrCreateConfig(int widgetId, WidgetType type) {
        // Check cache first
        if (configCache.containsKey(widgetId)) {
            return configCache.get(widgetId);
        }
        
        WidgetConfig config = configDao.getConfig(widgetId);
        if (config == null) {
            config = WidgetConfig.createDefault(widgetId, type);
            // Sync save
            configDao.saveConfig(config);
        }
        configCache.put(widgetId, config);
        return config;
    }

    /**
     * Get number of widgets.
     */
    public int getWidgetCount() {
        return configDao.getWidgetCount();
    }

    // ==================== Chameleon Mode Operations ====================

    public void setChameleonMode(int widgetId, boolean enabled) {
        executor.execute(() -> configDao.setChameleonMode(widgetId, enabled));
    }

    public void setChameleonIntensity(int widgetId, float intensity) {
        executor.execute(() -> configDao.setChameleonIntensity(widgetId, intensity));
    }

    public List<WidgetConfig> getWidgetsWithChameleonEnabled() {
        return configDao.getWidgetsWithChameleonEnabled();
    }

    // ==================== Emoji Rule Operations ====================

    /**
     * Get enabled emoji rules for a widget.
     */
    public List<EmojiRule> getEmojiRules(int widgetId) {
        return emojiRuleDao.getRulesForWidget(widgetId);
    }

    /**
     * Get all emoji rules for a widget (including disabled).
     */
    public List<EmojiRule> getAllEmojiRules(int widgetId) {
        return emojiRuleDao.getAllRulesForWidget(widgetId);
    }

    /**
     * Get emoji rule by ID.
     */
    public EmojiRule getEmojiRule(long ruleId) {
        return emojiRuleDao.getRuleById(ruleId);
    }

    /**
     * Add emoji rule.
     */
    public long addEmojiRule(EmojiRule rule) {
        return emojiRuleDao.insertRule(rule);
    }

    /**
     * Update emoji rule.
     */
    public void updateEmojiRule(EmojiRule rule) {
        executor.execute(() -> emojiRuleDao.updateRule(rule));
    }

    /**
     * Delete emoji rule (synchronous - caller handles threading).
     */
    public void deleteEmojiRule(long ruleId) {
        emojiRuleDao.deleteById(ruleId);
    }

    /**
     * Get emoji rule count for a widget.
     */
    public int getEmojiRuleCount(int widgetId) {
        return emojiRuleDao.getRuleCount(widgetId);
    }

    // ==================== Pro Features ====================

    /**
     * Check if user can add another widget.
     */
    public boolean canAddWidget() {
        if (preferences.isProUnlocked()) {
            return true;
        }
        return getWidgetCount() < FREE_WIDGET_LIMIT;
    }

    /**
     * Check if user can add another emoji rule.
     */
    public boolean canAddEmojiRule(int widgetId) {
        if (preferences.isProUnlocked()) {
            return true;
        }
        return getEmojiRuleCount(widgetId) < FREE_EMOJI_RULE_LIMIT;
    }

    /**
     * Get free widget limit.
     */
    public int getFreeWidgetLimit() {
        return FREE_WIDGET_LIMIT;
    }

    /**
     * Get free emoji rule limit.
     */
    public int getFreeEmojiRuleLimit() {
        return FREE_EMOJI_RULE_LIMIT;
    }

    /**
     * Check if Pro is unlocked.
     */
    public boolean isProUnlocked() {
        return preferences.isProUnlocked();
    }

    /**
     * Set Pro unlocked status.
     */
    public void setProUnlocked(boolean unlocked) {
        preferences.setProUnlocked(unlocked);
    }
}
