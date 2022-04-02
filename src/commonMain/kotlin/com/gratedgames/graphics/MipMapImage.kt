package com.gratedgames.graphics

import com.gratedgames.Kore
import com.gratedgames.graphics
import com.gratedgames.graphics.gpu.Texture
import com.gratedgames.graphics.gpu.Texture2D
import kotlin.math.min

class MipMapImage(image: Image, minSize: Int = 1) : Iterable<MipMapImage.Level> {
    class Level(image: Image, width: Int, height: Int) {
        val image = Image(width, height)

        init {
            this.image.drawImage(image, 0, 0, width, height)
        }
    }

    private val levels: Array<Level>

    val numLevels get() = levels.size

    init {
        var size = min(image.width, image.height)
        var count = 1

        while (size > minSize) {
            count++
            size /= 2
        }

        val levelsList = arrayListOf<Level>()

        var width = image.width
        var height = image.height
        var lastImage = image

        repeat(count) {
            val level = Level(lastImage, width, height)
            levelsList += level
            lastImage = level.image

            width /= 2
            height /= 2
        }

        levels = levelsList.toTypedArray()
    }

    override fun iterator() = levels.iterator()

    operator fun get(level: Int) = levels[level]
}

fun MipMapImage.toTexture2D(format: Texture.Format = Texture.Format.RGBA8_UNORM, block: Texture2D.() -> Unit = {}) = Kore.graphics.createTexture2D(format, block).also {
    repeat(numLevels) { level ->
        it.setImage(this[level].image, level)
    }
}