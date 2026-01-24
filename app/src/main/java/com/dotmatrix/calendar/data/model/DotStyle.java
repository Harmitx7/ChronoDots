package com.dotmatrix.calendar.data.model;

/**
 * Visual style of the dot (Fill, Outline, etc.)
 */
public enum DotStyle {
    FILLED,     // Standard solid fill
    OUTLINE,    // Stroke only (empty/incomplete)
    RING,       // Concentric ring (multiple items)
    GLOW        // Solid with outer glow (highlight)
}
