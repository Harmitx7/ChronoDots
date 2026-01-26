package com.dotmatrix.calendar.widget.provider;

import android.graphics.Bitmap;

import com.dotmatrix.calendar.R;
import com.dotmatrix.calendar.data.model.EmojiRule;
import com.dotmatrix.calendar.data.model.WidgetConfig;
import com.dotmatrix.calendar.data.model.WidgetType;

import java.time.LocalDate;
import java.util.List;

/**
 * Widget provider for Week View widget.
 * Displays current week (7 days) as a strip of dots.
 */
public class WeekViewWidgetProvider extends BaseWidgetProvider {

    @Override
    protected WidgetType getWidgetType() {
        return WidgetType.WEEK;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.widget_week;
    }

    @Override
    protected Bitmap renderWidget(android.content.Context context, int width, int height, WidgetConfig config,
                                   List<EmojiRule> rules, LocalDate currentDate) {
        return renderer.renderWeekView(context, width, height, config, rules, currentDate);
    }
}
