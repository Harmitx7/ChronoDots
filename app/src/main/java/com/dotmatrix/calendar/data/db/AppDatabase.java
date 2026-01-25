package com.dotmatrix.calendar.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.dotmatrix.calendar.data.model.EmojiRule;
import com.dotmatrix.calendar.data.model.WidgetConfig;

/**
 * Room database for the Dot Matrix Calendar app.
 * Stores widget configurations and emoji rules.
 */
@Database(
    entities = {WidgetConfig.class, EmojiRule.class},
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "dot_matrix_database";
    private static volatile AppDatabase INSTANCE;

    public abstract WidgetConfigDao widgetConfigDao();
    public abstract EmojiRuleDao emojiRuleDao();

    /**
     * Get singleton instance of the database.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * For testing purposes - allows building an in-memory database.
     */
    public static AppDatabase getInMemoryInstance(Context context) {
        return Room.inMemoryDatabaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class
        ).build();
    }
}
