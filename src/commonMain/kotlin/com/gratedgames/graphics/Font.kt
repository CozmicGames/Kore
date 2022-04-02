package com.gratedgames.graphics

interface Font {
    val size: Int

    fun getCharImage(char: Char): Image?
}