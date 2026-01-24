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

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        // Widget was resized, invalidate cache and re-render
        WidgetBitmapCache.getInstance().invalidate(appWidgetId);
        updateWidget(context, appWidgetManager, appWidgetId);
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
        executor.execute(() -> {
            try {
                // Get widget options (size)
                // Get widget options (size)
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
                
                // Ensure reasonable texture limits
                width = Math.max(100, Math.min(width, 2048));
                height = Math.max(100, Math.min(height, 2048));
                
                // Get or create config
                WidgetRepository repository = WidgetRepository.getInstance(context);
                WidgetConfig config = repository.getOrCreateConfig(widgetId, getWidgetType());
                
                // Resolve dynamic theme colors if needed
                resolveDynamicColors(context, config);
                
                // Get emoji rules
                List<EmojiRule> rules = repository.getEmojiRules(widgetId);
                
                // Render bitmap
                LocalDate today = LocalDate.now();
                Bitmap bitmap = renderWidget(width, height, config, rules, today);
                
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
    protected abstract Bitmap renderWidget(int width, int height, WidgetConfig config,
                                            List<EmojiRule> rules, LocalDate currentDate);

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
