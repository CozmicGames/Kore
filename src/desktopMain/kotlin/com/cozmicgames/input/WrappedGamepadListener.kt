package com.cozmicgames.input

import com.cozmicgames.Kore
import com.cozmicgames.input
import net.java.games.input.ControllerEvent
import net.java.games.input.ControllerListener

class WrappedGamepadListener(val listener: GamepadListener) : ControllerListener {
    override fun controllerAdded(e: ControllerEvent) {
        (Kore.input as DesktopInput).onControllerAdded(e.controller)
    }

    override fun controllerRemoved(e: ControllerEvent) {
        (Kore.input as DesktopInput).onControllerRemoved(e.controller)
    }
}