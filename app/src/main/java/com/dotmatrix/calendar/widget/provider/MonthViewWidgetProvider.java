package com.dotmatrix.calendar.widget.provider;

import android.graphics.Bitmap;

import com.dotmatrix.calendar.R;
import com.dotmatrix.calendar.data.model.EmojiRule;
import com.dotmatrix.calendar.data.model.WidgetConfig;
import com.dotmatrix.calendar.data.model.WidgetType;

import java.time.LocalDate;
import java.util.List;

/**
 * Widget provider for Month View widget.
 * Displays current month with emoji support.
 */
public class MonthViewWidgetProvider extends BaseWidgetProvider {

    @Override
    protected WidgetType getWidgetType() {
        return WidgetType.MONTH;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.widget_month;
    }

    @Override
    protected Bitmap renderWidget(android.content.Context context, int width, int height, WidgetConfig config,
                                   List<EmojiRule> rules, LocalDate currentDate) {
        return renderer.renderMonthView(context, width, height, config, rules, currentDate);
    }
}
