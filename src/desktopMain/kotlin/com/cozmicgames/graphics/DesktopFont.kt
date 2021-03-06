package com.cozmicgames.graphics

import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage

class DesktopFont(private val awtFont: java.awt.Font) : Font {
    private class DerivedFont(val awtFont: java.awt.Font) {
        private val images = hashMapOf<Char, Image>()

        fun getImage(char: Char): Image? {
            if (char in images)
                return images[char]

            if (!awtFont.canDisplay(char))
                return null

            val tempImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
            val tempGraphics = tempImage.graphics as Graphics2D
            tempGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            tempGraphics.font = awtFont
            val metrics = tempGraphics.fontMetrics

            val bufferedImage = BufferedImage(metrics.charWidth(char), metrics.height, BufferedImage.TYPE_INT_ARGB)
            val graphics = bufferedImage.graphics as Graphics2D
            graphics.color = Color(0f, 0f, 0f, 0f)
            graphics.fillRect(0, 0, bufferedImage.width, bufferedImage.height)
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.font = awtFont
            graphics.color = Color.WHITE
            graphics.drawString(char.toString(), 0, metrics.ascent)

            val image = Image(bufferedImage.width, bufferedImage.height)

            repeat(bufferedImage.height) { y ->
                repeat(bufferedImage.width) { x ->
                    val color = bufferedImage.getRGB(x, (bufferedImage.height - 1) - y)

                    val a = ((color ushr 24) and 0xFF).toFloat() / 0xFF
                    val b = ((color ushr 16) and 0xFF).toFloat() / 0xFF
                    val g = ((color ushr 8) and 0xFF).toFloat() / 0xFF
                    val r = (color and 0xFF).toFloat() / 0xFF

                    image.pixels.data[image.getPixelsIndex(x, y)].set(r, g, b, a)
                }
            }

            return image
        }
    }

    private val derivedFonts = hashMapOf<Float, DerivedFont>()

    private fun getDerivedFont(size: Float) = derivedFonts.getOrPut(size) {
        DerivedFont(awtFont.deriveFont(size))
    }

    override fun getCharImage(char: Char, size: Float) = getDerivedFont(size).getImage(char)
}