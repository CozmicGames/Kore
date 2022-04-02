package com.gratedgames.graphics.gpu

import com.gratedgames.Kore
import com.gratedgames.graphics

class Viewport(var x: Int, var y: Int, var width: Int, var height: Int) {
    companion object {
        operator fun invoke(block: Viewport.() -> Unit) = Viewport(0, 0, Kore.graphics.width, Kore.graphics.height).also(block)
    }
}
