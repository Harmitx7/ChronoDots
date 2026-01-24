package com.dotmatrix.calendar.ui.emoji;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dotmatrix.calendar.databinding.ActivityEmojiRulesBinding;

/**
 * Activity for managing emoji rules.
 * Placeholder - to be fully implemented.
 */
public class EmojiRuleActivity extends AppCompatActivity {

    private ActivityEmojiRulesBinding binding;
    private EmojiRuleAdapter adapter;
    private final java.util.List<com.dotmatrix.calendar.data.model.EmojiRule> rules = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmojiRulesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupRecyclerView();
        setupListeners();
    }

    private void setupRecyclerView() {
        adapter = new EmojiRuleAdapter(this::onDeleteRule);
        binding.rulesRecycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        binding.rulesRecycler.setAdapter(adapter);
        
        // Populate with fake data if empty (for demo) or load from DB
        if (rules.isEmpty()) {
            // Optional: Add dummy rule to show it works
            // rules.add(com.dotmatrix.calendar.data.model.EmojiRule.createSpecificDate(0, "🎂", System.currentTimeMillis(), "Example Birthday"));
        }
        adapter.setRules(rules);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.fabAddRule.setOnClickListener(v -> {
            AddRuleSheet sheet = new AddRuleSheet();
            sheet.setListener(this::onRuleCreated);
            sheet.show(getSupportFragmentManager(), "AddRuleSheet");
        });
    }

    private void onRuleCreated(com.dotmatrix.calendar.data.model.EmojiRule rule) {
        rules.add(rule);
        adapter.setRules(rules);
        // TODO: Save to database
    }

    private void onDeleteRule(com.dotmatrix.calendar.data.model.EmojiRule rule) {
        rules.remove(rule);
        adapter.setRules(rules);
        // TODO: Delete from database
    }
}
