package com.cozmicgames.graphics

import com.cozmicgames.graphics.gpu.*

interface Statistics {
    /**
     * The time since the applications' startup.
     */
    val runTime: Float

    /**
     * A collection of a number of previous frame times, used to calculate the average frame time.
     */
    val lastFrameTimes: Iterable<Float>

    /**
     * The average frame time.
     */
    val averageFrameTime: Float

    /**
     * The current frame time.
     */
    val frameTime: Float

    /**
     * The maximum frame time so far.
     */
    val maxFrameTime: Float

    /**
     * The minimum frame time so far.
     */
    val minFrameTime: Float

    /**
     * The number of frames rendered since the applications' startup.
     */
    val numFrames: Int

    /**
     * The number of draw calls issued last frame.
     */
    val numDrawCalls: Int

    /**
     * The number of compute dispatches issued last frame.
     */
    val numComputeDispatches: Int

    /**
     * The number of active (created and not yet disposed) [GraphicsBuffer]s.
     */
    val numBuffers: Int

    /**
     * The number of active (created and not yet disposed) [Framebuffer]s.
     */
    val numFramebuffers: Int

    /**
     * The number of active (created and not yet disposed) [Pipeline]s.
     */
    val numPipelines: Int

    /**
     * The number of active (created and not yet disposed) [Texture]s.
     */
    val numTextures: Int

    /**
     * Gets the number of rendered primitives last frame.
     *
     * @param primitive The primitive type.
     *
     * @return The number of rendered primitives last frame.
     */
    fun getNumberOfRenderedPrimitives(primitive: Primitive): Int
}

/**
 * The current frames per second.
 */
val Statistics.framesPerSecond get() = (1.0f / frameTime).toInt()

/**
 * The average frames per second.
 */
val Statistics.averageFramesPerSecond get() = (1.0f / averageFrameTime).toInt()

/**
 * The maximum frames per second so far.
 */
val Statistics.maxFramesPerSecond get() = (1.0f / minFrameTime).toInt()

/**
 * The minimum frames per second so far.
 */
val Statistics.minFramesPerSecond get() = (1.0f / maxFrameTime).toInt()
