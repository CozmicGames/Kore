package com.cozmicgames.graphics

/**
 * Describes a primitive shape.
 * [POINTS]: 1 vertex per point
 * [LINES]: 2 vertices per line
 * [LINE_STRIP]: 1 vertex per line segment
 * [TRIANGLES]: 3 vertices per triangle
 * [TRIANGLE_STRIP]: 3 vertices per triangle
 */
enum class Primitive {
    POINTS,
    LINES,
    LINE_STRIP,
    TRIANGLES,
    TRIANGLE_STRIP
}