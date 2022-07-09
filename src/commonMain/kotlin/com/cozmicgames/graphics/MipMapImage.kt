package com.cozmicgames.graphics

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.gpu.Sampler
import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.graphics.gpu.Texture2D
import kotlin.math.min

/**
 * Creates mipmaps from the gives [Image].
 *
 * @param image The image to create mipmaps from.
 * @param minSize The minimum size of a mip level.
 */
class MipMapImage(image: Image, minSize: Int = 1) : Iterable<MipMapImage.Level> {
    /**
     * A single mip level.
     *
     * @param image The image for this level.
     * @param width The width of this level.
     * @param height The height of this level.
     */
    class Level(image: Image, width: Int, height: Int) {
        val image = Image(width, height)

        init {
            this.image.drawImage(image, 0, 0, width, height)
        }
    }

    /**
     * The number of levels.
     */
    val numLevels get() = levels.size

    private val levels: Array<Level>

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

    /**
     * Gets an iterator over the levels.
     *
     * @return An iterator over the levels.
     */
    override fun iterator() = levels.iterator()

    /**
     * Gets the level at the given index.
     *
     * @param level The index of the level to get.
     *
     * @return The level at the given index.
     */
    operator fun get(level: Int) = levels[level]
}

/**
 * Creates a [Texture2D] from the image.
 * @see [Graphics.createTexture2D]
 *
 * @param format The format of the texture. Defaults to [Texture.Format.RGBA8_UNORM].
 * @param block The block to execute to configure the texture.
 *
 * @return The created texture.
 */
fun MipMapImage.toTexture2D(sampler: Sampler, format: Texture.Format = Texture.Format.RGBA8_UNORM, block: Texture2D.() -> Unit = {}) = Kore.graphics.createTexture2D(format, sampler, block).also {
    repeat(numLevels) { level ->
        it.setImage(this[level].image, level)
    }
}