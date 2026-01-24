package com.dotmatrix.calendar.widget.renderer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.dotmatrix.calendar.data.model.DotShape;
import com.dotmatrix.calendar.data.model.DotStyle;
import com.dotmatrix.calendar.data.model.EmojiRule;
import com.dotmatrix.calendar.data.model.ProgressMode;
import com.dotmatrix.calendar.data.model.RuleType;
import com.dotmatrix.calendar.data.model.WidgetConfig;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.util.List;

/**
 * Core renderer that creates bitmaps for all widget types.
 * Uses Canvas drawing for efficient rendering.
 */
public class DotRenderer {

    private final Paint dotPaint;
    private final Paint accentPaint;
    private final Paint pastDotPaint;
    private final Paint futureDotPaint;
    private final Paint textPaint;
    private final Paint emojiPaint;
    private final RectF rectF;

    public DotRenderer() {
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pastDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        futureDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT);
        
        emojiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emojiPaint.setTextAlign(Paint.Align.CENTER);
        
        rectF = new RectF();
    }

    /**
     * Draw background with support for glassmorphism.
     */
    private void drawBackground(Canvas canvas, int width, int height, WidgetConfig config) {
        float cornerRadius = 48f; // Larger corners for minimal aesthetic
        rectF.set(0, 0, width, height);

        if (config.isHasBlur()) {
            // Glassmorphism Effect
            
            // 1. Fill (Semi-transparent)
            Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            fillPaint.setColor(applyOpacity(config.getBackgroundColor(), config.getBackgroundOpacity()));
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, fillPaint);

            // 2. Border/Stroke (Gradient for Shine)
            Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(2f); // Thin border
            
            // Create a diagonal gradient for the border (Top-Left White to Bottom-Right Transparent)
            // Using int[] colors and float[] positions
            int startColor = Color.argb(100, 255, 255, 255); // White with alpha
            int endColor = Color.argb(20, 255, 255, 255);   // Faint white
            
            android.graphics.Shader gradient = new android.graphics.LinearGradient(
                0, 0, width, height,
                new int[]{startColor, endColor},
                null,
                android.graphics.Shader.TileMode.CLAMP
            );
            borderPaint.setShader(gradient);
            
            // Inset rect slightly for border so it doesn't clip
            RectF borderRect = new RectF(1f, 1f, width - 1f, height - 1f);
            canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint);

        } else {
            // Standard Solid Background
            Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(applyOpacity(config.getBackgroundColor(), config.getBackgroundOpacity()));
            
            // If opacity is 1.0 (opaque), we might want to fill the whole bitmap
            // removing corner radius if the widget container handles clipping.
            // But usually nice to have the bitmap itself rounded or fill the rect.
            // Let's assume the ImageView/Layout handles clipping or we draw a rect.
            // For now, draw full rect for solid bg
             canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, bgPaint);
        }
    }

    /**
     * Render Year View widget.
     */
    public Bitmap renderYearView(int width, int height, WidgetConfig config, 
                                  List<EmojiRule> rules, LocalDate currentDate) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Draw background
        drawBackground(canvas, width, height, config);
        
        // Setup paints
        setupPaints(config);
        
        // Calculate layout with margins for the glass container
        // Reduce padding for cleaner minimal look
        float padding = config.isHasBlur() ? 20f : 12f;
        
        float density = 2.5f; // Approximate density
        float dotSizePx = config.getDotSize() * density;
        float spacingPx = config.getDotSpacing() * density;
        
        // Adjust available space for dots
        // Initialize year variable first
        int year = currentDate.getYear();

        // Calculate footer text space
        String yearText = String.valueOf(year);
        // Calculate days left
        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(currentDate, LocalDate.of(year, 12, 31));
        String daysLeftText = daysLeft + " days left";

        float footerHeight = displayMetricsAwareTextSize(24f); // Approx 24dp for footer area
        
        // Adjust text paint for footer
        textPaint.setColor(config.getDotColor());
        textPaint.setTextSize(displayMetricsAwareTextSize(14f)); // 14sp - slightly larger for readability
        textPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL)); // Geometric/Medium style
        textPaint.setTextAlign(Paint.Align.LEFT);

        // Adjust available space for dots to leave room for footer
        int availableWidth = (int) (width - padding * 2);
        int availableHeight = (int) (height - padding * 2 - footerHeight);
        
        DotLayout layout = DotLayout.forYearView(availableWidth, availableHeight, dotSizePx, spacingPx);
        
        // Offset layout start by padding
        float offsetX = padding + layout.getStartX();
        float offsetY = padding + layout.getStartY(); // Dots centered in availableHeight (top portion)
        
        // Draw Footer Text (bottom aligned like reference)
        float footerY = height - padding - 8f; // Closer to bottom
        canvas.drawText(yearText, padding + 12f, footerY, textPaint);
        
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(daysLeftText, width - padding - 12f, footerY, textPaint);

        int yearVal = currentDate.getYear();
        int currentDayOfYear = currentDate.getDayOfYear();
        
        // Draw dots for each month and day
        for (int month = 1; month <= 12; month++) {
            YearMonth ym = YearMonth.of(yearVal, month);
            int daysInMonth = ym.lengthOfMonth();
            
            for (int day = 1; day <= 31; day++) {
                int row = month - 1;
                int col = day - 1;
                
                // Use offset coordinates (centered in top area)
                float cx = layout.getDotCenterX(col) + padding; 
                float cy = layout.getDotCenterY(row) + padding;
                float radius = layout.getDotSize() / 2;
                
                if (day > daysInMonth) {
                    futureDotPaint.setAlpha(30);
                    // Force FILLED for background/future dots to keep noise low
                    drawDot(canvas, cx, cy, radius, config.getDotShape(), futureDotPaint, DotStyle.FILLED);
                    continue;
                }
                
                LocalDate date = LocalDate.of(yearVal, month, day);
                int dayOfYear = date.getDayOfYear();
                
                String emoji = findEmoji(date, rules);
                if (emoji != null) {
                    drawEmoji(canvas, cx, cy, radius, emoji);
                    continue;
                }
                
                Paint paint;
                DotStyle style = config.getDotStyle(); // Default to user config
                
                if (dayOfYear == currentDayOfYear) {
                    paint = accentPaint;
                    radius *= 1.2f;
                    style = DotStyle.GLOW; // Always glow/highlight today
                } else if (dayOfYear < currentDayOfYear) {
                    paint = dotPaint;
                } else {
                    paint = futureDotPaint;
                    // Future dots often look good as Outline if user chose Outline, or Filled otherwise
                }
                
                drawDot(canvas, cx, cy, radius, config.getDotShape(), paint, style);
            }
        }
        
        return bitmap;
    }
    
    // Helper for approximate pixel conversion (assuming density 2.5 usually, but really should be passed)
    // For now using constant density assumption from existing code which was 2.5f
    private float displayMetricsAwareTextSize(float sp) {
        return sp * 2.5f; 
    }


    /**
     * Render Month View widget.
     */
    public Bitmap renderMonthView(int width, int height, WidgetConfig config,
                                   List<EmojiRule> rules, LocalDate currentDate) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Draw background
        drawBackground(canvas, width, height, config);
        
        // Setup paints
        setupPaints(config);
        
        float padding = config.isHasBlur() ? 20f : 12f;
        
        // Scale up for Month View (fewer dots, so they should be bigger)
        float baseScale = 7.0f; 
        float density = 2.5f * baseScale;
        float dotSizePx = config.getDotSize() * density;
        float spacingPx = config.getDotSpacing() * density;
        
        int availableWidth = (int) (width - padding * 2);
        int availableHeight = (int) (height - padding * 2);
        
        boolean hasHeader = config.isShowWeekHeaders() || config.isShowMonthLabel();
        DotLayout layout = DotLayout.forMonthView(availableWidth, availableHeight, dotSizePx, spacingPx, hasHeader);
        
        YearMonth ym = YearMonth.of(currentDate.getYear(), currentDate.getMonthValue());
        int daysInMonth = ym.lengthOfMonth();
        
        LocalDate firstDay = ym.atDay(1);
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue();
        
        if (config.getWeekStartDay() == 0) {
            firstDayOfWeek = firstDayOfWeek % 7; 
        } else {
            firstDayOfWeek = firstDayOfWeek - 1;
        }
        
        int startRow = hasHeader ? 1 : 0;
        
        if (config.isShowWeekHeaders()) {
            String[] headers;
            if (config.getWeekStartDay() == 0) {
                headers = new String[]{"S", "M", "T", "W", "T", "F", "S"};
            } else {
                headers = new String[]{"M", "T", "W", "T", "F", "S", "S"};
            }
            
            textPaint.setTextSize(layout.getDotSize() * 0.8f);
            textPaint.setColor(applyOpacity(config.getDotColor(), 0.5f));
            
            for (int col = 0; col < 7; col++) {
                float cx = layout.getDotCenterX(col) + padding;
                float cy = layout.getDotCenterY(0) + padding;
                canvas.drawText(headers[col], cx, cy + layout.getDotSize() / 4, textPaint);
            }
        }
        
        int currentDay = 1;
        int row = startRow;
        int col = firstDayOfWeek;
        
        while (currentDay <= daysInMonth) {
            float cx = layout.getDotCenterX(col) + padding;
            float cy = layout.getDotCenterY(row) + padding;
            float radius = layout.getDotSize() / 2;
            
            LocalDate date = ym.atDay(currentDay);
            
            String emoji = findEmoji(date, rules);
            if (emoji != null) {
                drawEmoji(canvas, cx, cy, radius, emoji);
            } else {
                Paint paint;
                DotStyle style = config.getDotStyle();

                if (currentDay == currentDate.getDayOfMonth() && 
                    currentDate.getMonthValue() == ym.getMonthValue()) {
                    paint = accentPaint;
                    radius *= 1.2f;
                    style = DotStyle.GLOW;
                } else if (date.isBefore(currentDate)) {
                    paint = dotPaint;
                } else {
                    paint = futureDotPaint;
                }
                
                drawDot(canvas, cx, cy, radius, config.getDotShape(), paint, style);
            }
            
            col++;
            if (col >= 7) {
                col = 0;
                row++;
            }
            currentDay++;
        }
        
        return bitmap;
    }

    /**
     * Render Progress View widget.
     */
    public Bitmap renderProgressView(int width, int height, WidgetConfig config,
                                      LocalDate currentDate) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Draw background
        drawBackground(canvas, width, height, config);
        
        // Setup paints
        setupPaints(config);
        
        float padding = config.isHasBlur() ? 16f : 8f;
        int availableWidth = (int) (width - padding * 2);
        int availableHeight = (int) (height - padding * 2);

        int completed;
        int total;
        
        switch (config.getProgressMode()) {
            case MONTH:
                completed = currentDate.getDayOfMonth();
                total = currentDate.lengthOfMonth();
                break;
            case QUARTER:
                int quarter = (currentDate.getMonthValue() - 1) / 3;
                LocalDate quarterStart = LocalDate.of(currentDate.getYear(), quarter * 3 + 1, 1);
                LocalDate quarterEnd = quarterStart.plusMonths(3).minusDays(1);
                completed = (int) (currentDate.toEpochDay() - quarterStart.toEpochDay()) + 1;
                total = (int) (quarterEnd.toEpochDay() - quarterStart.toEpochDay()) + 1;
                break;
            case WEEK:
                completed = currentDate.getDayOfWeek().getValue();
                total = 7;
                break;
            case YEAR:
            default:
                completed = currentDate.getDayOfYear();
                total = currentDate.lengthOfYear();
                break;
        }
        
        // Max 52 dots for visibility (usually 1 row)
        // If width is constrained, DotLayout handles it?
        // Actually for progress bar we might want more dots if horizontal space allows
        // But 52 is weeks in year, good default.
        int dotCount = Math.min(total, 52); 
        float ratio = (float) completed / total;
        int filledDots = Math.round(ratio * dotCount);
        
        float density = 2.5f;
        float dotSizePx = config.getDotSize() * density;
        float spacingPx = config.getDotSpacing() * density;
        
        DotLayout layout = DotLayout.forProgressView(availableWidth, availableHeight, dotSizePx, spacingPx, dotCount);
        
        for (int i = 0; i < dotCount; i++) {
            float cx = layout.getDotCenterX(i) + padding;
            float cy = layout.getDotCenterY(0) + padding;
            float radius = layout.getDotSize() / 2;
            
            Paint paint = (i < filledDots) ? dotPaint : futureDotPaint;
            DotStyle style = config.getDotStyle();
            
            if (i == filledDots - 1 || i == filledDots) {
                paint = accentPaint;
                style = DotStyle.GLOW;
            }
            
            drawDot(canvas, cx, cy, radius, config.getDotShape(), paint, style);
        }
        
        if (width > height * 2) {
            int percentage = Math.round(ratio * 100);
            String percentText = percentage + "%";
            
            textPaint.setTextSize(layout.getDotSize() * 2);
            textPaint.setColor(config.getDotColor());
            
            float textX = width - padding - layout.getDotSize() * 2;
            float textY = height / 2f + layout.getDotSize() / 2;
            canvas.drawText(percentText, textX, textY, textPaint);
        }
        
        return bitmap;
    }

    /**
     * Render Week View widget.
     */
    public Bitmap renderWeekView(int width, int height, WidgetConfig config,
                                  List<EmojiRule> rules, LocalDate currentDate) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Draw background
        drawBackground(canvas, width, height, config);
        
        // Setup paints
        setupPaints(config);
        
        float padding = config.isHasBlur() ? 16f : 8f;
        
        // Scale up for Week View (fewer dots/cols)
        float baseScale = 7.0f;
         float density = 2.5f * baseScale;
        float dotSizePx = config.getDotSize() * density;
        float spacingPx = config.getDotSpacing() * density;
        
        int availableWidth = (int) (width - padding * 2);
        int availableHeight = (int) (height - padding * 2);
        
        boolean hasHeader = config.isShowWeekHeaders();
        // Use WeekView layout
        DotLayout layout = DotLayout.forWeekView(availableWidth, availableHeight, dotSizePx, spacingPx, hasHeader);
        
        // Calculate start of week
        LocalDate startOfWeek = currentDate;
        int currentDoW = currentDate.getDayOfWeek().getValue(); // 1=Mon...7=Sun
        int offset = 0;
        
        if (config.getWeekStartDay() == 0) { // Sunday Start
            offset = currentDoW % 7;
        } else { // Monday Start
            offset = currentDoW - 1;
        }
        startOfWeek = currentDate.minusDays(offset);
        
        // Draw Headers if enabled
        if (hasHeader) {
            String[] headers;
            if (config.getWeekStartDay() == 0) {
                 headers = new String[]{"S", "M", "T", "W", "T", "F", "S"};
            } else {
                 headers = new String[]{"M", "T", "W", "T", "F", "S", "S"};
            }
            
            textPaint.setTextSize(layout.getDotSize() * 0.6f);
            textPaint.setColor(applyOpacity(config.getDotColor(), 0.7f));
            
            for (int col = 0; col < 7; col++) {
                float cx = layout.getDotCenterX(col) + padding;
                float cy = layout.getDotCenterY(0) + padding;
                canvas.drawText(headers[col], cx, cy + layout.getDotSize() / 4, textPaint);
            }
        }
        
        // Draw Dots
        int row = hasHeader ? 1 : 0;
        for (int col = 0; col < 7; col++) {
             LocalDate date = startOfWeek.plusDays(col);
             
             float cx = layout.getDotCenterX(col) + padding;
             float cy = layout.getDotCenterY(row) + padding;
             float radius = layout.getDotSize() / 2;
             
             String emoji = findEmoji(date, rules);
             if (emoji != null) {
                 drawEmoji(canvas, cx, cy, radius, emoji);
             } else {
                 Paint paint;
                 DotStyle style = config.getDotStyle();
                 
                 if (date.isEqual(currentDate)) {
                     paint = accentPaint;
                     radius *= 1.2f;
                     style = DotStyle.GLOW;
                 } else if (date.isBefore(currentDate)) {
                     paint = dotPaint;
                 } else {
                     paint = futureDotPaint;
                 }
                 drawDot(canvas, cx, cy, radius, config.getDotShape(), paint, style);
             }
        }
        
        return bitmap;
    }
    private void setupPaints(WidgetConfig config) {
        int dotColor = config.getDotColor();
        int accentColor = config.getAccentColor();
        float opacity = config.getDotOpacity();
        
        dotPaint.setColor(applyOpacity(dotColor, opacity));
        accentPaint.setColor(accentColor);
        pastDotPaint.setColor(applyOpacity(dotColor, opacity * 0.7f));
        futureDotPaint.setColor(applyOpacity(dotColor, opacity * 0.3f));
    }

    /**
     * Draw a single dot with the specified shape and style.
     */
    private void drawDot(Canvas canvas, float cx, float cy, float radius, 
                         DotShape shape, Paint paint, DotStyle style) {
        
        // Prepare paint for style
        configurePaintForStyle(paint, style, radius);

        switch (shape) {
            case SQUARE:
                rectF.set(cx - radius, cy - radius, cx + radius, cy + radius);
                canvas.drawRect(rectF, paint);
                break;
            case ROUNDED_SQUARE:
                rectF.set(cx - radius, cy - radius, cx + radius, cy + radius);
                canvas.drawRoundRect(rectF, radius * 0.3f, radius * 0.3f, paint);
                break;
            case CIRCLE:
            default:
                canvas.drawCircle(cx, cy, radius, paint);
                break;
        }
        
        // Restore paint style for next usage
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0);
        paint.setShadowLayer(0, 0, 0, 0);
    }
    
    private void configurePaintForStyle(Paint paint, DotStyle style, float radius) {
        switch (style) {
            case OUTLINE:
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Math.max(2f, radius * 0.2f)); // Proportional stroke
                break;
            case RING:
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Math.max(1f, radius * 0.15f)); 
                // Draw logic might need to be inside drawDot for multi-ring, 
                // but simpler Ring style is just a thin stroke circle.
                break;
            case GLOW:
                paint.setStyle(Paint.Style.FILL);
                // paint.setShadowLayer(radius, 0, 0, paint.getColor()); // Glow effect
                // Note: setShadowLayer is expensive on hardware acceleration, 
                // but bitmaps are software rendered so it's fine.
                // However, shadow color usually needs separate alpha.
                paint.setShadowLayer(radius * 0.8f, 0, 0, paint.getColor());
                break;
            case FILLED:
            default:
                paint.setStyle(Paint.Style.FILL);
                break;
        }
    }

    /**
     * Draw an emoji at the specified position.
     */
    private void drawEmoji(Canvas canvas, float cx, float cy, float radius, String emoji) {
        emojiPaint.setTextSize(radius * 2);
        canvas.drawText(emoji, cx, cy + radius * 0.4f, emojiPaint);
    }

    /**
     * Apply opacity to a color.
     */
    private int applyOpacity(int color, float opacity) {
        int alpha = Math.round(Color.alpha(color) * opacity);
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Find matching emoji for a date from rules.
     */
    private String findEmoji(LocalDate date, List<EmojiRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        
        for (EmojiRule rule : rules) {
            if (!rule.isEnabled()) continue;
            
            if (matchesRule(date, rule)) {
                return rule.getEmoji();
            }
        }
        
        return null;
    }

    /**
     * Check if a date matches a rule.
     */
    private boolean matchesRule(LocalDate date, EmojiRule rule) {
        switch (rule.getRuleType()) {
            case SPECIFIC_DATE:
                if (rule.getStartDate() != null) {
                    LocalDate ruleDate = LocalDate.ofEpochDay(rule.getStartDate() / 86400000L);
                    return date.equals(ruleDate);
                }
                break;
                
            case RECURRING_DAY:
                String daysOfWeek = rule.getDaysOfWeek();
                if (daysOfWeek != null) {
                    int dayValue = date.getDayOfWeek().getValue() % 7; // 0 = Sunday
                    String[] days = daysOfWeek.split(",");
                    for (String day : days) {
                        if (Integer.parseInt(day.trim()) == dayValue) {
                            return true;
                        }
                    }
                }
                break;
                
            case RECURRING_DATE:
                if (rule.getDayOfMonth() != null && 
                    rule.getDayOfMonth() == date.getDayOfMonth()) {
                    if (rule.getMonthOfYear() == null || 
                        rule.getMonthOfYear() == date.getMonthValue()) {
                        return true;
                    }
                }
                break;
                
            case DATE_RANGE:
                if (rule.getStartDate() != null && rule.getEndDate() != null) {
                    long epochDay = date.toEpochDay();
                    long start = rule.getStartDate() / 86400000L;
                    long end = rule.getEndDate() / 86400000L;
                    return epochDay >= start && epochDay <= end;
                }
                break;
        }
        
        return false;
    }
}
