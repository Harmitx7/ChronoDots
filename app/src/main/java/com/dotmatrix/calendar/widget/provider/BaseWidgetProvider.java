package com.dotmatrix.calendar.widget.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.dotmatrix.calendar.R;
import com.dotmatrix.calendar.data.model.EmojiRule;
import com.dotmatrix.calendar.data.model.WidgetConfig;
import com.dotmatrix.calendar.data.model.WidgetType;
import com.dotmatrix.calendar.data.repository.WidgetRepository;
import com.dotmatrix.calendar.ui.editor.WidgetEditorActivity;
import com.dotmatrix.calendar.util.DynamicColorHelper;
import com.dotmatrix.calendar.widget.cache.WidgetBitmapCache;
import com.dotmatrix.calendar.widget.renderer.DotRenderer;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base class for widget providers with common functionality.
 */
public abstract class BaseWidgetProvider extends AppWidgetProvider {

    protected static final ExecutorService executor = Executors.newSingleThreadExecutor();
    protected static final DotRenderer renderer = new DotRenderer();
    
    // Note: Resize handling moved to WidgetResizeHandler for launcher-specific optimizations

    /**
     * Get the widget type for this provider.
     */
    protected abstract WidgetType getWidgetType();

    /**
     * Get the layout resource for this widget.
     */
    protected abstract int getLayoutResource();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
    }

    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        // Use the comprehensive WidgetResizeHandler for launcher-specific handling
        final Context appContext = context.getApplicationContext();
        final AppWidgetManager awm = appWidgetManager;
        
        com.dotmatrix.calendar.widget.resize.WidgetResizeHandler.getInstance()
            .handleResize(appContext, appWidgetId, newOptions, (widgetId, width, height, options) -> {
                // Widget was resized, invalidate cache and re-render
                WidgetBitmapCache.getInstance().invalidate(widgetId);
                updateWidget(appContext, awm, widgetId, options);
            });
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        WidgetRepository repository = WidgetRepository.getInstance(context);
        
        for (int widgetId : appWidgetIds) {
            // Delete config from database
            repository.deleteWidgetConfig(widgetId);
            // Clear bitmap cache
            WidgetBitmapCache.getInstance().invalidate(widgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // First widget of this type added
    }

    @Override
    public void onDisabled(Context context) {
        // Last widget of this type removed
    }

    /**
     * Update a single widget.
     */
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        updateWidget(context, appWidgetManager, widgetId, null);
    }

    /**
     * Update a single widget with specific options.
     */
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId, Bundle specificOptions) {
        final Context appContext = context.getApplicationContext();
        executor.execute(() -> {
            try {
                // Get widget options (size) - prioritize specific options if provided
                Bundle options = specificOptions;
                if (options == null) {
                    options = appWidgetManager.getAppWidgetOptions(widgetId);
                }
                int width = 200; // default
                int height = 200; // default

                if (options != null) {
                    int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
                    int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
                    
                    if (minWidth > 0 && minHeight > 0) {
                        // Use DeviceCompatHelper for OEM-specific dimension correction
                        com.dotmatrix.calendar.util.DeviceCompatHelper compatHelper = 
                            new com.dotmatrix.calendar.util.DeviceCompatHelper(appContext);
                        float density = appContext.getResources().getDisplayMetrics().density;
                        int[] correctedDims = compatHelper.correctWidgetDimensions(minWidth, minHeight, density);
                        width = correctedDims[0];
                        height = correctedDims[1];
                    }
                }
                
                // Universal safety clamps (also in compatHelper, but belt-and-suspenders)
                width = Math.max(100, Math.min(width, 2048));
                height = Math.max(100, Math.min(height, 2048));
                
                // Get or create config
                WidgetRepository repository = WidgetRepository.getInstance(context);
                WidgetConfig config = repository.getOrCreateConfig(widgetId, getWidgetType());
                
                // Resolve dynamic theme colors if needed
                resolveDynamicColors(context, config);
                
                // Generate cache key based on date and config
                LocalDate today = LocalDate.now();
                String cacheKey = WidgetBitmapCache.generateCacheKey(
                        widgetId, width, height, 
                        today.toString(), config.hashCode());
                
                // Check cache first to avoid unnecessary rendering
                Bitmap bitmap = WidgetBitmapCache.getInstance().get(widgetId, cacheKey);
                
                if (bitmap == null || bitmap.isRecycled()) {
                    // Cache miss - render new bitmap
                    List<EmojiRule> rules = repository.getEmojiRules(widgetId);
                    bitmap = renderWidget(appContext, width, height, config, rules, today);
                    
                    // Store in cache for future updates
                    if (bitmap != null) {
                        WidgetBitmapCache.getInstance().put(widgetId, cacheKey, bitmap);
                    }
                }
                
                // Create RemoteViews
                RemoteViews views = new RemoteViews(context.getPackageName(), getLayoutResource());
                views.setImageViewBitmap(R.id.widget_image, bitmap);
                
                // Set click handler to open editor
                Intent intent = new Intent(context, WidgetEditorActivity.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                intent.putExtra("widget_type", getWidgetType().name());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context, widgetId, intent, 
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
                
                // Update widget
                appWidgetManager.updateAppWidget(widgetId, views);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Render the widget bitmap based on type.
     */
    protected abstract Bitmap renderWidget(Context context, int width, int height, WidgetConfig config,
                                            List<EmojiRule> rules, LocalDate currentDate);

    /**
     * Update all widgets of this type.
     */
    /**
     * Update all widgets of this type.
     */
    public static void updateAllWidgets(Context context, Class<? extends BaseWidgetProvider> providerClass) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, providerClass);
        int[] widgetIds = appWidgetManager.getAppWidgetIds(componentName);
        
        if (widgetIds.length > 0) {
            Intent intent = new Intent(context, providerClass);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
            context.sendBroadcast(intent);
        }
    }

    /**
     * Force immediate update of a specific widget.
     * Compatible with all Android versions; bypasses broadcast delays.
     * This version accepts the config directly to avoid race conditions with database reads.
     */
    public static void forceUpdate(Context context, int widgetId, WidgetConfig config) {
        if (config == null) return;
        
        executor.execute(() -> {
            try {
                WidgetRepository repository = WidgetRepository.getInstance(context);
                
                // 2. Identify the correct Provider class and layout based on type
                // This mimics the abstract methods getWidgetType() / getLayoutResource()
                Class<?> providerClass;
                int layoutId;
                
                switch (config.getWidgetType()) {
                    case MONTH:
                        providerClass = MonthViewWidgetProvider.class;
                        layoutId = R.layout.widget_month;
                        break;
                    case WEEK:
                        providerClass = WeekViewWidgetProvider.class;
                        layoutId = R.layout.widget_week;
                        break;
                    case YEAR:
                    default:
                        providerClass = YearViewWidgetProvider.class;
                        layoutId = R.layout.widget_year;
                        break;
                }
                
                // 3. Perform the update logic manually (copied from updateWidget)
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
                int width = 200; // default
                int height = 200; // default

                if (options != null) {
                    int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
                    int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
                    
                    if (minWidth > 0 && minHeight > 0) {
                        float density = context.getResources().getDisplayMetrics().density;
                        width = (int) (minWidth * density);
                        height = (int) (minHeight * density);
                    }
                }
                
                width = Math.max(100, Math.min(width, 2048));
                height = Math.max(100, Math.min(height, 2048));
                
                // Resolve dynamic colors
                if ("dynamic_harmony".equals(config.getThemeId()) || "chameleon_pro".equals(config.getThemeId())) {
                    DynamicColorHelper helper = new DynamicColorHelper(context);
                    if ("dynamic_harmony".equals(config.getThemeId())) {
                         int[] colors = helper.getDynamicHarmonyColors();
                         config.setBackgroundColor(colors[0]);
                         config.setDotColor(colors[1]);
                         config.setAccentColor(colors[2]);
                    } else {
                         int[] colors = helper.getChameleonProColors();
                         config.setBackgroundColor(colors[0]);
                         config.setDotColor(colors[1]);
                         config.setAccentColor(colors[2]);
                    }
                }
                
                List<EmojiRule> rules = repository.getEmojiRules(widgetId);
                LocalDate today = LocalDate.now();
                
                // 4. Render using the renderer (we need to cast or use the helper)
                // Since this is static, we use the static renderer instance if available or create new
                DotRenderer updateRenderer = new DotRenderer();
                Bitmap bitmap = null;
                
                switch (config.getWidgetType()) {
                    case YEAR:
                        bitmap = updateRenderer.renderYearView(context, width, height, config, rules, today);
                        break;
                    case MONTH:
                        bitmap = updateRenderer.renderMonthView(context, width, height, config, rules, today);
                        break;
                    case WEEK:
                        bitmap = updateRenderer.renderWeekView(context, width, height, config, rules, today);
                        break;
                }
                
                if (bitmap != null) {
                    RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
                    views.setImageViewBitmap(R.id.widget_image, bitmap);
                    
                    Intent intent = new Intent(context, WidgetEditorActivity.class);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                    intent.putExtra("widget_type", config.getWidgetType().name());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            context, widgetId, intent, 
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
                    
                    appWidgetManager.updateAppWidget(widgetId, views);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Overload: Force update by reading config from database.
     * Used by midnight receivers and other non-editor callers.
     */
    public static void forceUpdate(Context context, int widgetId) {
        executor.execute(() -> {
            WidgetRepository repository = WidgetRepository.getInstance(context);
            WidgetConfig config = repository.getWidgetConfig(widgetId);
            if (config != null) {
                // Re-invoke with config loaded from DB
                // Since we're already on the executor, use this pattern
                forceUpdate(context.getApplicationContext(), widgetId, config);
            }
        });
    }

    /**
     * Resolve dynamic theme colors (Material You) for dynamic_harmony and chameleon_pro themes.
     * This replaces the 0x00000000 placeholder colors with actual system colors.
     */
    private void resolveDynamicColors(Context context, WidgetConfig config) {
        String themeId = config.getThemeId();
        
        if ("dynamic_harmony".equals(themeId)) {
            DynamicColorHelper helper = new DynamicColorHelper(context);
            int[] colors = helper.getDynamicHarmonyColors();
            config.setBackgroundColor(colors[0]);
            config.setDotColor(colors[1]);
            config.setAccentColor(colors[2]);
        } else if ("chameleon_pro".equals(themeId)) {
            DynamicColorHelper helper = new DynamicColorHelper(context);
            int[] colors = helper.getChameleonProColors();
            config.setBackgroundColor(colors[0]);
            config.setDotColor(colors[1]);
            config.setAccentColor(colors[2]);
        }
    }
}
