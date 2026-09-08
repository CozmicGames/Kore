package com.cozmicgames.core.utils

import com.cozmicgames.core.utils.maths.randomInt
import kotlin.math.max
import kotlin.math.min

class Animation<T : Any>(val frameDuration: Float, val keyFrames: List<T>, var playMode: PlayMode = PlayMode.NORMAL) {
    enum class PlayMode {
        NORMAL,
        REVERSED,
        LOOP,
        LOOP_REVERSED,
        LOOP_PINGPONG,
        LOOP_RANDOM
    }


    val animationDuration = keyFrames.size * frameDuration

    private var lastFrameNumber = 0
    private var lastStateTime = 0.0f

    fun getKeyFrame(stateTime: Float, looping: Boolean): T? {
        val oldPlayMode = playMode
        if (looping && (playMode == PlayMode.NORMAL || playMode == PlayMode.REVERSED)) {
            playMode = if (playMode == PlayMode.NORMAL) PlayMode.LOOP else PlayMode.LOOP_REVERSED
        } else if (!looping && !(playMode == PlayMode.NORMAL || playMode == PlayMode.REVERSED)) {
            playMode = if (playMode == PlayMode.LOOP_REVERSED) PlayMode.REVERSED else PlayMode.LOOP
        }
        val frame = getKeyFrame(stateTime)
        playMode = oldPlayMode
        return frame
    }

    fun getKeyFrame(stateTime: Float): T {
        val frameNumber = getKeyFrameIndex(stateTime)
        return keyFrames[frameNumber]
    }

    fun getKeyFrameIndex(stateTime: Float): Int {
        if (keyFrames.size == 1) return 0
        var frameNumber = (stateTime / frameDuration).toInt()
        when (playMode) {
            PlayMode.NORMAL -> frameNumber = min(keyFrames.size - 1, frameNumber)
            PlayMode.LOOP -> frameNumber %= keyFrames.size
            PlayMode.LOOP_PINGPONG -> {
                frameNumber %= (keyFrames.size * 2 - 2)
                if (frameNumber >= keyFrames.size) frameNumber = keyFrames.size - 2 - (frameNumber - keyFrames.size)
            }

            PlayMode.LOOP_RANDOM -> {
                val lastFrameNumber = (lastStateTime / frameDuration).toInt()
                frameNumber = if (lastFrameNumber != frameNumber) {
                    randomInt(keyFrames.size - 1)
                } else {
                    this.lastFrameNumber
                }
            }

            PlayMode.REVERSED -> frameNumber = max(keyFrames.size - frameNumber - 1, 0)
            PlayMode.LOOP_REVERSED -> {
                frameNumber %= keyFrames.size
                frameNumber = keyFrames.size - frameNumber - 1
            }
        }
        lastFrameNumber = frameNumber
        lastStateTime = stateTime
        return frameNumber
    }

    fun isAnimationFinished(stateTime: Float): Boolean {
        val frameNumber = (stateTime / frameDuration).toInt()
        return keyFrames.size - 1 < frameNumber
    }
}