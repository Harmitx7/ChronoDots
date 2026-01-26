package com.dotmatrix.calendar.widget.glassmorphism

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import java.util.Random

/**
 * Renders iOS-style liquid glass effect as a bitmap.
 */
class GlassRenderer(private val context: Context) {
    
    /**
     * Renders complete glass effect bitmap.
     * 
     * @return Rendered glass background bitmap (excluding shadow which is separate)
     */
    fun renderGlass(
        material: GlassMaterial,
        width: Int,
        height: Int,
        widgetId: Int
    ): Bitmap {
        // Check if glass effects should be disabled for this device
        val compatHelper = com.dotmatrix.calendar.util.DeviceCompatHelper(context)
        if (compatHelper.shouldDisableGlassEffects()) {
            // Return a simple gradient fallback for low-end devices
            return createSimpleFallbackBitmap(width, height, material)
        }
        
        // Check cache first
        val isDark = material.backgroundColor == Color.BLACK
        val cacheKey = GlassEffectCache.getCacheKey(widgetId, width, height, material.blurRadius, isDark)
        val cached = GlassEffectCache.get(cacheKey)
        if (cached != null && !cached.isRecycled) {
            return cached
        }

        // Create base canvas
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        
        // Create clipping path for rounded corners (iOS Squircle-like)
        val density = context.resources.displayMetrics.density
        // iOS widgets have ~18-22dp radius standard. Fixed 22dp looks good.
        val cornerRadius = 22f * density 
        val clipPath = android.graphics.Path()
        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        clipPath.addRoundRect(rectF, cornerRadius, cornerRadius, android.graphics.Path.Direction.CW)
        
        // Save canvas to restore after clipping
        canvas.save()
        canvas.clipPath(clipPath)

        // 1. Blurred wallpaper background
        val blurredBackground = createBlurredBackground(width, height, material)
        canvas.drawBitmap(blurredBackground, 0f, 0f, null)
        blurredBackground.recycle() // Recycle intermediate bitmap
        
        // 2. Color overlay
        val overlayPaint = Paint().apply {
            color = material.backgroundColor
            alpha = (material.backgroundOpacity * 255).toInt().coerceIn(0, 255)
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        
        // 3. Noise texture (optional)
        if (material.hasNoise) {
            drawNoiseTexture(canvas, width, height, material.noiseOpacity)
        }
        
        // 5. Top highlight (light reflection) - Drawn BEFORE border so border is on top, but INSIDE clip
        if (material.hasTopHighlight) {
            drawTopHighlight(canvas, width, height, material.highlightOpacity)
        }
        
        // Restore canvas (unclip) to draw border (border center is on edge, might need half-clip or just draw on top)
        // Actually, for border stroke, we usually want it antialiased. Drawing it while clipped might cut half the stroke.
        // Let's restore.
        canvas.restore()
        
        // 4. Border/edge - Draw ON TOP of everything
        drawBorder(canvas, width, height, material, cornerRadius)
        

        
        // Cache the result
        GlassEffectCache.put(cacheKey, resultBitmap)
        
        return resultBitmap
    }
    
    /**
     * Creates blurred wallpaper background.
     */
    private fun createBlurredBackground(
        width: Int,
        height: Int,
        material: GlassMaterial
    ): Bitmap {
        val extractor = WallpaperBackgroundExtractor(context)
        return extractor.createGlassBackground(
            widgetWidth = width,
            widgetHeight = height,
            glassColor = material.backgroundColor,
            glassOpacity = material.backgroundOpacity,
            blurRadius = material.blurRadius
        )
    }
    
    /**
     * Creates a simple gradient fallback for low-end devices.
     * No blur, no noise - just solid color with rounded corners.
     */
    private fun createSimpleFallbackBitmap(
        width: Int,
        height: Int,
        material: GlassMaterial
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val density = context.resources.displayMetrics.density
        val cornerRadius = 22f * density
        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        
        // Simple solid color with some transparency
        val bgPaint = Paint().apply {
            color = material.backgroundColor
            alpha = ((material.backgroundOpacity + 0.2f).coerceIn(0f, 1f) * 255).toInt()
            isAntiAlias = true
        }
        
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, bgPaint)
        
        // Simple subtle border
        val borderPaint = Paint().apply {
            color = material.borderColor
            alpha = ((material.borderOpacity * 0.5f) * 255).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f * density
            isAntiAlias = true
        }
        
        val inset = borderPaint.strokeWidth / 2
        val borderRect = RectF(inset, inset, width - inset, height - inset)
        canvas.drawRoundRect(borderRect, cornerRadius - inset, cornerRadius - inset, borderPaint)
        
        return bitmap
    }
    
