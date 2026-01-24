package com.dotmatrix.calendar.ui.fluid

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.AnimationSpec

object AnimationEngine {

    fun parseEasing(easingString: String?): Easing {
        if (easingString == null) return LinearEasing
        
        return when {
            easingString == "linear" -> LinearEasing
            easingString == "ease_in" -> CubicBezierEasing(0.42f, 0f, 1f, 1f)
            easingString == "ease_out" -> CubicBezierEasing(0f, 0f, 0.58f, 1f)
            easingString == "ease_in_out" -> CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
            easingString == "elastic_out" -> CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f) // Approximation
            easingString == "ease_out_cubic" -> CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
            easingString.startsWith("cubic_bezier") -> {
                try {
                    val content = easingString.removePrefix("cubic_bezier(").removeSuffix(")")
                    val parts = content.split(",").map { it.trim().toFloat() }
                    if (parts.size == 4) {
                        CubicBezierEasing(parts[0], parts[1], parts[2], parts[3])
                    } else {
                        LinearEasing
                    }
                } catch (e: Exception) {
                    LinearEasing
                }
            }
            else -> LinearEasing
        }
    }

    fun <T> getAnimationSpec(
        duration: Long,
        easing: String?,
        delay: Long = 0
    ): AnimationSpec<T> {
        val easingCurve = parseEasing(easing)
        return tween(
            durationMillis = duration.toInt(),
            delayMillis = delay.toInt(),
            easing = easingCurve
        )
    }

    // Helper to resolve a definition from the registry
    fun getDefinition(name: String): AnimationDefinition? {
        // In a real app, this might look up from a map passed down
        return null 
    }
}
