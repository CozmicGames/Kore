package com.cozmicgames

interface Application {
    fun onCreate() {}
    fun onFrame(delta: Float) {}
    fun onDispose() {}
    fun onResize(width: Int, height: Int) {}
    fun onPause() {}
    fun onResume() {}
}
