package com.dotmatrix.calendar.ui.editor;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
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
import com.dotmatrix.calendar.widget.renderer.DotRenderer;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;

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
    private List<EmojiRule> rules = Collections.emptyList();
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
        updateUI();
        updatePreview();
    }

    private void loadConfig(String typeString) {
        executor.execute(() -> {
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID && !isNew) {
                config = repository.getWidgetConfig(widgetId);
                rules = repository.getEmojiRules(widgetId);
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
                updateUI();
                updatePreview();
            });
        });
    }

    private void setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Save button
        binding.btnSave.setOnClickListener(v -> saveAndExit());

        // Widget type toggle
        binding.widgetTypeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && config != null) {
                if (checkedId == R.id.btn_type_year) {
                    config.setWidgetType(WidgetType.YEAR);
                } else if (checkedId == R.id.btn_type_month) {
                    config.setWidgetType(WidgetType.MONTH);
                } else if (checkedId == R.id.btn_type_progress) {
                    config.setWidgetType(WidgetType.PROGRESS);
                }
                updatePreview();
            }
        });

        // Theme button
        binding.btnTheme.setOnClickListener(v -> {
            Intent intent = new Intent(this, ThemeGalleryActivity.class);
            intent.putExtra("current_theme", config.getThemeId());
            startActivityForResult(intent, REQUEST_THEME);
        });

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
    }

    private void updateUI() {
        if (config == null) return;

        // Widget type
        switch (config.getWidgetType()) {
            case YEAR:
                binding.widgetTypeGroup.check(R.id.btn_type_year);
                break;
            case MONTH:
                binding.widgetTypeGroup.check(R.id.btn_type_month);
                break;
            case PROGRESS:
                binding.widgetTypeGroup.check(R.id.btn_type_progress);
                break;
        }

        // Theme
        ThemePreset theme = ThemePreset.findById(config.getThemeId());
        binding.btnTheme.setText(theme.getName());

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
    }

    private void updatePreview() {
        if (config == null) return;

        executor.execute(() -> {
            int width = 600;
            int height = 400;
            LocalDate today = LocalDate.now();

            Bitmap preview;
            switch (config.getWidgetType()) {
                case MONTH:
                    preview = renderer.renderMonthView(width, height, config, rules, today);
                    break;
                case PROGRESS:
                    preview = renderer.renderProgressView(width, height, config, today);
                    break;
                case YEAR:
                default:
                    preview = renderer.renderYearView(width, height, config, rules, today);
                    break;
            }

            runOnUiThread(() -> binding.previewImage.setImageBitmap(preview));
        });
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
                // This is an actual home screen widget, trigger update
                switch (config.getWidgetType()) {
                    case YEAR:
                        YearViewWidgetProvider.updateAllWidgets(this, YearViewWidgetProvider.class);
                        break;
                    case MONTH:
                        MonthViewWidgetProvider.updateAllWidgets(this, MonthViewWidgetProvider.class);
                        break;
                    case PROGRESS:
                        ProgressWidgetProvider.updateAllWidgets(this, ProgressWidgetProvider.class);
                        break;
                }

                // Return result for widget configuration
                Intent resultIntent = new Intent();
                resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, realWidgetId);
                setResult(RESULT_OK, resultIntent);
            } else {
                setResult(RESULT_OK);
            }

            runOnUiThread(this::finish);
        });
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
