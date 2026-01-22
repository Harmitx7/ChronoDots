package com.dotmatrix.calendar.ui.theme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.dotmatrix.calendar.data.model.ThemePreset;
import com.dotmatrix.calendar.data.preferences.AppPreferences;
import com.dotmatrix.calendar.databinding.ActivityThemeGalleryBinding;
import com.dotmatrix.calendar.ui.pro.ProUpgradeActivity;

/**
 * Activity for browsing and selecting themes.
 */
public class ThemeGalleryActivity extends AppCompatActivity {

    private ActivityThemeGalleryBinding binding;
    private String currentThemeId;
    private AppPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThemeGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = AppPreferences.getInstance(this);
        currentThemeId = getIntent().getStringExtra("current_theme");
        if (currentThemeId == null) {
            currentThemeId = "classic_light";
        }

        setupToolbar();
        setupRecyclerView();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        ThemeAdapter adapter = new ThemeAdapter(ThemePreset.ALL_THEMES, currentThemeId, 
            preferences.isProUnlocked(), theme -> {
                // Check if Pro is required
                if (theme.isPro() && !preferences.isProUnlocked()) {
                    startActivity(new Intent(this, ProUpgradeActivity.class));
                } else {
                    // Return selected theme
                    Intent result = new Intent();
                    result.putExtra("theme_id", theme.getId());
                    setResult(RESULT_OK, result);
                    finish();
                }
            });

        binding.themesRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        binding.themesRecycler.setAdapter(adapter);
    }
}
