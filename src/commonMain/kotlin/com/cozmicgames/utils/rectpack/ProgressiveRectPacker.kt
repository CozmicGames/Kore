package com.cozmicgames.utils.rectpack

import com.cozmicgames.utils.collections.Pool

class ProgressiveRectPacker(val width: Int, val height: Int) {
    class Rectangle {
        var x = 0
        var y = 0
        var width = 0
        var height = 0
    }

    private val spaces = mutableListOf<Rectangle>()
    private val rectanglePool = Pool(Rectangle::class)

    init {
        spaces += rectanglePool.obtain().also {
            it.x = 0
            it.y = 0
            it.width = width
            it.height = height
        }
    }

    fun clear() {
        spaces.forEach(rectanglePool::free)
        spaces.clear()
        spaces += rectanglePool.obtain().also {
            it.x = 0
            it.y = 0
            it.width = width
            it.height = height
        }
    }

    fun add(width: Int, height: Int): Rectangle? {
        val rect = rectanglePool.obtain()
        rect.width = width
        rect.height = height

        for (i in spaces.indices.reversed()) {
            val space = spaces.getOrNull(i) ?: continue

            if (rect.width > space.width || rect.height > space.height)
                continue

            rect.x = space.x
            rect.y = space.y

            if (rect.width == space.width && rect.height == space.height)
                spaces.removeAt(i)
            else if (rect.height == space.height) {
                space.x += rect.width
                space.width -= rect.width
            } else if (rect.width == space.width) {
                space.y += rect.height
                space.height -= rect.height
            } else {
                spaces += rectanglePool.obtain().also {
                    it.x = space.x + rect.width
                    it.y = space.y
                    it.width = space.width - rect.width
                    it.height = rect.height
                }
                space.y += rect.height
                space.height -= rect.height
            }

            return rect
        }

        return null
    }
}