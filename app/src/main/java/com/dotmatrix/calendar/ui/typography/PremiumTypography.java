package com.dotmatrix.calendar.ui.typography;

import android.graphics.Paint;
import android.graphics.Typeface;

/**
 * iOS-inspired typography system for premium text rendering.
 * Emulates San Francisco font metrics and optical sizing.
 * 
 * Reference: Apple Human Interface Guidelines - Typography
 */
public class PremiumTypography {
   
    /**
     * Text size categories matching iOS Dynamic Type.
     */
    public enum TextSize {
        /** Large titles - 34sp */
        LARGE_TITLE(34f, 0.05f, 41f),
        /** Title 1 - 28sp */
        TITLE_1(28f, 0.04f, 34f),
        /** Title 2 - 22sp */
        TITLE_2(22f, 0.03f, 28f),
        /** Title 3 - 20sp */
        TITLE_3(20f, 0.02f, 25f),
        /** Headline - 17sp (default) */
        HEADLINE(17f, 0.03f, 22f),
        /** Body - 17sp */
        BODY(17f, 0.02f, 22f),
        /** Callout - 16sp */
        CALLOUT(16f, 0.02f, 21f),
        /** Subheadline - 15sp */
        SUBHEADLINE(15f, 0.02f, 20f),
        /** Footnote - 13sp */
        FOOTNOTE(13f, 0.01f, 18f),
        /** Caption 1 - 12sp */
        CAPTION_1(12f, 0.01f, 16f),
        /** Caption 2 - 11sp */
        CAPTION_2(11f, 0.01f, 13f);
        
        public final float sizeSp;
        public final float letterSpacing;
        public final float lineHeightSp;
        
        TextSize(float sizeSp, float letterSpacing, float lineHeightSp) {
            this.sizeSp = sizeSp;
            this.letterSpacing = letterSpacing;
            this.lineHeightSp = lineHeightSp;
        }
    }
    
    /**
     * Font weights matching SF Pro.
     */
    public enum FontWeight {
        /** Ultralight - 100 */
        ULTRALIGHT(100),
        /** Thin - 200 */
        THIN(200),
        /** Light - 300 */
        LIGHT(300),
        /** Regular - 400 */
        REGULAR(400),
        /** Medium - 500 */
        MEDIUM(500),
        /** Semibold - 600 */
        SEMIBOLD(600),
        /** Bold - 700 */
        BOLD(700),
        /** Heavy - 800 */
        HEAVY(800),
        /** Black - 900 */
        BLACK(900);
        
        public final int weight;
        
        FontWeight(int weight) {
            this.weight = weight;
        }
        
        /**
         * Get Typeface style approximation for older Android versions.
         */
        public int getTypefaceStyle() {
            if (weight >= 700) {
                return Typeface.BOLD;
            } else if (weight <= 300) {
                return Typeface.NORMAL; // No built-in light
            }
            return Typeface.NORMAL;
        }
    }
    
    /**
     * Configure Paint with iOS-style typography.
     * 
     * @param paint Paint object to configure
     * @param size Text size category
     * @param weight Font weight
     * @param density Display density (from DisplayMetrics)
     */
    public static void configurePaint(Paint paint, TextSize size, FontWeight weight, float density) {
        // Set size
        paint.setTextSize(size.sizeSp * density);
        
        // Set letter spacing (API 21+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            paint.setLetterSpacing(size.letterSpacing);
        }
        
        // Set font weight
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            // API 28+: Use variable font weight
            // First create base typeface, then apply weight
            Typeface base = Typeface.create("sans-serif", Typeface.NORMAL);
            Typeface typeface = Typeface.create(base, weight.weight, false);
            paint.setTypeface(typeface);
        } else {
            // Fallback: Approximate with system fonts
            String family = getFontFamilyForWeight(weight);
            Typeface typeface = Typeface.create(family, weight.getTypefaceStyle());
            paint.setTypeface(typeface);
        }
        
        // Premium rendering flags
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setFilterBitmap(true);
        
        // Hint for better rendering on small sizes
        if (size.sizeSp < 14) {
            paint.setHinting(Paint.HINTING_ON);
        } else {
            paint.setHinting(Paint.HINTING_OFF); // Let subpixel rendering dominate
        }
    }
    
    /**
     * Get font family approximation for pre-API 28 devices.
     */
    private static String getFontFamilyForWeight(FontWeight weight) {
        switch (weight) {
            case ULTRALIGHT:
            case THIN:
            case LIGHT:
                return "sans-serif-light";
            case MEDIUM:
            case SEMIBOLD:
                return "sans-serif-medium";
            case BOLD:
            case HEAVY:
            case BLACK:
                return "sans-serif-black";
            case REGULAR:
            default:
                return "sans-serif";
        }
    }
    
    /**
     * Get line height for text size (useful for multi-line text).
     */
    public static float getLineHeight(TextSize size, float density) {
        return size.lineHeightSp * density;
    }
    
    /**
     * Calculate optimal text baseline offset for vertical centering.
     * 
     * @param paint Configured paint
     * @return Baseline offset from center
     */
    public static float getCenterBaselineOffset(Paint paint) {
        Paint.FontMetrics metrics = paint.getFontMetrics();
        return (metrics.descent + metrics.ascent) / 2;
    }
    
    /**
     * Apply optical sizing adjustments for dots/small UI elements.
     * iOS uses optical sizing to make small text more readable.
     * 
     * @param baseSizeSp Base text size
     * @param targetSizeSp Target rendered size
     * @return Adjusted size with optical compensation
     */
    public static float applyOpticalSizing(float baseSizeSp, float targetSizeSp) {
        if (targetSizeSp < 12) {
            // Slightly increase size for very small text
            return baseSizeSp * 1.05f;
        } else if (targetSizeSp > 28) {
            // Slightly reduce size for very large text
            return baseSizeSp * 0.98f;
        }
        return baseSizeSp;
    }
}
