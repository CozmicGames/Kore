package com.cozmicgames.core.utils.spatial

abstract class Spatial() {
    constructor(x: Float, y: Float, width: Float, height: Float) : this() {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    internal var cachedStructureData: Any? = null

    var x = 0.0f
    var y = 0.0f
    var width = 0.0f
    var height = 0.0f

    val centerX get() = x + width * 0.5f
    val centerY get() = y + height * 0.5f
}