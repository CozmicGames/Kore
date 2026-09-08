package com.cozmicgames.core.input

import com.cozmicgames.core.input.Cursor
import com.cozmicgames.core.utils.Disposable
import org.lwjgl.glfw.GLFW.glfwDestroyCursor

class DesktopCursor(val handle: Long) : Cursor, Disposable {
    override fun dispose() {
        glfwDestroyCursor(handle)
    }
}