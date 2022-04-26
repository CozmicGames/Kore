package com.cozmicgames

interface Application {
    /**
     * Called when the application is first created.
     */
    fun onCreate() {}

    /**
     * Called each frame.
     *
     * @param delta The time in seconds since the last frame.
     */
    fun onFrame(delta: Float) {}

    /**
     * Called when the application is being destroyed.
     */
    fun onDispose() {}

    /**
     * Called when the application is resized.
     *
     * @param width The new width of the application.
     * @param height The new height of the application.
     */
    fun onResize(width: Int, height: Int) {}

    /**
     * Called when the application is paused.
     */
    fun onPause() {}

    /**
     * Called when the application is resumed from a paused state.
     */
    fun onResume() {}
}
