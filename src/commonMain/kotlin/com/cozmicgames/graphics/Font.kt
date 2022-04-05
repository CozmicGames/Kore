package com.cozmicgames.graphics

interface Font {
    val size: Int

    fun getCharImage(char: Char): Image?
}