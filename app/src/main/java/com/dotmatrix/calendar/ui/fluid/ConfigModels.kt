package com.dotmatrix.calendar.ui.fluid

import com.google.gson.annotations.SerializedName

// Top Level Config
data class FluidUIConfig(
    @SerializedName("ui_transformation") val uiTransformation: UITransformation
)

data class UITransformation(
    val version: String,
    @SerializedName("target_platform") val targetPlatform: String,
    @SerializedName("design_system") val designSystem: String,
    val theme: ThemeConfig,
    val layout: LayoutConfig,
    val animations: AnimationsConfig,
    val interactions: Map<String, InteractionConfig>,
    @SerializedName("dot_states") val dotStates: Map<String, DotStateConfig>,
    val performance: PerformanceConfig,
    val accessibility: AccessibilityConfig
)

// Theme
data class ThemeConfig(
    val name: String,
    @SerializedName("color_palette") val colorPalette: ColorPalette,
    val typography: Map<String, TypographyStyle>,
    val spacing: SpacingConfig
)

data class ColorPalette(
    val background: String,
    @SerializedName("background_dark") val backgroundDark: String,
    @SerializedName("primary_text") val primaryText: String,
    @SerializedName("secondary_text") val secondaryText: String,
    @SerializedName("tertiary_text") val tertiaryText: String,
    @SerializedName("accent_primary") val accentPrimary: String,
    @SerializedName("accent_secondary") val accentSecondary: String,
    @SerializedName("dot_past") val dotPast: String,
    @SerializedName("dot_current") val dotCurrent: String,
    @SerializedName("dot_future") val dotFuture: String,
    @SerializedName("dot_inactive") val dotInactive: String
)

data class TypographyStyle(
    @SerializedName("font_family") val fontFamily: String,
    @SerializedName("font_weight") val fontWeight: Int,
    @SerializedName("font_size") val fontSize: Float,
    @SerializedName("line_height") val lineHeight: Float,
    @SerializedName("letter_spacing") val letterSpacing: Float = 0f,
    @SerializedName("text_transform") val textTransform: String? = null,
    val color: String
)

data class SpacingConfig(
    @SerializedName("screen_padding_horizontal") val screenPaddingHorizontal: Int,
    @SerializedName("screen_padding_top") val screenPaddingTop: Int,
    @SerializedName("screen_padding_bottom") val screenPaddingBottom: Int,
    @SerializedName("section_gap") val sectionGap: Int,
    @SerializedName("element_gap") val elementGap: Int,
    @SerializedName("dot_size") val dotSize: Int,
    @SerializedName("dot_gap") val dotGap: Int,
    @SerializedName("row_height") val rowHeight: Int
)

// Layout
data class LayoutConfig(
    val structure: String,
    val sections: List<LayoutSection>
)

data class LayoutSection(
    val id: String,
    val type: String,
    @SerializedName("height_ratio") val heightRatio: Float = 0f,
    val children: List<LayoutSection>? = null,
    val content: String? = null,
    val style: String? = null,
    val alignment: String? = null,
    val justify: String? = null,
    @SerializedName("padding_left") val paddingLeft: Int = 0,
    @SerializedName("padding_top") val paddingTop: Int = 0,
    @SerializedName("padding_horizontal") val paddingHorizontal: Int = 0,
    @SerializedName("margin_bottom") val marginBottom: Int = 0,
    val items: List<String>? = null,
    val columns: Any? = null, // Can be Int or String "auto"
    val rows: String? = null,
    val gap: Int = 0,
    val animation: Map<String, Any>? = null // Flexible map for different animation trigger types
)

// Animations
data class AnimationsConfig(
    @SerializedName("global_settings") val globalSettings: GlobalSettings,
    val definitions: Map<String, AnimationDefinition>
)

data class GlobalSettings(
    @SerializedName("reduced_motion") val reducedMotion: Boolean,
    @SerializedName("default_duration") val defaultDuration: Long,
    @SerializedName("default_easing") val defaultEasing: String
)

