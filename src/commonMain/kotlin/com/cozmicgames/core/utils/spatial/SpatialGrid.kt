package com.cozmicgames.core.utils.spatial

import com.cozmicgames.core.utils.collections.Array2D
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class SpatialGrid(val width: Int, val height: Int, val cellSize: Int) {
    private class Cell {
        val items = arrayListOf<Spatial>()
    }

    private val cells = Array2D(width, height) { _, _ -> Cell() }

    fun add(item: Spatial) {
        val x1 = floor(item.x / cellSize).toInt()
        val y1 = floor(item.y / cellSize).toInt()
        val x2 = ceil((item.x + item.width) / cellSize).toInt()
        val y2 = ceil((item.y + item.height) / cellSize).toInt()

        for (y in y1..y2) {
            for (x in x1..x2) {
                cells[x, y]?.items?.add(item)
            }
        }
    }

    fun remove(item: Spatial) {
        val x1 = floor(item.x / cellSize).toInt()
        val y1 = floor(item.y / cellSize).toInt()
        val x2 = ceil((item.x + item.width) / cellSize).toInt()
        val y2 = ceil((item.y + item.height) / cellSize).toInt()

        for (y in y1..y2) {
            for (x in x1..x2) {
                cells[x, y]?.items?.remove(item)
            }
        }
    }

    fun query(x: Int, y: Int, width: Int, height: Int, callback: (Spatial) -> Boolean) {
        val xx1 = x / cellSize
        val xy1 = y / cellSize
        val xx2 = (x + width) / cellSize
        val xy2 = (y + height) / cellSize

        val x1 = min(xx1, xx2)
        val y1 = min(xy1, xy2)
        val x2 = max(xx1, xx2)
        val y2 = max(xy1, xy2)

        if (x2 < 0 || y2 < 0 || x1 >= this.width || y1 >= this.height)
            return

        if (x1 == x2 && y1 == y2)
            cells[x1, y1]?.items?.forEach { if (!callback(it)) return } ?: Unit
        else
            for (yy in y1..y2)
                for (xx in x1..x2)
                    for (item in cells[xx, yy]?.items ?: emptyList())
                        if (!callback(item))
                            return
    }

    fun clear() {
        repeat(width) { x ->
            repeat(height) { y ->
                cells[x, y]?.items?.clear()
            }
        }
    }
}