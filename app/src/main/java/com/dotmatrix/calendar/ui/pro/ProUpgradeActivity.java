package com.dotmatrix.calendar.ui.pro;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dotmatrix.calendar.R;
import com.dotmatrix.calendar.data.preferences.AppPreferences;
import com.dotmatrix.calendar.databinding.ActivityProUpgradeBinding;

/**
 * Activity for Pro upgrade purchase.
 */
public class ProUpgradeActivity extends AppCompatActivity {

    private ActivityProUpgradeBinding binding;
    private AppPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProUpgradeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = AppPreferences.getInstance(this);

        setupListeners();
    }

    private void setupListeners() {
        binding.btnClose.setOnClickListener(v -> finish());

        binding.btnPurchase.setOnClickListener(v -> {
            // TODO: Integrate with Google Play Billing
            // For now, simulate a purchase
            simulatePurchase();
        });

        binding.btnRestore.setOnClickListener(v -> {
            // TODO: Restore purchase from Google Play
            Toast.makeText(this, "Checking for previous purchases...", Toast.LENGTH_SHORT).show();
        });
    }

    private void simulatePurchase() {
        // In production, this would use Google Play Billing Library
        // For development/testing, we'll simulate the unlock
        preferences.setProUnlocked(true);
        Toast.makeText(this, "Pro features unlocked!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
