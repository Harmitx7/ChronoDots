package com.dotmatrix.calendar.chameleon

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.view.animation.DecelerateInterpolator
import com.dotmatrix.calendar.data.model.WidgetTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Animates smooth color transitions when widget theme changes.
 * Uses ValueAnimator to interpolate between old and new colors.
 */
class WidgetColorAnimator(private val context: Context) {
    
    companion object {
        private const val ANIMATION_DURATION_MS = 2000L // 2 seconds
    }
    
    private val argbEvaluator = ArgbEvaluator()
    private val activeAnimators = mutableMapOf<Int, ValueAnimator>()
    
    /**
     * Animates theme transition for a widget.
     * @param widgetId Widget instance ID
     * @param oldTheme Current theme (can be constructed from config)
     * @param newTheme Target theme
     * @param onUpdate Callback for each frame (to update widget)
     */
    suspend fun animateThemeTransition(
        widgetId: Int,
        oldTheme: WidgetTheme,
        newTheme: WidgetTheme,
        onUpdate: (WidgetTheme) -> Unit
    ) = withContext(Dispatchers.Main) {
        
        // Cancel any existing animation for this widget
        activeAnimators[widgetId]?.cancel()
        
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = ANIMATION_DURATION_MS
            interpolator = DecelerateInterpolator()
            
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                val interpolatedTheme = interpolateThemes(
                    oldTheme,
                    newTheme,
                    fraction
                )
                
                // Call update callback (will render widget)
                onUpdate(interpolatedTheme)
            }
            
            // Listener for end
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onUpdate(newTheme)
                    activeAnimators.remove(widgetId)
                }
            })
        }
        
        activeAnimators[widgetId] = animator
        animator.start()
    }
    
    /**
     * Cancels any active animation for a widget.
     */
    fun cancelAnimation(widgetId: Int) {
        activeAnimators[widgetId]?.cancel()
        activeAnimators.remove(widgetId)
    }
    
    /**
     * Interpolates between two themes based on fraction (0.0-1.0).
     */
    private fun interpolateThemes(
        startTheme: WidgetTheme,
        endTheme: WidgetTheme,
        fraction: Float
    ): WidgetTheme {
        return WidgetTheme(
            themeId = endTheme.themeId,
            name = endTheme.name,
            backgroundColor = interpolateColor(
                startTheme.backgroundColor,
                endTheme.backgroundColor,
                fraction
            ),
            dotColor = interpolateColor(
                startTheme.dotColor,
                endTheme.dotColor,
                fraction
            ),
            accentColor = interpolateColor(
                startTheme.accentColor,
                endTheme.accentColor,
                fraction
            ),
            dotColorCurrent = interpolateColor(
                startTheme.dotColorCurrent,
                endTheme.dotColorCurrent,
                fraction
            ),
            dotColorFuture = interpolateColor(
                startTheme.dotColorFuture,
                endTheme.dotColorFuture,
                fraction
            ),
            textPrimaryColor = interpolateColor(
                startTheme.textPrimaryColor,
                endTheme.textPrimaryColor,
                fraction
            ),
            textSecondaryColor = interpolateColor(
                startTheme.textSecondaryColor,
                endTheme.textSecondaryColor,
                fraction
            ),
            isChameleonGenerated = endTheme.isChameleonGenerated,
            sourceWallpaperHash = endTheme.sourceWallpaperHash
        )
    }
    
    /**
     * Interpolates between two colors using ArgbEvaluator.
     */
    private fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        return argbEvaluator.evaluate(fraction, startColor, endColor) as Int
    }
}
