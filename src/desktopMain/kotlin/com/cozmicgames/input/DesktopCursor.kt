package com.cozmicgames.input

import com.cozmicgames.utils.Disposable
import org.lwjgl.glfw.GLFW.glfwDestroyCursor

class DesktopCursor(val handle: Long) : Cursor, Disposable {
    override fun dispose() {
        glfwDestroyCursor(handle)
    }
}