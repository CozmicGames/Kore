package com.cozmicgames.audio

import com.cozmicgames.utils.maths.Matrix4x4
import com.cozmicgames.utils.maths.Quaternion
import com.cozmicgames.utils.maths.Vector3
import com.cozmicgames.utils.maths.transform

open class AudioListener {
    /**
     * The position of the listener in 3d space.
     */
    val position = Vector3(0.0f, 0.0f, 0.0f)

    /**
     * The current velocity of the listener in 3d space.
     */
    val velocity = Vector3(0.0f, 0.0f, 0.0f)

    /**
     * The current looking direction (i.e. forward direction) of the listener in 3d space.
     * Must be normalized.
     */
    val direction = Vector3(0.0f, 0.0f, 1.0f)

    /**
     * The current up direction of the listener in 3d space.
     * Must be normalized.
     */
    val up = Vector3(0.0f, 1.0f, 0.0f)

    /**
     * Sets the [position], [direction] and [up] properties to the values represented by the supplied transform matrix.
     *
     * @param transform The transform matrix.
     */
    fun setFromTransform(transform: Matrix4x4) {
        transform.getTranslation(position)

        val temp = Quaternion()
        transform.getRotation(temp)

        temp.transform(direction.set(0.0f, 0.0f, 1.0f)).normalize()
        temp.transform(up.set(0.0f, 1.0f, 0.0f)).normalize()
    }
}
