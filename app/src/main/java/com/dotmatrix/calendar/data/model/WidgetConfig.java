package com.dotmatrix.calendar.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a widget configuration.
 * Stores all customization options for a single widget instance.
 */
@Entity(tableName = "widget_configs")
public class WidgetConfig {

    @PrimaryKey
    private int widgetId;

    // Widget Type
    private WidgetType widgetType;

    // Theme
    private String themeId;

    // Appearance
    private float dotSize;        // 2.0 - 8.0 dp
    private float dotSpacing;     // 1.0 - 6.0 dp
    private float dotOpacity;     // 0.3 - 1.0
    private DotShape dotShape;
    private DotStyle dotStyle;    // FILLED, OUTLINE, etc.

    // Colors
    private int dotColor;
    private int backgroundColor;
    private int accentColor;
    private float backgroundOpacity;

    // Glassmorphism
    private boolean hasBlur;
    private float blurRadius;

    // Month View Specific
    private boolean showMonthLabel;
    private boolean showWeekHeaders;
    private int weekStartDay;     // 0 = Sunday, 1 = Monday

    // Progress View Specific
    private ProgressMode progressMode;
    private ProgressStyle progressStyle;

    // Metadata
    private long createdAt;
    private long lastUpdated;

    // Custom name for the widget
    private String widgetName;

    // Constructors
    public WidgetConfig() {
        // Defaults
        this.themeId = "classic_light";
        this.dotSize = 5.0f;
        this.dotSpacing = 3.0f;
        this.dotSpacing = 3.0f;
        this.dotOpacity = 0.9f;
        this.dotShape = DotShape.CIRCLE;
        this.dotStyle = DotStyle.FILLED;
        this.dotColor = 0xFFE0E0E0; // Off-white/Light Grey
        this.backgroundColor = 0xFF1A1625; // Deep Indigo
        this.accentColor = 0xFFD4A574; // Soft Gold
        this.backgroundOpacity = 1.0f;
        this.hasBlur = false;
        this.blurRadius = 20.0f;
        this.showMonthLabel = true;
        this.showWeekHeaders = true;
        this.weekStartDay = 0;
        this.progressMode = ProgressMode.YEAR;
        this.progressStyle = ProgressStyle.BAR;
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    // Static factory for creating default configs
    public static WidgetConfig createDefault(int widgetId, WidgetType type) {
        WidgetConfig config = new WidgetConfig();
        config.setWidgetId(widgetId);
        config.setWidgetType(type);
        config.setWidgetName(getDefaultName(type));
        return config;
    }

    private static String getDefaultName(WidgetType type) {
        switch (type) {
            case YEAR:
                return "Year View";
            case MONTH:
                return "Month View";
            case WEEK:
                return "Week View";
            case PROGRESS:
                return "Progress";
            default:
                return "Widget";
        }
    }

    // Getters and Setters
    public int getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(int widgetId) {
        this.widgetId = widgetId;
    }

    public WidgetType getWidgetType() {
        return widgetType;
    }

    public void setWidgetType(WidgetType widgetType) {
        this.widgetType = widgetType;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public float getDotSize() {
        return dotSize;
    }

    public void setDotSize(float dotSize) {
        this.dotSize = dotSize;
    }

    public float getDotSpacing() {
        return dotSpacing;
    }

    public void setDotSpacing(float dotSpacing) {
        this.dotSpacing = dotSpacing;
    }

    public float getDotOpacity() {
        return dotOpacity;
    }

    public void setDotOpacity(float dotOpacity) {
        this.dotOpacity = dotOpacity;
    }

    public DotShape getDotShape() {
        return dotShape;
    }

    public void setDotShape(DotShape dotShape) {
        this.dotShape = dotShape;
    }

    public DotStyle getDotStyle() {
        return dotStyle;
    }

    public void setDotStyle(DotStyle dotStyle) {
        this.dotStyle = dotStyle;
    }

    public int getDotColor() {
        return dotColor;
    }

    public void setDotColor(int dotColor) {
        this.dotColor = dotColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
    }

    public float getBackgroundOpacity() {
        return backgroundOpacity;
    }

    public void setBackgroundOpacity(float backgroundOpacity) {
        this.backgroundOpacity = backgroundOpacity;
    }

    public boolean isHasBlur() {
        return hasBlur;
    }

    public void setHasBlur(boolean hasBlur) {
        this.hasBlur = hasBlur;
    }

    public float getBlurRadius() {
        return blurRadius;
    }

    public void setBlurRadius(float blurRadius) {
        this.blurRadius = blurRadius;
    }

    public boolean isShowMonthLabel() {
        return showMonthLabel;
    }

    public void setShowMonthLabel(boolean showMonthLabel) {
        this.showMonthLabel = showMonthLabel;
    }

    public boolean isShowWeekHeaders() {
        return showWeekHeaders;
    }

    public void setShowWeekHeaders(boolean showWeekHeaders) {
        this.showWeekHeaders = showWeekHeaders;
    }

    public int getWeekStartDay() {
        return weekStartDay;
    }

    public void setWeekStartDay(int weekStartDay) {
        this.weekStartDay = weekStartDay;
    }

    public ProgressMode getProgressMode() {
        return progressMode;
    }

    public void setProgressMode(ProgressMode progressMode) {
        this.progressMode = progressMode;
    }

    public ProgressStyle getProgressStyle() {
        return progressStyle;
    }

    public void setProgressStyle(ProgressStyle progressStyle) {
        this.progressStyle = progressStyle;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getWidgetName() {
        return widgetName;
    }

    public void setWidgetName(String widgetName) {
        this.widgetName = widgetName;
    }

    /**
     * Mark config as updated with current timestamp.
     */
    public void markUpdated() {
        this.lastUpdated = System.currentTimeMillis();
    }
}
