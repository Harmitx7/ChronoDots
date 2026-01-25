package com.dotmatrix.calendar.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dotmatrix.calendar.widget.provider.MonthViewWidgetProvider;
import com.dotmatrix.calendar.widget.provider.WeekViewWidgetProvider;
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
        WeekViewWidgetProvider.updateAllWidgets(context, WeekViewWidgetProvider.class);
    }

    /**
     * Schedule an alarm for midnight to update widgets.
     */
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

        // Android 12+ (API 31) requires check for exact alarm permission
        // Android 14+ (API 34) restricts exact alarms by default
        boolean canScheduleExact = true;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            canScheduleExact = alarmManager.canScheduleExactAlarms();
        }

        try {
            if (canScheduleExact) {
                // Precise update at midnight
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC, // RTC prevents waking device from doze if not needed, but text says RTC_WAKEUP usually used for clocks
                        midnightMillis,
                        pendingIntent);
            } else {
                // Fallback to inexact if permission denied
                alarmManager.set(
                        AlarmManager.RTC,
                        midnightMillis,
                        pendingIntent);
            }
        } catch (SecurityException e) {
            // Permission revoked strictly at runtime, use fallback
            alarmManager.set(
                    AlarmManager.RTC,
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
