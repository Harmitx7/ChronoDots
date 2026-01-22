package com.dotmatrix.calendar.data.db;

import androidx.room.TypeConverter;

import com.dotmatrix.calendar.data.model.DotShape;
import com.dotmatrix.calendar.data.model.ProgressMode;
import com.dotmatrix.calendar.data.model.ProgressStyle;
import com.dotmatrix.calendar.data.model.RuleType;
import com.dotmatrix.calendar.data.model.WidgetType;

/**
 * Type converters for Room database to store enum values.
 */
public class Converters {

    // WidgetType converters
    @TypeConverter
    public static WidgetType toWidgetType(String value) {
        return value == null ? null : WidgetType.valueOf(value);
    }

    @TypeConverter
    public static String fromWidgetType(WidgetType type) {
        return type == null ? null : type.name();
    }

    // DotShape converters
    @TypeConverter
    public static DotShape toDotShape(String value) {
        return value == null ? null : DotShape.valueOf(value);
    }

    @TypeConverter
    public static String fromDotShape(DotShape shape) {
        return shape == null ? null : shape.name();
    }

    // ProgressMode converters
    @TypeConverter
    public static ProgressMode toProgressMode(String value) {
        return value == null ? null : ProgressMode.valueOf(value);
    }

    @TypeConverter
    public static String fromProgressMode(ProgressMode mode) {
        return mode == null ? null : mode.name();
    }

    // ProgressStyle converters
    @TypeConverter
    public static ProgressStyle toProgressStyle(String value) {
        return value == null ? null : ProgressStyle.valueOf(value);
    }

    @TypeConverter
    public static String fromProgressStyle(ProgressStyle style) {
        return style == null ? null : style.name();
    }

    // RuleType converters
    @TypeConverter
    public static RuleType toRuleType(String value) {
        return value == null ? null : RuleType.valueOf(value);
    }

    @TypeConverter
    public static String fromRuleType(RuleType type) {
        return type == null ? null : type.name();
    }
}
