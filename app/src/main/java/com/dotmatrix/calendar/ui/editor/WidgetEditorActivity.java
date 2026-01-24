package com.dotmatrix.calendar.ui.editor;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.dotmatrix.calendar.R;
import com.dotmatrix.calendar.data.model.DotShape;
import com.dotmatrix.calendar.data.model.EmojiRule;
import com.dotmatrix.calendar.data.model.ThemePreset;
import com.dotmatrix.calendar.data.model.WidgetConfig;
import com.dotmatrix.calendar.data.model.WidgetType;
import com.dotmatrix.calendar.data.repository.WidgetRepository;
import com.dotmatrix.calendar.databinding.ActivityWidgetEditorBinding;
import com.dotmatrix.calendar.ui.theme.ThemeGalleryActivity;
import com.dotmatrix.calendar.widget.provider.MonthViewWidgetProvider;
import com.dotmatrix.calendar.widget.provider.ProgressWidgetProvider;
import com.dotmatrix.calendar.widget.provider.YearViewWidgetProvider;
import com.dotmatrix.calendar.widget.provider.BaseWidgetProvider;
import com.dotmatrix.calendar.widget.renderer.DotRenderer;
import android.widget.TextView;
import android.view.View;
import com.google.android.material.slider.Slider;
import com.dotmatrix.calendar.util.DynamicColorHelper;
import com.dotmatrix.calendar.ui.emoji.AddRuleSheet;
import com.dotmatrix.calendar.ui.emoji.EmojiRuleAdapter;
import java.util.ArrayList;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for configuring widget appearance.
 */
public class WidgetEditorActivity extends AppCompatActivity {

    private static final int REQUEST_THEME = 1001;

    private ActivityWidgetEditorBinding binding;
    private WidgetRepository repository;
    private DotRenderer renderer;
    private ExecutorService executor;

    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private WidgetConfig config;
    private List<EmojiRule> rules = new ArrayList<>();
    private EmojiRuleAdapter rulesAdapter;
    private boolean isNew = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWidgetEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = WidgetRepository.getInstance(this);
        renderer = new DotRenderer();
        executor = Executors.newSingleThreadExecutor();

