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
        // TIER 1: CORE AESTHETICS
        // 1. Phantom Black (OLED Premium)
        new ThemePreset("phantom_black", "Phantom Black",
                0xFF000000, 0xFFE8E8E8, 0xFFFFFFFF, true, false),

        // 2. Arctic Frost (Premium Light)
        new ThemePreset("arctic_frost", "Arctic Frost",
                0xFFFAFAFA, 0xFF1A1A1A, 0xFF000000, false, false),

        // 3. Liquid Glass (Glassmorphism Premium) - PRO
        new ThemePreset("liquid_glass", "Liquid Glass",
                0x1FFFFFFF, 0xFFF5F5F5, 0xFFFFFFFF, true, true), // 12% white bg



        // 4. Smoke & Mirrors (Dark Glassmorphism) - PRO
        new ThemePreset("smoke_mirrors", "Smoke & Mirrors",
                0x59000000, 0xFFEDECEC, 0xFFFFFFFF, true, true), // 45% black bg

        // TIER 2: CHROMATIC NEUTRALS
        // 5. Moonlit Slate
        new ThemePreset("moonlit_slate", "Moonlit Slate",
                0xFFF7F8FA, 0xFF2C3E50, 0xFF34495E, false, false),

        // 6. Warm Concrete
        new ThemePreset("warm_concrete", "Warm Concrete",
                0xFFF7F5F2, 0xFF3E3935, 0xFF4A423D, false, false),

        // 7. Graphite Mist
        new ThemePreset("graphite_mist", "Graphite Mist",
                0xFFE5E5E5, 0xFF2A2A2A, 0xFF1A1A1A, false, false),

        // TIER 3: SUBTLE CHROMATIC
        // 8. Sakura Dawn - PRO
        new ThemePreset("sakura_dawn", "Sakura Dawn",
                0xFFFFF5F7, 0xFF8B5A6B, 0xFFA0647A, false, true),

        // 9. Sage Whisper - PRO
        new ThemePreset("sage_whisper", "Sage Whisper",
                0xFFF5F7F5, 0xFF3D4F3D, 0xFF4A5F4A, false, true),

        // 10. Coastal Blue - PRO
        new ThemePreset("coastal_blue", "Coastal Blue",
                0xFFF5F8FA, 0xFF2C4A5E, 0xFF3B5F7A, false, true),

        // TIER 4: BOLD CHROMATIC
        // 11. Ember Glow - PRO
        new ThemePreset("ember_glow", "Ember Glow",
                0xFF1A1513, 0xFFD4A574, 0xFFE8A65A, true, true),

        // 12. Arctic Aurora - PRO
        new ThemePreset("arctic_aurora", "Arctic Aurora",
                0xFF0D1217, 0xFF6B9FBF, 0xFF5FA8D3, true, true),

        // 13. Neon Night - PRO
        new ThemePreset("neon_night", "Neon Night",
                0xFF0A0E14, 0xFFA07CFF, 0xFFB085FF, true, true),

        // TIER 5: GRADIENT PREMIUM (Solids for now)
        // 14. Sunset Gradient - PRO
        new ThemePreset("sunset_gradient", "Sunset Gradient",
                0xFFFF6B6B, 0xFFFFFFFF, 0xFFFFFFFF, true, true),

        // 15. Ocean Depths - PRO
        new ThemePreset("ocean_depths", "Ocean Depths",
                0xFF667EEA, 0xFFFFFFFF, 0xFFFFFFFF, true, true),

        // 16. Rose Gold Luxury - PRO
        new ThemePreset("rose_gold", "Rose Gold Luxury",
                0xFFFFDEE9, 0xFF8B5A6B, 0xFFA0647A, false, true),

        // TIER 6: DYNAMIC
        // 17. Dynamic Harmony
        new ThemePreset("dynamic_harmony", "Dynamic Harmony",
                0x00000000, 0x00000000, 0x00000000, false, false),

        // 18. Chameleon Pro - PRO
        new ThemePreset("chameleon_pro", "Chameleon Pro",
                0x00000000, 0x00000000, 0x00000000, false, true),
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
        return ALL_THEMES[0]; // Default to Phantom Black
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
        
        // Handle Blur/Glass Themes
        if (id.equals("liquid_glass")) {
            config.setBackgroundColor(backgroundColor);
            config.setBackgroundOpacity(0.12f);    
            config.setDotColor(dotColor);
            config.setAccentColor(accentColor);
            config.setHasBlur(true);
            config.setBlurRadius(25f);
        } else if (id.equals("smoke_mirrors")) {
            config.setBackgroundColor(backgroundColor);
            config.setBackgroundOpacity(0.35f);    
            config.setDotColor(dotColor);
            config.setAccentColor(accentColor);
            config.setHasBlur(true);
            config.setBlurRadius(30f);
        } else if (id.equals("dynamic_harmony") || id.equals("chameleon_pro")) {
            // Material You / Dynamic markers
            config.setBackgroundColor(0); // Config logic will handle this
            config.setDotColor(0);
            config.setAccentColor(0);
            config.setHasBlur(false);
            config.setBackgroundOpacity(1.0f);
        } else {
            // Standard Solid Themes
            config.setBackgroundColor(backgroundColor);
            config.setDotColor(dotColor);
            config.setAccentColor(accentColor);
            config.setHasBlur(false);
            config.setBackgroundOpacity(1.0f);
            config.setBlurRadius(0f);
        }
    }
}
