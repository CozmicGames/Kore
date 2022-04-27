package com.cozmicgames.graphics

import kotlin.math.max
import kotlin.math.min

object DesktopStatistics : Statistics {
    override var runTime = 0.0f
    override val lastFrameTimes get() = frameTimes.asIterable()
    override var averageFrameTime = 0.0f
    override var frameTime = 0.0f
    override var maxFrameTime = 0.0f
    override var minFrameTime = 0.0f
    override var numFrames = 0
    override var numDrawCalls = 0
    override var numComputeDispatches = 0
    override var numBuffers = 0
    override var numFramebuffers = 0
    override var numPipelines = 0
    override var numTextures = 0

    val renderedPrimitives = IntArray(Primitive.values().size)

    override fun getNumberOfRenderedPrimitives(primitive: Primitive) = renderedPrimitives[primitive.ordinal]

    private val frameTimes = FloatArray(20)
    private var isFirstFrame = true

    fun newFrame(delta: Float) {
        if (isFirstFrame) {
            frameTimes.fill(delta)
            maxFrameTime = delta
            minFrameTime = delta
        } else {
            frameTimes.copyInto(frameTimes, 1)
            frameTimes[0] = delta
            maxFrameTime = max(maxFrameTime, delta)
            minFrameTime = min(minFrameTime, delta)
        }

        averageFrameTime = frameTimes.average().toFloat()
        frameTime = delta
        runTime += delta
        numFrames++
        numDrawCalls = 0
        numComputeDispatches = 0
        renderedPrimitives.fill(0)
    }
}