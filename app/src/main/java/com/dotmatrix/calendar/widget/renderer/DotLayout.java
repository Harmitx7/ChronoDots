package com.dotmatrix.calendar.widget.renderer;

/**
 * Represents the calculated layout for dots in a widget.
 */
public class DotLayout {
    
    private final int rows;
    private final int columns;
    private final float dotSize;
    private final float spacing;
    private final int totalWidth;
    private final int totalHeight;
    private final float startX;
    private final float startY;

    public DotLayout(int rows, int columns, float dotSize, float spacing,
                     int totalWidth, int totalHeight, float startX, float startY) {
        this.rows = rows;
        this.columns = columns;
        this.dotSize = dotSize;
        this.spacing = spacing;
        this.totalWidth = totalWidth;
        this.totalHeight = totalHeight;
        this.startX = startX;
        this.startY = startY;
    }

    /**
     * Calculate layout for Year View (12 months × 31 days).
     * Dots are now fully responsive - they scale to fill the available space optimally.
     */
    /**
     * Calculate layout for Year View (12 months × 31 days).
     * Respects user preference, scaling down ONLY if needed to fit.
     */
    public static DotLayout forYearView(int width, int height, float preferredDotSize, float preferredSpacing) {
        int rows = 12;       // 12 months
        int columns = 31;    // Max 31 days
        
        // 1. Try with preferred values
        float spacing = preferredSpacing;
        float dotSize = preferredDotSize;
        
        // 2. Check required dimensions
        float requiredWidth = columns * dotSize + (columns - 1) * spacing;
        float requiredHeight = rows * dotSize + (rows - 1) * spacing;
        
        // 3. Scale down if too big
        if (requiredWidth > width || requiredHeight > height) {
            float scaleX = width / requiredWidth;
            float scaleY = height / requiredHeight;
            float scale = Math.min(scaleX, scaleY);
            
            // Apply scale (keeping minimums to avoid disappearing dots)
            dotSize = Math.max(dotSize * scale, 2f);
            spacing = Math.max(spacing * scale, 1f);
        }
        
        // 4. Re-calculate final grid dimensions
        float finalGridWidth = columns * dotSize + (columns - 1) * spacing;
        float finalGridHeight = rows * dotSize + (rows - 1) * spacing;
        
        // 5. Center
        float startX = (width - finalGridWidth) / 2;
        float startY = (height - finalGridHeight) / 2;
        
        return new DotLayout(rows, columns, dotSize, spacing, 
                           width, height, startX, startY);
    }

    /**
     * Calculate layout for Month View (up to 6 weeks × 7 days).
     * Dots are now fully responsive - they scale to fill the available space optimally.
     */
    /**
     * Calculate layout for Month View (up to 6 weeks × 7 days).
     * Respects user preference, scaling down ONLY if needed to fit.
     */
    public static DotLayout forMonthView(int width, int height, float preferredDotSize, 
                                          float preferredSpacing, boolean hasHeader) {
        int rows = hasHeader ? 7 : 6;
        int columns = 7;
        
        float spacing = preferredSpacing;
        float dotSize = preferredDotSize;
        
        float requiredWidth = columns * dotSize + (columns - 1) * spacing;
        float requiredHeight = rows * dotSize + (rows - 1) * spacing;
        
        if (requiredWidth > width || requiredHeight > height) {
            float scaleX = width / requiredWidth;
            float scaleY = height / requiredHeight;
            float scale = Math.min(scaleX, scaleY);
            
            dotSize = Math.max(dotSize * scale, 3f);
            spacing = Math.max(spacing * scale, 2f);
        }
        
        float finalGridWidth = columns * dotSize + (columns - 1) * spacing;
        float finalGridHeight = rows * dotSize + (rows - 1) * spacing;
        
        float startX = (width - finalGridWidth) / 2;
        float startY = (height - finalGridHeight) / 2;
        
        return new DotLayout(rows, columns, dotSize, spacing,
                           width, height, startX, startY);
    }

    /**
     * Calculate layout for Progress View.
     * Dots are now fully responsive - they scale to fill the available space optimally.
     */
    /**
     * Calculate layout for Progress View.
     * Respects user preference, scaling down ONLY if needed to fit.
     */
    public static DotLayout forProgressView(int width, int height, float preferredDotSize, 
                                             float preferredSpacing, int dotCount) {
        int columns = dotCount;
        int rows = 1;
        
        float spacing = preferredSpacing;
        float dotSize = preferredDotSize;
        
        float requiredWidth = columns * dotSize + (columns - 1) * spacing;
        float requiredHeight = dotSize;
        
        if (requiredWidth > width || requiredHeight > height) {
            float scaleX = width / requiredWidth;
            float scaleY = height / requiredHeight;
            float scale = Math.min(scaleX, scaleY);
            
            dotSize = Math.max(dotSize * scale, 3f);
            spacing = Math.max(spacing * scale, 1f);
        }
        
        float finalGridWidth = columns * dotSize + (columns - 1) * spacing;
        float finalGridHeight = dotSize;
        
        float startX = (width - finalGridWidth) / 2;
        float startY = (height - finalGridHeight) / 2;
        
        return new DotLayout(rows, columns, dotSize, spacing,
                           width, height, startX, startY);
    }

    /**
     * Calculate layout for Week View (7 days).
     */
    /**
     * Calculate layout for Week View (7 days).
     * Respects user preference, scaling down ONLY if needed to fit.
     */
    public static DotLayout forWeekView(int width, int height, float preferredDotSize, 
                                         float preferredSpacing, boolean hasHeader) {
        int rows = hasHeader ? 2 : 1;
        int columns = 7;
        
        float spacing = preferredSpacing;
        float dotSize = preferredDotSize;
        
        float requiredWidth = columns * dotSize + (columns - 1) * spacing;
        float requiredHeight = rows * dotSize + (rows - 1) * spacing;
        
        if (requiredWidth > width || requiredHeight > height) {
            float scaleX = width / requiredWidth;
            float scaleY = height / requiredHeight;
            float scale = Math.min(scaleX, scaleY);
            
            dotSize = Math.max(dotSize * scale, 4f);
            spacing = Math.max(spacing * scale, 2f);
        }
        
        float finalGridWidth = columns * dotSize + (columns - 1) * spacing;
        float finalGridHeight = rows * dotSize + (rows - 1) * spacing;
        
        float startX = (width - finalGridWidth) / 2;
        float startY = (height - finalGridHeight) / 2;
        
        return new DotLayout(rows, columns, dotSize, spacing,
                           width, height, startX, startY);
    }

    /**
     * Get the center X position for a dot at given column.
     */
    public float getDotCenterX(int column) {
        return startX + column * (dotSize + spacing) + dotSize / 2;
    }

    /**
     * Get the center Y position for a dot at given row.
     */
    public float getDotCenterY(int row) {
        return startY + row * (dotSize + spacing) + dotSize / 2;
    }

    /**
     * Get the left X position for a dot at given column.
     */
    public float getDotLeft(int column) {
        return startX + column * (dotSize + spacing);
    }

    /**
     * Get the top Y position for a dot at given row.
     */
    public float getDotTop(int row) {
        return startY + row * (dotSize + spacing);
    }

    // Getters
    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public float getDotSize() { return dotSize; }
    public float getSpacing() { return spacing; }
    public int getTotalWidth() { return totalWidth; }
    public int getTotalHeight() { return totalHeight; }
    public float getStartX() { return startX; }
    public float getStartY() { return startY; }
}
