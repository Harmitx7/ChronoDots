package com.dotmatrix.calendar.ui.theme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dotmatrix.calendar.data.model.ThemePreset;
import com.dotmatrix.calendar.databinding.ItemThemeBinding;

/**
 * RecyclerView adapter for theme selection grid.
 */
public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder> {

    private final ThemePreset[] themes;
    private final String currentThemeId;
    private final boolean isProUnlocked;
    private final OnThemeSelectedListener listener;

    public interface OnThemeSelectedListener {
        void onThemeSelected(ThemePreset theme);
    }

    public ThemeAdapter(ThemePreset[] themes, String currentThemeId, 
                        boolean isProUnlocked, OnThemeSelectedListener listener) {
        this.themes = themes;
        this.currentThemeId = currentThemeId;
        this.isProUnlocked = isProUnlocked;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemThemeBinding binding = ItemThemeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ThemeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        ThemePreset theme = themes[position];
        holder.bind(theme);
    }

    @Override
    public int getItemCount() {
        return themes.length;
    }

    class ThemeViewHolder extends RecyclerView.ViewHolder {
        private final ItemThemeBinding binding;

        ThemeViewHolder(ItemThemeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ThemePreset theme) {
            // Set background color
            int backgroundColor = theme.getBackgroundColor();
            
            // Resolve dynamic colors for preview
            if ("dynamic_harmony".equals(theme.getId()) || "chameleon_pro".equals(theme.getId())) {
                com.dotmatrix.calendar.util.DynamicColorHelper helper = 
                    new com.dotmatrix.calendar.util.DynamicColorHelper(itemView.getContext());
                backgroundColor = helper.getBackgroundColor();
            }
            
            binding.themeBackground.setBackgroundColor(backgroundColor);
            
            // Set name
            binding.themeName.setText(theme.getName());
            
            // Pro badge
            if (theme.isPro() && !isProUnlocked) {
                binding.proBadge.setVisibility(View.VISIBLE);
            } else {
                binding.proBadge.setVisibility(View.GONE);
            }
            
            // Selected indicator
            if (theme.getId().equals(currentThemeId)) {
                binding.selectedIndicator.setVisibility(View.VISIBLE);
            } else {
                binding.selectedIndicator.setVisibility(View.GONE);
            }
            
            // Click listener
            binding.getRoot().setOnClickListener(v -> listener.onThemeSelected(theme));
        }
    }
}