    /**
     * Draws subtle noise/grain texture.
     * Optimized to use a tiled 64x64 pattern to save memory.
     */
    private fun drawNoiseTexture(
        canvas: Canvas,
        width: Int,
        height: Int,
        opacity: Float
    ) {
        val noiseShader = getCachedNoiseShader(opacity)
        
        val paint = Paint().apply {
            shader = noiseShader
            isAntiAlias = true
            this.alpha = 255 // Shader handles opacity implicitly via generation, but if we change opacities dynamically we might need multiple shaders or stick to one standard noise.
            // Actually, for variable opacity, it's cheaper to have one strong noise shader and apply alpha here.
             this.alpha = (opacity * 255).toInt().coerceIn(0, 255)
        }
        
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private companion object {
        // Cache the noise shader to avoid regeneration
        private var cachedNoiseShader: android.graphics.BitmapShader? = null
        
        private fun getCachedNoiseShader(baseOpacity: Float): Shader {
             if (cachedNoiseShader != null) return cachedNoiseShader!!
             
             // Create a small 64x64 tile
             val tileSize = 64
             val noiseBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888)
             val pixels = IntArray(tileSize * tileSize)
             val random = Random(12345) // Fixed seed for consistent texture
             
             for (i in pixels.indices) {
                 // High contrast noise, we'll reduce alpha when drawing
                 val noise = random.nextInt(256)
                 // Greyscale noise
                 pixels[i] = Color.argb(255, noise, noise, noise)
             }
             noiseBitmap.setPixels(pixels, 0, tileSize, 0, 0, tileSize, tileSize)
             
             cachedNoiseShader = android.graphics.BitmapShader(
                 noiseBitmap, 
                 Shader.TileMode.REPEAT, 
                 Shader.TileMode.REPEAT
             )
             return cachedNoiseShader!!
        }
    }
    
    /**
     * Draws border/edge highlight.
     */
    private fun drawBorder(
        canvas: Canvas,
        width: Int,
        height: Int,
        material: GlassMaterial,
        cornerRadius: Float
    ) {
        val density = context.resources.displayMetrics.density
        val strokeW = material.borderWidth * density
        
        val borderPaint = Paint().apply {
            color = material.borderColor
            alpha = (material.borderOpacity * 255).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = strokeW
            isAntiAlias = true
        }
        
        // Inset by half stroke width nicely
        val inset = strokeW / 2
        val rect = RectF(
            inset,
            inset,
            width - inset,
            height - inset
        )
        
        canvas.drawRoundRect(rect, cornerRadius - inset, cornerRadius - inset, borderPaint)
    }
    
    /**
     * Draws top gradient highlight.
     */
    private fun drawTopHighlight(
        canvas: Canvas,
        width: Int,
        height: Int,
        opacity: Float
    ) {
        val gradientHeight = height * 0.4f 
        
        val gradient = LinearGradient(
            0f, 0f,
            0f, gradientHeight,
            Color.argb((opacity * 255).toInt().coerceIn(0, 255), 255, 255, 255), // White
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        
        val paint = Paint().apply {
            shader = gradient
            isAntiAlias = true
        }
        
        canvas.drawRect(0f, 0f, width.toFloat(), gradientHeight, paint)
    }
    
    /**
     * Creates shadow bitmap (drawn beneath widget).
     * This is separate because we can't draw outside the widget bounds easily 
     * without resizing the content view which affects padding.
     * We often render shadow INTO the same bitmap if we have padding, 
     * OR return a separate bitmap if the layout supports layers.
     * 
     * NOTE: For existing DotRenderer we likely want to Composite it into the main bitmap 
     * if the prompt asks for "Liquid Glass", usually the shadow is part of the widget aesthetics.
     * 
     * BUT: The prompt strictly says "6. Shadow beneath (for depth)... renderShadow is drawn separately".
     */
    fun renderShadow(
        material: GlassMaterial,
        width: Int,
        height: Int
    ): Bitmap {
        val offsetY = material.shadowOffsetY * context.resources.displayMetrics.density
        val blurRadius = material.shadowBlur * context.resources.displayMetrics.density
        
        // Shadow needs bit more space
        val shadowBitmap = Bitmap.createBitmap(
            width,
            (height + offsetY + blurRadius * 2).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(shadowBitmap)
        
        val shadowPaint = Paint().apply {
            color = material.shadowColor
            alpha = (material.shadowOpacity * 255).toInt().coerceIn(0, 255)
            maskFilter = BlurMaskFilter(
                blurRadius,
                BlurMaskFilter.Blur.NORMAL
            )
            isAntiAlias = true
        }
        
        // Shadow rect
        val cornerRadius = 24f * context.resources.displayMetrics.density
        val rect = RectF(
            blurRadius, // Inset X
            offsetY + blurRadius, // Inset Y
            width - blurRadius,
            height.toFloat() // + blurRadius?
        )
        
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, shadowPaint)
        
        return shadowBitmap
    }
}
