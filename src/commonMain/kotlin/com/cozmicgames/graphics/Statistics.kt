package com.cozmicgames.graphics

interface Statistics {
    val runTime: Float
    val lastFrameTimes: Iterable<Float>
    val averageFrameTime: Float
    val frameTime: Float
    val maxFrameTime: Float
    val minFrameTime: Float
    val numFrames: Int
    val numDrawCalls: Int
    val numComputeDispatches: Int
    val numBuffers: Int
    val numFramebuffers: Int
    val numPipelines: Int
    val numTextures: Int

    fun getNumberOfRenderedPrimitives(primitive: Primitive): Int
}

val Statistics.framesPerSecond get() = (1.0f / frameTime).toInt()
val Statistics.averageFramesPerSecond get() = (1.0f / averageFrameTime).toInt()
val Statistics.maxFramesPerSecond get() = (1.0f / minFrameTime).toInt()
val Statistics.minFramesPerSecond get() = (1.0f / maxFrameTime).toInt()
