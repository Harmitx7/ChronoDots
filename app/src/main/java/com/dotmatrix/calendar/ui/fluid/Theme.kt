package com.dotmatrix.calendar.ui.fluid

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import java.io.InputStreamReader

// Config Loader
object FluidConfigLoader {
    fun loadConfig(context: Context): FluidUIConfig {
        val assetManager = context.assets
        val inputStream = assetManager.open("ui_transformation.json")
        val reader = InputStreamReader(inputStream)
        return Gson().fromJson(reader, FluidUIConfig::class.java).also {
            reader.close()
        }
    }
}

// Composition Locals
val LocalFluidConfig = staticCompositionLocalOf<FluidUIConfig> {
    error("No FluidConfig provided")
}

val LocalFluidColors = staticCompositionLocalOf<FluidColors> {
    error("No FluidColors provided")
}

@Immutable
data class FluidColors(
    val background: Color,
    val backgroundDark: Color,
    val surface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val tertiaryText: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val accent: Color,
    val dotPast: Color,
    val dotCurrent: Color,
    val dotFuture: Color,
    val dotInactive: Color
)

@Composable
fun FluidTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val config = remember { FluidConfigLoader.loadConfig(context) }
    val palette = config.uiTransformation.theme.colorPalette
    
    val fluidColors = remember(palette) {
        FluidColors(
            background = parseColor(palette.background),
            backgroundDark = parseColor(palette.backgroundDark),
            surface = parseColor(palette.backgroundDark), // Use backgroundDark as surface
            primaryText = parseColor(palette.primaryText),
            secondaryText = parseColor(palette.secondaryText),
            tertiaryText = parseColor(palette.tertiaryText),
            accentPrimary = parseColor(palette.accentPrimary),
            accentSecondary = parseColor(palette.accentSecondary),
            accent = parseColor(palette.accentPrimary), // Alias for accent
            dotPast = parseColor(palette.dotPast),
            dotCurrent = parseColor(palette.dotCurrent),
            dotFuture = parseColor(palette.dotFuture),
            dotInactive = parseColor(palette.dotInactive)
        )
    }

    val materialColorScheme = lightColorScheme(
        primary = fluidColors.accentPrimary,
        background = fluidColors.background,
        onBackground = fluidColors.primaryText,
        surface = fluidColors.background,
        onSurface = fluidColors.primaryText
    )

    CompositionLocalProvider(
        LocalFluidConfig provides config,
        LocalFluidColors provides fluidColors
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}

// Helper to parse hex color (supports #RRGGBB and rgba(r, g, b, a))
fun parseColor(colorString: String): Color {
    return try {
        if (colorString.startsWith("#")) {
            Color(android.graphics.Color.parseColor(colorString))
        } else if (colorString.startsWith("rgba")) {
            val parts = colorString.removePrefix("rgba(").removeSuffix(")").split(",")
            val r = parts[0].trim().toInt()
            val g = parts[1].trim().toInt()
            val b = parts[2].trim().toInt()
            val a = parts[3].trim().toFloat()
            Color(r, g, b, (a * 255).toInt())
        } else if (colorString == "transparent") {
            Color.Transparent
        } else {
            Color.Magenta // Fallback
        }
    } catch (e: Exception) {
        Color.Magenta // Fallback
    }
}

// Accessor Object
object FluidTheme {
    val colors: FluidColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFluidColors.current
        
    val config: FluidUIConfig
        @Composable
        @ReadOnlyComposable
        get() = LocalFluidConfig.current
}
