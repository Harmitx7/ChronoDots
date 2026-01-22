package com.dotmatrix.calendar.widget.provider;

import android.graphics.Bitmap;

import com.dotmatrix.calendar.R;
import com.dotmatrix.calendar.data.model.EmojiRule;
import com.dotmatrix.calendar.data.model.WidgetConfig;
import com.dotmatrix.calendar.data.model.WidgetType;

import java.time.LocalDate;
import java.util.List;

/**
 * Widget provider for Progress View widget.
 * Displays year/month/quarter/week progress as dot bar.
 */
public class ProgressWidgetProvider extends BaseWidgetProvider {

    @Override
    protected WidgetType getWidgetType() {
        return WidgetType.PROGRESS;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.widget_progress;
    }

    @Override
    protected Bitmap renderWidget(int width, int height, WidgetConfig config,
                                   List<EmojiRule> rules, LocalDate currentDate) {
        return renderer.renderProgressView(width, height, config, currentDate);
    }
}
