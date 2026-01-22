package com.dotmatrix.calendar.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room entity representing an emoji rule for dates.
 * Allows users to assign emojis to specific dates or patterns.
 */
@Entity(
    tableName = "emoji_rules",
    foreignKeys = @ForeignKey(
        entity = WidgetConfig.class,
        parentColumns = "widgetId",
        childColumns = "widgetId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index(value = "widgetId")
)
public class EmojiRule {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private int widgetId;

    // The emoji character(s) to display
    private String emoji;

    // Rule type determines how to match dates
    private RuleType ruleType;

    // For SPECIFIC_DATE and DATE_RANGE
    // Stored as epoch milliseconds
    private Long startDate;
    private Long endDate;

    // For RECURRING_DATE (e.g., every 15th of the month)
    private Integer dayOfMonth;     // 1-31
    private Integer monthOfYear;    // 1-12, null = every month

    // For RECURRING_DAY (e.g., every Monday)
    // Comma-separated: "1,3,5" for Mon/Wed/Fri
    private String daysOfWeek;

    // Priority for rule ordering (higher = checked first)
    private int priority;

    // Whether rule is active
    private boolean enabled;

    // Optional label for the rule
    private String label;

    // Metadata
    private long createdAt;

    // Constructor
    public EmojiRule() {
        this.enabled = true;
        this.priority = 0;
        this.createdAt = System.currentTimeMillis();
    }

    // Factory methods for different rule types
    public static EmojiRule createSpecificDate(int widgetId, String emoji, long date, String label) {
        EmojiRule rule = new EmojiRule();
        rule.setWidgetId(widgetId);
        rule.setEmoji(emoji);
        rule.setRuleType(RuleType.SPECIFIC_DATE);
        rule.setStartDate(date);
        rule.setLabel(label);
        return rule;
    }

    public static EmojiRule createRecurringDay(int widgetId, String emoji, String daysOfWeek, String label) {
        EmojiRule rule = new EmojiRule();
        rule.setWidgetId(widgetId);
        rule.setEmoji(emoji);
        rule.setRuleType(RuleType.RECURRING_DAY);
        rule.setDaysOfWeek(daysOfWeek);
        rule.setLabel(label);
        return rule;
    }

    public static EmojiRule createRecurringDate(int widgetId, String emoji, int dayOfMonth, Integer monthOfYear, String label) {
        EmojiRule rule = new EmojiRule();
        rule.setWidgetId(widgetId);
        rule.setEmoji(emoji);
        rule.setRuleType(RuleType.RECURRING_DATE);
        rule.setDayOfMonth(dayOfMonth);
        rule.setMonthOfYear(monthOfYear);
        rule.setLabel(label);
        return rule;
    }

    public static EmojiRule createDateRange(int widgetId, String emoji, long startDate, long endDate, String label) {
        EmojiRule rule = new EmojiRule();
        rule.setWidgetId(widgetId);
        rule.setEmoji(emoji);
        rule.setRuleType(RuleType.DATE_RANGE);
        rule.setStartDate(startDate);
        rule.setEndDate(endDate);
        rule.setLabel(label);
        return rule;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(int widgetId) {
        this.widgetId = widgetId;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public Integer getMonthOfYear() {
        return monthOfYear;
    }

    public void setMonthOfYear(Integer monthOfYear) {
        this.monthOfYear = monthOfYear;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get a human-readable description of the rule.
     */
    public String getDescription() {
        switch (ruleType) {
            case SPECIFIC_DATE:
                return "Specific date";
            case RECURRING_DAY:
                return "Every " + formatDaysOfWeek();
            case RECURRING_DATE:
                if (monthOfYear != null) {
                    return "Every " + getMonthName(monthOfYear) + " " + dayOfMonth;
                }
                return "Every " + ordinal(dayOfMonth) + " of the month";
            case DATE_RANGE:
                return "Date range";
            default:
                return "Unknown rule";
        }
    }

    private String formatDaysOfWeek() {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return "";
        }
        String[] days = daysOfWeek.split(",");
        StringBuilder sb = new StringBuilder();
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < days.length; i++) {
            int dayNum = Integer.parseInt(days[i].trim());
            if (dayNum >= 0 && dayNum < 7) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(dayNames[dayNum]);
            }
        }
        return sb.toString();
    }

    private String getMonthName(int month) {
        String[] months = {"", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return (month >= 1 && month <= 12) ? months[month] : "";
    }

    private String ordinal(int n) {
        if (n >= 11 && n <= 13) {
            return n + "th";
        }
        switch (n % 10) {
            case 1: return n + "st";
            case 2: return n + "nd";
            case 3: return n + "rd";
            default: return n + "th";
        }
    }
}
