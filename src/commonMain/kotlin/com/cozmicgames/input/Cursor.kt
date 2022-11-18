package com.cozmicgames.input

import com.cozmicgames.Kore
import com.cozmicgames.input
import com.cozmicgames.utils.Disposable

interface Cursor : Disposable {
    companion object {
        val ARROW = Kore.input.createStandardCursor(StandardType.ARROW)
        val IBEAM = Kore.input.createStandardCursor(StandardType.IBEAM)
        val CROSSHAIR = Kore.input.createStandardCursor(StandardType.CROSSHAIR)
        val HAND = Kore.input.createStandardCursor(StandardType.HAND)
        val HRESIZE = Kore.input.createStandardCursor(StandardType.HRESIZE)
        val VRESIZE = Kore.input.createStandardCursor(StandardType.VRESIZE)

        init {
            Kore.addShutdownListener {
                ARROW.dispose()
                IBEAM.dispose()
                CROSSHAIR.dispose()
                HAND.dispose()
                HRESIZE.dispose()
                VRESIZE.dispose()
            }
        }
    }

    enum class StandardType {
        ARROW,
        IBEAM,
        CROSSHAIR,
        HAND,
        HRESIZE,
        VRESIZE
    }
}