        // Get widget ID from intent
        Intent intent = getIntent();
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                intent.getIntExtra("widget_id", AppWidgetManager.INVALID_APPWIDGET_ID));
        isNew = intent.getBooleanExtra("is_new", false);
        String typeString = intent.getStringExtra("widget_type");

        // Load or create config
        loadConfig(typeString);
        setupListeners();
        setupEmojiRules();
        updateUI();
        updatePreview();
    }

    private void setupEmojiRules() {
        rulesAdapter = new EmojiRuleAdapter(this::onDeleteRule);
        binding.rulesRecycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        binding.rulesRecycler.setAdapter(rulesAdapter);
    }

    private void loadConfig(String typeString) {
        executor.execute(() -> {
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID && !isNew) {
                config = repository.getWidgetConfig(widgetId);
                // Load rules for this widget
                rules = new ArrayList<>(repository.getEmojiRules(widgetId));
            }
            
            if (config == null) {
                WidgetType type = WidgetType.YEAR;
                if (typeString != null) {
                    try {
                        type = WidgetType.valueOf(typeString);
                    } catch (IllegalArgumentException e) {
                        // Use default
                    }
                }
                
                // Generate new widget ID for in-app widget
                if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                    widgetId = (int) System.currentTimeMillis();
                }
                
                config = WidgetConfig.createDefault(widgetId, type);
            }
            
            runOnUiThread(() -> {
                rulesAdapter.setRules(rules);
                binding.rulesCount.setText(rules.size() + " / ∞");
                updateUI();
                updatePreview();
            });
        });
    }

    private void setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finishWithAnimation());

        // Save button
        binding.btnSave.setOnClickListener(v -> saveAndExit());

        // Widget type segmented control
        View.OnClickListener segmentClickListener = v -> {
            if (config == null) return;
            
            // Update selection state
            binding.btnTypeYear.setBackgroundResource(R.drawable.bg_segment_item);
            binding.btnTypeMonth.setBackgroundResource(R.drawable.bg_segment_item);
            binding.btnTypeWeek.setBackgroundResource(R.drawable.bg_segment_item);
            v.setBackgroundResource(R.drawable.bg_segment_item_selected);
            
            // Update config
            if (v.getId() == R.id.btn_type_year) {
                config.setWidgetType(WidgetType.YEAR);
            } else if (v.getId() == R.id.btn_type_month) {
                config.setWidgetType(WidgetType.MONTH);
            } else if (v.getId() == R.id.btn_type_week) {
                config.setWidgetType(WidgetType.WEEK);
            }
            updatePreview();
        };
        
        binding.btnTypeYear.setOnClickListener(segmentClickListener);
        binding.btnTypeMonth.setOnClickListener(segmentClickListener);
        binding.btnTypeWeek.setOnClickListener(segmentClickListener);

        // Theme chips RecyclerView
        androidx.recyclerview.widget.LinearLayoutManager layoutManager =
                new androidx.recyclerview.widget.LinearLayoutManager(this,
                        androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false);
        binding.themesRecycler.setLayoutManager(layoutManager);

        ThemeChipAdapter themeAdapter = new ThemeChipAdapter(theme -> {
            if (config != null) {
                theme.applyTo(config);
                updatePreview();
            }
        });
        binding.themesRecycler.setAdapter(themeAdapter);

        // Set initial theme selection
        if (config != null) {
            themeAdapter.setSelectedTheme(config.getThemeId());
        }

        // Sliders
        binding.sliderDotSize.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && config != null) {
                config.setDotSize(value);
                updatePreview();
            }
        });

        binding.sliderDotSpacing.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && config != null) {
                config.setDotSpacing(value);
                updatePreview();
            }
        });

        binding.sliderOpacity.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && config != null) {
                config.setDotOpacity(value);
                updatePreview();
            }
        });

        // Shape radio buttons
        binding.shapeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (config != null) {
                if (checkedId == R.id.radio_circle) {
                    config.setDotShape(DotShape.CIRCLE);
                } else if (checkedId == R.id.radio_square) {
                    config.setDotShape(DotShape.SQUARE);
                } else if (checkedId == R.id.radio_rounded) {
                    config.setDotShape(DotShape.ROUNDED_SQUARE);
                }
                updatePreview();
            }
        });

        // Color picker click handlers
        binding.colorDotPreview.setOnClickListener(v -> showColorPicker("dot"));
        binding.colorAccentPreview.setOnClickListener(v -> showColorPicker("accent"));
        binding.colorBgPreview.setOnClickListener(v -> showColorPicker("background"));
        
        // Initialize color previews
        updateColorPreviews();
        
        // Add Rule Button
        binding.btnAddRule.setOnClickListener(v -> {
            AddRuleSheet sheet = new AddRuleSheet();
            sheet.setListener(this::onRuleCreated);
            sheet.show(getSupportFragmentManager(), "AddRuleSheet");
        });
    }

    private void onRuleCreated(EmojiRule rule) {
        if (config == null) return;
        
        executor.execute(() -> {
            rule.setWidgetId(widgetId);
            long id = repository.addEmojiRule(rule);
            rule.setId(id);
            rules.add(rule);
            
            runOnUiThread(() -> {
                rulesAdapter.setRules(rules);
                binding.rulesCount.setText(rules.size() + " / ∞");
                updatePreview();
            });
        });
    }

    private void onDeleteRule(EmojiRule rule) {
        executor.execute(() -> {
            repository.deleteEmojiRule(rule.getId());
            rules.remove(rule);
            
            runOnUiThread(() -> {
                rulesAdapter.setRules(rules);
                binding.rulesCount.setText(rules.size() + " / ∞");
                updatePreview();
            });
        });
    }

    private void updateColorPreviews() {
        if (config == null) return;
        
        android.graphics.drawable.GradientDrawable dotDrawable = new android.graphics.drawable.GradientDrawable();
        dotDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        dotDrawable.setColor(config.getDotColor());
        dotDrawable.setStroke(4, getResources().getColor(R.color.editor_button_stroke, null));
        binding.colorDotPreview.setBackground(dotDrawable);
        
        android.graphics.drawable.GradientDrawable accentDrawable = new android.graphics.drawable.GradientDrawable();
        accentDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        accentDrawable.setColor(config.getAccentColor());
        accentDrawable.setStroke(4, getResources().getColor(R.color.editor_button_stroke, null));
        binding.colorAccentPreview.setBackground(accentDrawable);
        
        android.graphics.drawable.GradientDrawable bgDrawable = new android.graphics.drawable.GradientDrawable();
        bgDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        bgDrawable.setColor(config.getBackgroundColor());
        bgDrawable.setStroke(4, getResources().getColor(R.color.editor_button_stroke, null));
        binding.colorBgPreview.setBackground(bgDrawable);
    }

    private void showColorPicker(String colorType) {
        if (config == null) return;
        
        // Preset color palette
        int[] colors = {
            0xFF000000, // Black
            0xFFFFFFFF, // White
            0xFFE8E8E8, // Light Gray
            0xFF1A1A1A, // Dark Gray
            0xFFFF6B6B, // Red
            0xFFFB6905, // Custom Orange
            0xFFFF9800, // Orange
            0xFFFFEB3B, // Yellow
            0xFF4CAF50, // Green
            0xFF2196F3, // Blue
            0xFF9C27B0, // Purple
            0xFFE91E63, // Pink
            0xFF00BCD4, // Cyan
            0xFFD4A574, // Gold
            0xFF6750A4, // Material Purple
            0xFF667EEA, // Ocean Blue
            0xFFFFDEE9, // Rose
        };
        
        // Create a dialog with color options
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Choose Color");
        
        // Create grid of color circles
        android.widget.GridLayout gridLayout = new android.widget.GridLayout(this);
        gridLayout.setColumnCount(4);
        gridLayout.setPadding(32, 32, 32, 32);
        
        // Create dialog first, then add click listeners that reference it
        android.app.AlertDialog dialog = builder.create();
        
        for (int color : colors) {
            android.widget.FrameLayout container = new android.widget.FrameLayout(this);
            android.widget.GridLayout.LayoutParams params = new android.widget.GridLayout.LayoutParams();
            params.width = (int) (56 * getResources().getDisplayMetrics().density);
            params.height = (int) (56 * getResources().getDisplayMetrics().density);
            params.setMargins(8, 8, 8, 8);
            container.setLayoutParams(params);
            
            android.view.View colorView = new android.view.View(this);
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            drawable.setColor(color);
            drawable.setStroke(4, 0x40FFFFFF);
            colorView.setBackground(drawable);
            colorView.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
            
            final int selectedColor = color;
            container.setOnClickListener(v -> {
                applySelectedColor(colorType, selectedColor);
                dialog.dismiss();
            });
            
            container.addView(colorView);
            gridLayout.addView(container);
        }
        
        dialog.setView(gridLayout);
        dialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "Cancel", (d, w) -> d.dismiss());
        dialog.show();
    }

    private void applySelectedColor(String colorType, int color) {
        if (config == null) return;
        
        switch (colorType) {
            case "dot":
                config.setDotColor(color);
                break;
            case "accent":
                config.setAccentColor(color);
                break;
            case "background":
                config.setBackgroundColor(color);
                break;
        }
        
        updateColorPreviews();
        updatePreview();
    }

    private void updateUI() {
        if (config == null) return;

        // Widget type - update segmented control
        binding.btnTypeYear.setBackgroundResource(R.drawable.bg_segment_item);
        binding.btnTypeMonth.setBackgroundResource(R.drawable.bg_segment_item);
        binding.btnTypeWeek.setBackgroundResource(R.drawable.bg_segment_item);
        
        switch (config.getWidgetType()) {
            case YEAR:
                binding.btnTypeYear.setBackgroundResource(R.drawable.bg_segment_item_selected);
                break;
            case MONTH:
                binding.btnTypeMonth.setBackgroundResource(R.drawable.bg_segment_item_selected);
                break;
            case WEEK:
                binding.btnTypeWeek.setBackgroundResource(R.drawable.bg_segment_item_selected);
                break;
        }

        // Theme is handled by RecyclerView adapter (themesRecycler)
        
        // Sliders
        binding.sliderDotSize.setValue(config.getDotSize());
        binding.sliderDotSpacing.setValue(config.getDotSpacing());
        binding.sliderOpacity.setValue(config.getDotOpacity());

        // Shape
        switch (config.getDotShape()) {
            case CIRCLE:
                binding.radioCircle.setChecked(true);
                break;
            case SQUARE:
                binding.radioSquare.setChecked(true);
                break;
            case ROUNDED_SQUARE:
                binding.radioRounded.setChecked(true);
                break;
        }

        // Rules count
        int ruleCount = rules.size();
        int limit = repository.isProUnlocked() ? Integer.MAX_VALUE : repository.getFreeEmojiRuleLimit();
        String limitText = repository.isProUnlocked() ? "∞" : String.valueOf(limit);
        binding.rulesCount.setText(ruleCount + " / " + limitText);
        
        // Update color previews
        updateColorPreviews();
    }

    private void updatePreview() {
        if (config == null) return;

        executor.execute(() -> {
            // Determine dimensions based on widget type
            float aspectRatio; // width / height
            int displayHeightDp;
            
            switch (config.getWidgetType()) {
                case MONTH:
                    aspectRatio = 1.0f; // Square
                    displayHeightDp = 300;
                    break;
                case WEEK:
                    aspectRatio = 4.0f; // Wide strip
                    displayHeightDp = 100;
                    break;
                case YEAR:
                default:
                    aspectRatio = 2.0f; // Rectangle (2:1 scale)
                    displayHeightDp = 200;
                    break;
            }
            
            // High resolution for rendering
            int renderWidth = 800;
            int renderHeight = (int) (renderWidth / aspectRatio);
            
            LocalDate today = LocalDate.now();
            Bitmap preview;
            
            // Resolve dynamic colors if needed
            if ("dynamic_harmony".equals(config.getThemeId()) || "chameleon_pro".equals(config.getThemeId())) {
                DynamicColorHelper helper = new DynamicColorHelper(this);
                if ("dynamic_harmony".equals(config.getThemeId())) {
                    int[] colors = helper.getDynamicHarmonyColors();
                    config.setBackgroundColor(colors[0]);
                    config.setDotColor(colors[1]);
                    config.setAccentColor(colors[2]);
                } else if ("chameleon_pro".equals(config.getThemeId())) {
                    int[] colors = helper.getChameleonProColors();
                    config.setBackgroundColor(colors[0]);
                    config.setDotColor(colors[1]);
                    config.setAccentColor(colors[2]);
                }
            }
            
            try {
                switch (config.getWidgetType()) {
                    case MONTH:
                        preview = renderer.renderMonthView(renderWidth, renderHeight, config, rules, today);
                        break;
                    case WEEK:
                        preview = renderer.renderWeekView(renderWidth, renderHeight, config, rules, today);
                        break;
                    case YEAR:
                    default:
                        preview = renderer.renderYearView(renderWidth, renderHeight, config, rules, today);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            runOnUiThread(() -> {
                // Update CardView height
                android.view.View card = (android.view.View) binding.previewImage.getParent();
                if (card != null) {
                    android.view.ViewGroup.LayoutParams params = card.getLayoutParams();
                    float density = getResources().getDisplayMetrics().density;
                    params.height = (int) (displayHeightDp * density);
                    card.setLayoutParams(params);
                }
                
                // Animate the preview update with crossfade and scale
                animatePreviewUpdate(preview);
            });
        });
    }

    /**
     * Animates the preview update with a smooth crossfade and scale effect.
     * This provides visual feedback when the user changes themes.
     */
    private void animatePreviewUpdate(android.graphics.Bitmap newPreview) {
        android.widget.ImageView previewImage = binding.previewImage;
        
        // If this is the first preview (no existing image), just fade in
        if (previewImage.getDrawable() == null) {
            previewImage.setAlpha(0f);
            previewImage.setImageBitmap(newPreview);
            previewImage.animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
            return;
        }
        
        // For subsequent updates, do a crossfade with scale animation
        // Phase 1: Fade out and scale down slightly
        previewImage.animate()
            .alpha(0.3f)
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(150)
            .setInterpolator(new android.view.animation.AccelerateInterpolator())
            .withEndAction(() -> {
                // Phase 2: Set new image, then fade in and scale up with bounce
                previewImage.setImageBitmap(newPreview);
                previewImage.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(250)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                    .start();
            })
            .start();
    }

    private void saveAndExit() {
        if (config == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        executor.execute(() -> {
            // Save to database
            repository.saveWidgetConfigSync(config);

            // Update widget if it's a real widget
            Intent intent = getIntent();
            int realWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            if (realWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // CRITICAL: Use application context since Activity may be destroyed
                Context appContext = getApplicationContext();
                
                // Get layout resource based on widget type
                int layoutId;
                switch (config.getWidgetType()) {
                    case MONTH:
                        layoutId = R.layout.widget_month;
                        break;
                    case WEEK:
                        layoutId = R.layout.widget_week;
                        break;
                    case PROGRESS:
                        layoutId = R.layout.widget_progress;
                        break;
                    case YEAR:
                    default:
                        layoutId = R.layout.widget_year;
                        break;
                }
                
                // Get widget dimensions
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(appContext);
                Bundle options = appWidgetManager.getAppWidgetOptions(realWidgetId);
                int width = 200;
                int height = 200;
                
                if (options != null) {
                    int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
                    int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
                    
                    if (minWidth > 0 && minHeight > 0) {
                        float density = getResources().getDisplayMetrics().density;
                        width = (int) (minWidth * density);
                        height = (int) (minHeight * density);
                    }
                }
                
                width = Math.max(100, Math.min(width, 2048));
                height = Math.max(100, Math.min(height, 2048));
                
                // Resolve dynamic colors if needed
                if ("dynamic_harmony".equals(config.getThemeId()) || "chameleon_pro".equals(config.getThemeId())) {
                    DynamicColorHelper helper = new DynamicColorHelper(appContext);
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
                
                // Render widget bitmap SYNCHRONOUSLY
                List<EmojiRule> widgetRules = repository.getEmojiRules(realWidgetId);
                java.time.LocalDate today = java.time.LocalDate.now();
                Bitmap bitmap = null;
                
                switch (config.getWidgetType()) {
                    case YEAR:
                        bitmap = renderer.renderYearView(width, height, config, widgetRules, today);
                        break;
                    case MONTH:
                        bitmap = renderer.renderMonthView(width, height, config, widgetRules, today);
                        break;
                    case WEEK:
                        bitmap = renderer.renderWeekView(width, height, config, widgetRules, today);
                        break;
                    case PROGRESS:
                        bitmap = renderer.renderProgressView(width, height, config, today);
                        break;
                }
                
                if (bitmap != null) {
                    android.widget.RemoteViews views = new android.widget.RemoteViews(appContext.getPackageName(), layoutId);
                    views.setImageViewBitmap(R.id.widget_image, bitmap);
                    
                    // Set click handler
                    Intent clickIntent = new Intent(appContext, WidgetEditorActivity.class);
                    clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, realWidgetId);
                    clickIntent.putExtra("widget_type", config.getWidgetType().name());
                    clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    
                    android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                            appContext, realWidgetId, clickIntent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
                    views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
                    
                    // UPDATE THE WIDGET
                    appWidgetManager.updateAppWidget(realWidgetId, views);
                    
                    android.util.Log.d("WidgetEditor", "Widget updated successfully - realWidgetId=" + realWidgetId);
                }

                // Return result for widget configuration
                Intent resultIntent = new Intent();
                resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, realWidgetId);
                setResult(RESULT_OK, resultIntent);
            } else {
                setResult(RESULT_OK);
            }

            runOnUiThread(this::finishWithAnimation);
        });
    }
    
    private void finishWithAnimation() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_THEME && resultCode == RESULT_OK && data != null) {
            String themeId = data.getStringExtra("theme_id");
            if (themeId != null && config != null) {
                ThemePreset theme = ThemePreset.findById(themeId);
                theme.applyTo(config);
                updateUI();
                updatePreview();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
