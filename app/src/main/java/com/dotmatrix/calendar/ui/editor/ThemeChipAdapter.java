package com.dotmatrix.calendar.ui.editor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dotmatrix.calendar.R;
import com.dotmatrix.calendar.data.model.ThemePreset;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for horizontal theme chip selector.
 */
public class ThemeChipAdapter extends RecyclerView.Adapter<ThemeChipAdapter.ThemeViewHolder> {

    private final List<ThemePreset> themes = new ArrayList<>();
    private int selectedPosition = 0;
    private OnThemeSelectedListener listener;

    public interface OnThemeSelectedListener {
        void onThemeSelected(ThemePreset theme);
    }

    public ThemeChipAdapter(OnThemeSelectedListener listener) {
        this.listener = listener;
        // Load all themes
        for (ThemePreset theme : ThemePreset.ALL_THEMES) {
            themes.add(theme);
        }
    }

    public void setSelectedTheme(String themeId) {
        for (int i = 0; i < themes.size(); i++) {
            if (themes.get(i).getId().equals(themeId)) {
                int oldPosition = selectedPosition;
                selectedPosition = i;
                notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theme_chip, parent, false);
        return new ThemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        ThemePreset theme = themes.get(position);
        holder.bind(theme, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return themes.size();
    }

    class ThemeViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView nameText;

        ThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.theme_chip);
            nameText = itemView.findViewById(R.id.theme_name);
        }

        void bind(ThemePreset theme, boolean isSelected) {
            nameText.setText(theme.getName());

            // Update selection state with new editor colors
            if (isSelected) {
                card.setCardBackgroundColor(itemView.getContext().getColor(R.color.editor_chip_bg_selected));
                card.setStrokeColor(itemView.getContext().getColor(R.color.editor_accent));
                card.setStrokeWidth(4);
                nameText.setTextColor(itemView.getContext().getColor(R.color.editor_accent));
            } else {
                card.setCardBackgroundColor(itemView.getContext().getColor(R.color.editor_chip_bg));
                card.setStrokeColor(itemView.getContext().getColor(R.color.editor_chip_stroke));
                card.setStrokeWidth((int) (1.5f * itemView.getContext().getResources().getDisplayMetrics().density));
                nameText.setTextColor(itemView.getContext().getColor(R.color.text_primary));
            }

            // Click handler
            card.setOnClickListener(v -> {
                int oldPosition = selectedPosition;
                selectedPosition = getAdapterPosition();
                notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    listener.onThemeSelected(theme);
                }
            });
        }
    }
}
