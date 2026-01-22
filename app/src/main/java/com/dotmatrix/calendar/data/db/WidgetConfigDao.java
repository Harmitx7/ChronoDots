package com.dotmatrix.calendar.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dotmatrix.calendar.data.model.WidgetConfig;

import java.util.List;

/**
 * Data Access Object for widget configurations.
 */
@Dao
public interface WidgetConfigDao {

    @Query("SELECT * FROM widget_configs WHERE widgetId = :widgetId")
    WidgetConfig getConfig(int widgetId);

    @Query("SELECT * FROM widget_configs")
    List<WidgetConfig> getAllConfigs();

    @Query("SELECT COUNT(*) FROM widget_configs")
    int getWidgetCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveConfig(WidgetConfig config);

    @Update
    void updateConfig(WidgetConfig config);

    @Delete
    void deleteConfig(WidgetConfig config);

    @Query("DELETE FROM widget_configs WHERE widgetId = :widgetId")
    void deleteById(int widgetId);

    @Query("DELETE FROM widget_configs")
    void deleteAll();
}