data class AnimationDefinition(
    val type: String,
    val properties: Map<String, PropertyAnimation>? = null,
    val steps: List<AnimationStep>? = null,
    @SerializedName("base_animation") val baseAnimation: Map<String, PropertyAnimation>? = null,
    @SerializedName("stagger_delay") val staggerDelay: Long = 0,
    @SerializedName("stagger_direction") val staggerDirection: String? = null,
    @SerializedName("initial_delay") val initialDelay: Long = 0,
    val keyframes: List<Keyframe>? = null,
    val duration: Long = 0,
    val easing: String? = null,
    val iterations: Int = 1,
    @SerializedName("exit_animation") val exitAnimation: Map<String, PropertyAnimation>? = null,
    @SerializedName("enter_animation") val enterAnimation: Map<String, PropertyAnimation>? = null,
    @SerializedName("transition_delay") val transitionDelay: Long = 0,
    val sequence: List<OrchestratedStep>? = null,
    val trigger: String? = null,
    val animation: Any? = null, // Can be complex object or string
    @SerializedName("spring_config") val springConfig: SpringConfig? = null,
    val target: String? = null,
    @SerializedName("pause_on_interaction") val pauseOnInteraction: Boolean = false
)

data class PropertyAnimation(
    val from: Any? = null, // Float or String
    val to: Any? = null,
    val duration: Long = 0,
    val easing: String? = null,
    @SerializedName("return_to") val returnTo: Any? = null,
    @SerializedName("spring_config") val springConfig: SpringConfig? = null,
    val alternate: Boolean = false
)

data class AnimationStep(
    val property: String,
    val from: Any? = null,
    val to: Any? = null,
    val action: String? = null,
    val duration: Long,
    val easing: String? = null
)

data class Keyframe(
    val time: Float,
    val scale: Float? = null,
    @SerializedName("shadow_opacity") val shadowOpacity: Float? = null
)

data class OrchestratedStep(
    val step: String,
    val animation: Any, // definition name or object
    val duration: Long,
    val delay: Long = 0,
    val target: String,
    val easing: String? = null
)

data class SpringConfig(
    val stiffness: Float,
    val damping: Float
)

// Interactions
data class InteractionConfig(
    val trigger: String,
    val target: String,
    @SerializedName("feedback_animation") val feedbackAnimation: Any? = null,
    val haptic: String,
    val action: ActionConfig? = null,
    val threshold: Int = 0,
    val animation: String? = null,
    val duration: Long = 0
)

data class ActionConfig(
    val type: String,
    val transition: String? = null,
    val duration: Long = 0,
    val direction: String? = null
)

// Dot States
data class DotStateConfig(
    @SerializedName("background_color") val backgroundColor: String,
    val opacity: Float,
    val scale: Float,
    val shadow: ShadowConfig,
    val transition: TransitionConfig? = null,
    @SerializedName("idle_animation") val idleAnimation: String? = null,
    @SerializedName("content_type") val contentType: String? = null,
    @SerializedName("emoji_config") val emojiConfig: EmojiConfig? = null
)

data class ShadowConfig(
    val enabled: Boolean,
    val color: String? = null,
    @SerializedName("offset_x") val offsetX: Int = 0,
    @SerializedName("offset_y") val offsetY: Int = 0,
    @SerializedName("blur_radius") val blurRadius: Int = 0,
    @SerializedName("spread_radius") val spreadRadius: Int = 0
)

data class TransitionConfig(
    val duration: Long,
    val easing: String
)

data class EmojiConfig(
    @SerializedName("font_size") val fontSize: Int,
    @SerializedName("vertical_align") val verticalAlign: String
)

// Performance
data class PerformanceConfig(
    @SerializedName("enable_gpu_acceleration") val enableGpuAcceleration: Boolean,
    @SerializedName("use_compositing_layers") val useCompositingLayers: Boolean
)

// Accessibility
data class AccessibilityConfig(
    @SerializedName("reduced_motion") val reducedMotion: ReducedMotion
)

data class ReducedMotion(
    val enabled: Boolean,
    @SerializedName("fallback_animations") val fallbackAnimations: Map<String, String>
)
