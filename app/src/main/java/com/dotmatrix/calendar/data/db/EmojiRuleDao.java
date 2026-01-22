package com.dotmatrix.calendar.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.dotmatrix.calendar.data.model.EmojiRule;

import java.util.List;

/**
 * Data Access Object for emoji rules.
 */
@Dao
public interface EmojiRuleDao {

    @Query("SELECT * FROM emoji_rules WHERE widgetId = :widgetId AND enabled = 1 ORDER BY priority DESC")
    List<EmojiRule> getRulesForWidget(int widgetId);

    @Query("SELECT * FROM emoji_rules WHERE widgetId = :widgetId ORDER BY priority DESC")
    List<EmojiRule> getAllRulesForWidget(int widgetId);

    @Query("SELECT * FROM emoji_rules WHERE id = :id")
    EmojiRule getRuleById(long id);

    @Query("SELECT COUNT(*) FROM emoji_rules WHERE widgetId = :widgetId")
    int getRuleCount(int widgetId);

    @Insert
    long insertRule(EmojiRule rule);

    @Update
    void updateRule(EmojiRule rule);

    @Delete
    void deleteRule(EmojiRule rule);

    @Query("DELETE FROM emoji_rules WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM emoji_rules WHERE widgetId = :widgetId")
    void deleteAllForWidget(int widgetId);

    @Query("UPDATE emoji_rules SET priority = :priority WHERE id = :id")
    void updatePriority(long id, int priority);

    @Query("UPDATE emoji_rules SET enabled = :enabled WHERE id = :id")
    void setEnabled(long id, boolean enabled);
}
