package com.cozmicgames.graphics

/**
 * Represents a font that can be used to create character images.
 */
interface Font {
    /**
     * Creates a character image from the given character.
     * If the character is not supported by the font, null is returned.
     *
     * @param char The character to create an image for.
     * @param size The font size to produce the character image with.
     *
     * @return The image for the given character.
     */
    fun getCharImage(char: Char, size: Float): Image?
}