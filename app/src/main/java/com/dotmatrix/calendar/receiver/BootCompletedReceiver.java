package com.dotmatrix.calendar.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dotmatrix.calendar.widget.provider.MonthViewWidgetProvider;
import com.dotmatrix.calendar.widget.provider.WeekViewWidgetProvider;
import com.dotmatrix.calendar.widget.provider.YearViewWidgetProvider;

/**
 * Receiver for boot completed events.
 * Reschedules midnight alarms and updates widgets after device reboot.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // Reschedule midnight update alarm
            MidnightUpdateReceiver.scheduleMidnightUpdate(context);
            
            // Update all widgets (date may have changed during shutdown)
            YearViewWidgetProvider.updateAllWidgets(context, YearViewWidgetProvider.class);
            MonthViewWidgetProvider.updateAllWidgets(context, MonthViewWidgetProvider.class);
            WeekViewWidgetProvider.updateAllWidgets(context, WeekViewWidgetProvider.class);
        }
    }
}
