package com.dotmatrix.calendar.data.model;

/**
 * Represents a theme preset with predefined colors.
 */
public class ThemePreset {

    private final String id;
    private final String name;
    private final int backgroundColor;
    private final int dotColor;
    private final int accentColor;
    private final boolean isDark;
    private final boolean isPro;

    public ThemePreset(String id, String name, int backgroundColor, int dotColor, 
                       int accentColor, boolean isDark, boolean isPro) {
        this.id = id;
        this.name = name;
        this.backgroundColor = backgroundColor;
        this.dotColor = dotColor;
        this.accentColor = accentColor;
        this.isDark = isDark;
        this.isPro = isPro;
    }

    // Predefined themes from design doc
    public static final ThemePreset[] ALL_THEMES = {
        // Free themes
        new ThemePreset("classic_light", "Classic Light",
                0xFFFFFFFF, 0xFF212121, 0xFF2196F3, false, false),
        
        new ThemePreset("oled_dark", "OLED Dark",
                0xFF000000, 0xFFE0E0E0, 0xFFBB86FC, true, false),
        
        new ThemePreset("monochrome", "Monochrome",
                0xFFF5F5F5, 0xFF808080, 0xFF424242, false, false),
        
        // Pro themes (but glassmorphism is free per user request)
        new ThemePreset("glassmorphism", "Glassmorphism",
                0x40FFFFFF, 0xFFFFFFFF, 0xFFD4A574, true, false), // isPro = false

        new ThemePreset("nord_aurora", "Nord Aurora",
                0xFF2E3440, 0xFFECEFF4, 0xFF88C0D0, true, true),
        
        new ThemePreset("sunset_glow", "Sunset Glow",
                0xFF1A1015, 0xFFFFD6E8, 0xFFFF6B6B, true, true),
        
        new ThemePreset("ocean_breeze", "Ocean Breeze",
                0xFF0A1929, 0xFFB3E5FC, 0xFF0077BE, true, true),
        
        new ThemePreset("forest_calm", "Forest Calm",
                0xFF0D1F0D, 0xFFC8E6C9, 0xFF2E7D32, true, true),
        
        new ThemePreset("cyberpunk", "Cyberpunk",
                0xFF0D1117, 0xFF00FFFF, 0xFFFF00FF, true, true),
        
        new ThemePreset("pastel_dream", "Pastel Dream",
                0xFFFFF5F5, 0xFFC7CEEA, 0xFFFFD6E8, false, true),
        
        new ThemePreset("minimal_beige", "Minimal Beige",
                0xFFF7F3EF, 0xFFD4C5B9, 0xFF8B7355, false, true),
        
        new ThemePreset("tokyo_night", "Tokyo Night",
                0xFF1A1B26, 0xFFC0CAF5, 0xFF7AA2F7, true, true),
        
        new ThemePreset("dracula", "Dracula",
                0xFF282A36, 0xFFF8F8F2, 0xFFBD93F9, true, true),
    };

    /**
     * Find theme by ID.
     */
    public static ThemePreset findById(String id) {
        for (ThemePreset theme : ALL_THEMES) {
            if (theme.getId().equals(id)) {
                return theme;
            }
        }
        return ALL_THEMES[0]; // Default to classic light
    }

    /**
     * Check if theme with given ID is Pro-only.
     */
    public static boolean isProTheme(String id) {
        ThemePreset theme = findById(id);
        return theme != null && theme.isPro();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getDotColor() {
        return dotColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public boolean isDark() {
        return isDark;
    }

    public boolean isPro() {
        return isPro;
    }

    /**
     * Apply theme colors to a widget config.
     */
    public void applyTo(WidgetConfig config) {
        config.setThemeId(id);
        
        // Special handling for Glassmorphism to ensure premium look
        if (id.equals("glassmorphism")) {
            config.setBackgroundColor(0xFFFFFFFF); // White base
            config.setBackgroundOpacity(0.15f);    // Very subtle fill (15%)
            config.setDotColor(0xFFFFFFFF);        // White dots
            config.setAccentColor(0xFFFFFFFF);     // White accent (or maybe a gold/color?)
            // actually let's keep the preset colors but force opacity
            
            // Re-apply preset colors but override opacity
            config.setDotColor(dotColor);
            config.setAccentColor(accentColor);
            
            config.setHasBlur(true);
            config.setBlurRadius(30f);
        } else {
            config.setBackgroundColor(backgroundColor);
            config.setDotColor(dotColor);
            config.setAccentColor(accentColor);
            config.setHasBlur(false);
            config.setBackgroundOpacity(1.0f);
            config.setBlurRadius(0f);
        }
    }
}
