package com.dotmatrix.calendar.ui.main;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dotmatrix.calendar.R;
import com.dotmatrix.calendar.data.model.WidgetConfig;
import com.dotmatrix.calendar.databinding.ItemWidgetCardBinding;
import com.dotmatrix.calendar.widget.renderer.DotRenderer;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RecyclerView adapter for widget cards.
 */
public class WidgetAdapter extends RecyclerView.Adapter<WidgetAdapter.WidgetViewHolder> {

    private List<WidgetConfig> widgets;
    private final OnWidgetClickListener listener;
    private final DotRenderer renderer;
    private final ExecutorService executor;

    public interface OnWidgetClickListener {
        void onWidgetClick(WidgetConfig config);
        void onEditClick(WidgetConfig config);
        void onDeleteClick(WidgetConfig config);
    }

    public WidgetAdapter(List<WidgetConfig> widgets, OnWidgetClickListener listener) {
        this.widgets = widgets;
        this.listener = listener;
        this.renderer = new DotRenderer();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void updateData(List<WidgetConfig> newWidgets) {
        this.widgets = newWidgets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WidgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWidgetCardBinding binding = ItemWidgetCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WidgetViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WidgetViewHolder holder, int position) {
        WidgetConfig config = widgets.get(position);
        holder.bind(config);
    }

    @Override
    public int getItemCount() {
        return widgets.size();
    }

    class WidgetViewHolder extends RecyclerView.ViewHolder {
        private final ItemWidgetCardBinding binding;

        WidgetViewHolder(ItemWidgetCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(WidgetConfig config) {
            binding.widgetName.setText(config.getWidgetName());
            binding.widgetType.setText(config.getWidgetType().name() + " View");

            // Render preview
            executor.execute(() -> {
                Bitmap preview = renderPreview(config);
                binding.getRoot().post(() -> binding.widgetPreview.setImageBitmap(preview));
            });

            // Click handlers
            binding.getRoot().setOnClickListener(v -> listener.onWidgetClick(config));
            binding.btnEdit.setOnClickListener(v -> listener.onEditClick(config));
            binding.btnMore.setOnClickListener(v -> showPopupMenu(v, config));
        }

        private Bitmap renderPreview(WidgetConfig config) {
            int width = 300;
            int height = 200;
            LocalDate today = LocalDate.now();

            switch (config.getWidgetType()) {
                case YEAR:
                    return renderer.renderYearView(width, height, config, Collections.emptyList(), today);
                case MONTH:
                    return renderer.renderMonthView(width, height, config, Collections.emptyList(), today);
                case PROGRESS:
                    return renderer.renderProgressView(width, height, config, today);
                default:
                    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }
        }

        private void showPopupMenu(View anchor, WidgetConfig config) {
            PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
            popup.getMenu().add(0, 1, 0, R.string.edit);
            popup.getMenu().add(0, 2, 1, R.string.delete);

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    listener.onEditClick(config);
                    return true;
                } else if (item.getItemId() == 2) {
                    listener.onDeleteClick(config);
                    return true;
                }
                return false;
            });

            popup.show();
        }
    }
}
