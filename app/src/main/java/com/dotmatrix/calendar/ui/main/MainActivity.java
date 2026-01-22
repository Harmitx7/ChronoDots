package com.dotmatrix.calendar.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dotmatrix.calendar.R;
import com.dotmatrix.calendar.data.model.WidgetConfig;
import com.dotmatrix.calendar.data.preferences.AppPreferences;
import com.dotmatrix.calendar.data.repository.WidgetRepository;
import com.dotmatrix.calendar.databinding.ActivityMainBinding;
import com.dotmatrix.calendar.ui.editor.WidgetEditorActivity;
import com.dotmatrix.calendar.ui.onboarding.OnboardingActivity;
import com.dotmatrix.calendar.ui.pro.ProUpgradeActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main activity displaying widget gallery.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private WidgetRepository repository;
    private AppPreferences preferences;
    private WidgetAdapter adapter;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = WidgetRepository.getInstance(this);
        preferences = AppPreferences.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        // Check for first launch
        if (preferences.isFirstLaunch() || !preferences.isOnboardingCompleted()) {
            startActivity(new Intent(this, OnboardingActivity.class));
        }

        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupFab();

        loadWidgets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWidgets();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.my_widgets));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.templates));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Handle tab selection
                if (tab.getPosition() == 0) {
                    loadWidgets();
                } else {
                    loadTemplates();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new WidgetAdapter(new ArrayList<>(), new WidgetAdapter.OnWidgetClickListener() {
            @Override
            public void onWidgetClick(WidgetConfig config) {
                openEditor(config.getWidgetId());
            }

            @Override
            public void onEditClick(WidgetConfig config) {
                openEditor(config.getWidgetId());
            }

            @Override
            public void onDeleteClick(WidgetConfig config) {
                showDeleteConfirmation(config);
            }
        });

        binding.widgetsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.widgetsRecycler.setAdapter(adapter);

        // Empty state button
        binding.btnAddFirst.setOnClickListener(v -> showAddWidgetDialog());
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            if (repository.canAddWidget()) {
                showAddWidgetDialog();
            } else {
                // Show Pro upgrade
                startActivity(new Intent(this, ProUpgradeActivity.class));
            }
        });
    }

    private void loadWidgets() {
        executor.execute(() -> {
            List<WidgetConfig> configs = repository.getAllWidgetConfigs();
            runOnUiThread(() -> {
                adapter.updateData(configs);
                updateEmptyState(configs.isEmpty());
            });
        });
    }

    private void loadTemplates() {
        // Show template widgets (static list for now)
        adapter.updateData(new ArrayList<>());
        updateEmptyState(true);
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.widgetsRecycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showAddWidgetDialog() {
        String[] options = {
            getString(R.string.widget_year_name),
            getString(R.string.widget_month_name),
            getString(R.string.widget_progress_name)
        };

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_widget)
            .setItems(options, (dialog, which) -> {
                String widgetType;
                switch (which) {
                    case 0:
                        widgetType = "YEAR";
                        break;
                    case 1:
                        widgetType = "MONTH";
                        break;
                    case 2:
                        widgetType = "PROGRESS";
                        break;
                    default:
                        widgetType = "YEAR";
                }
                openEditorForNewWidget(widgetType);
            })
            .show();
    }

    private void openEditorForNewWidget(String widgetType) {
        Intent intent = new Intent(this, WidgetEditorActivity.class);
        intent.putExtra("widget_type", widgetType);
        intent.putExtra("is_new", true);
        startActivity(intent);
    }

    private void openEditor(int widgetId) {
        Intent intent = new Intent(this, WidgetEditorActivity.class);
        intent.putExtra("widget_id", widgetId);
        startActivity(intent);
    }

    private void showDeleteConfirmation(WidgetConfig config) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_rule)
            .setMessage("Delete widget \"" + config.getWidgetName() + "\"?")
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                repository.deleteWidgetConfig(config.getWidgetId());
                loadWidgets();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_pro) {
            startActivity(new Intent(this, ProUpgradeActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
