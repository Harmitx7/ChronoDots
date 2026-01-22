package com.dotmatrix.calendar.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dotmatrix.calendar.widget.provider.MonthViewWidgetProvider;
import com.dotmatrix.calendar.widget.provider.ProgressWidgetProvider;
import com.dotmatrix.calendar.widget.provider.YearViewWidgetProvider;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Receiver for midnight updates and date changes.
 * Updates all widgets when the date changes.
 */
public class MidnightUpdateReceiver extends BroadcastReceiver {

    private static final int REQUEST_CODE_MIDNIGHT = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action == null) {
            // Manual trigger, update all widgets
            updateAllWidgets(context);
        } else if (action.equals(Intent.ACTION_DATE_CHANGED) ||
                   action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                   action.equals(Intent.ACTION_TIME_CHANGED)) {
            // System date/time change, update all widgets
            updateAllWidgets(context);
        }
        
        // Reschedule next midnight alarm
        scheduleMidnightUpdate(context);
    }

    /**
     * Update all widget types.
     */
    private void updateAllWidgets(Context context) {
        YearViewWidgetProvider.updateAllWidgets(context, YearViewWidgetProvider.class);
        MonthViewWidgetProvider.updateAllWidgets(context, MonthViewWidgetProvider.class);
        ProgressWidgetProvider.updateAllWidgets(context, ProgressWidgetProvider.class);
    }

    /**
     * Schedule an alarm for midnight to update widgets.
     */
    public static void scheduleMidnightUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, MidnightUpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_MIDNIGHT,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Calculate next midnight
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        long midnightMillis = midnight.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        // Schedule alarm
        try {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        midnightMillis,
                        pendingIntent);
            } else {
                // Fallback to inexact alarm
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        midnightMillis,
                        pendingIntent);
            }
        } catch (SecurityException e) {
            // Permission not granted, use inexact alarm
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    midnightMillis,
                    pendingIntent);
        }
    }

    /**
     * Cancel scheduled midnight update.
     */
    public static void cancelMidnightUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, MidnightUpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_MIDNIGHT,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);
    }
}
