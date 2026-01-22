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
     */
    public static DotLayout forYearView(int width, int height, float dotSize, float spacing) {
        int rows = 12;       // 12 months
        int columns = 31;    // Max 31 days
        
        // Calculate the actual dot size to fit
        float availableWidth = width - spacing;
        float availableHeight = height - spacing;
        
        float maxDotWidth = (availableWidth - (columns - 1) * spacing) / columns;
        float maxDotHeight = (availableHeight - (rows - 1) * spacing) / rows;
        
        float actualDotSize = Math.min(dotSize, Math.min(maxDotWidth, maxDotHeight));
        actualDotSize = Math.max(actualDotSize, 2); // Minimum 2px
        
        float gridWidth = columns * actualDotSize + (columns - 1) * spacing;
        float gridHeight = rows * actualDotSize + (rows - 1) * spacing;
        
        float startX = (width - gridWidth) / 2;
        float startY = (height - gridHeight) / 2;
        
        return new DotLayout(rows, columns, actualDotSize, spacing, 
                           width, height, startX, startY);
    }

    /**
     * Calculate layout for Month View (up to 6 weeks × 7 days).
     */
    public static DotLayout forMonthView(int width, int height, float dotSize, 
                                          float spacing, boolean hasHeader) {
        int rows = hasHeader ? 7 : 6;  // 6 weeks max + optional header
        int columns = 7;               // 7 days
        
        float availableWidth = width - spacing;
        float availableHeight = height - spacing;
        
        float maxDotWidth = (availableWidth - (columns - 1) * spacing) / columns;
        float maxDotHeight = (availableHeight - (rows - 1) * spacing) / rows;
        
        float actualDotSize = Math.min(dotSize, Math.min(maxDotWidth, maxDotHeight));
        actualDotSize = Math.max(actualDotSize, 2);
        
        float gridWidth = columns * actualDotSize + (columns - 1) * spacing;
        float gridHeight = rows * actualDotSize + (rows - 1) * spacing;
        
        float startX = (width - gridWidth) / 2;
        float startY = (height - gridHeight) / 2;
        
        return new DotLayout(rows, columns, actualDotSize, spacing,
                           width, height, startX, startY);
    }

    /**
     * Calculate layout for Progress View.
     */
    public static DotLayout forProgressView(int width, int height, float dotSize, 
                                             float spacing, int dotCount) {
        int columns = dotCount;
        int rows = 1;
        
        float availableWidth = width - spacing * 2;
        float maxDotWidth = (availableWidth - (columns - 1) * spacing) / columns;
        
        float actualDotSize = Math.min(dotSize, Math.min(maxDotWidth, height - spacing * 2));
        actualDotSize = Math.max(actualDotSize, 2);
        
        float gridWidth = columns * actualDotSize + (columns - 1) * spacing;
        float gridHeight = actualDotSize;
        
        float startX = (width - gridWidth) / 2;
        float startY = (height - gridHeight) / 2;
        
        return new DotLayout(rows, columns, actualDotSize, spacing,
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
