package com.dotmatrix.calendar.widget.provider;

import android.graphics.Bitmap;

import com.dotmatrix.calendar.R;
import com.dotmatrix.calendar.data.model.EmojiRule;
import com.dotmatrix.calendar.data.model.WidgetConfig;
import com.dotmatrix.calendar.data.model.WidgetType;

import java.time.LocalDate;
import java.util.List;

/**
 * Widget provider for Year View widget.
 * Displays entire year in dot matrix format.
 */
public class YearViewWidgetProvider extends BaseWidgetProvider {

    @Override
    protected WidgetType getWidgetType() {
        return WidgetType.YEAR;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.widget_year;
    }

    @Override
    protected Bitmap renderWidget(android.content.Context context, int width, int height, WidgetConfig config,
                                   List<EmojiRule> rules, LocalDate currentDate) {
        return renderer.renderYearView(context, width, height, config, rules, currentDate);
    }
}
