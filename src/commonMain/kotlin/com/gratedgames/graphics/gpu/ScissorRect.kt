package com.gratedgames.graphics.gpu

import com.gratedgames.Kore
import com.gratedgames.graphics

class ScissorRect(var x: Int, var y: Int, var width: Int, var height: Int) {
    companion object {
        operator fun invoke(block: ScissorRect.() -> Unit) = ScissorRect(0, 0, Kore.graphics.width, Kore.graphics.height).also(block)
    }
}
