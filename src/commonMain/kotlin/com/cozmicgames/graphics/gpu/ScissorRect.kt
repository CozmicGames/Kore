package com.cozmicgames.graphics.gpu

import com.cozmicgames.Kore
import com.cozmicgames.graphics

class ScissorRect(var x: Int, var y: Int, var width: Int, var height: Int) {
    companion object {
        operator fun invoke(block: ScissorRect.() -> Unit) = ScissorRect(0, 0, Kore.graphics.width, Kore.graphics.height).also(block)
    }
}
