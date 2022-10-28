package com.cozmicgames.audio

import com.cozmicgames.utils.Updateable
import com.cozmicgames.utils.maths.Matrix4x4
import com.cozmicgames.utils.maths.Quaternion
import com.cozmicgames.utils.maths.Vector3
import com.cozmicgames.utils.maths.transform

open class AudioObject : Updateable {
    /**
     * The position of this [AudioObject] in 3d space.
     */
    val position = Vector3(0.0f, 0.0f, 0.0f)

    /**
     * The current velocity of this [AudioObject] in 3d space.
     */
    val velocity = Vector3(0.0f, 0.0f, 0.0f)

    /**
     * The current looking direction (i.e. forward direction) of this [AudioObject] in 3d space.
     * Must be normalized.
     */
    val direction = Vector3(0.0f, 0.0f, 1.0f)

    /**
     * The current up direction of this [AudioObject] in 3d space.
     * Must be normalized.
     */
    val up = Vector3(0.0f, 1.0f, 0.0f)

    /**
     * The 4 by 4 transform matrix this [AudioObject] should follow.
     * All the other fields will be set on [update] from the data contained in this transform matrix.
     */
    var followedTransform: Matrix4x4? = null

    /**
     * This will be called internally by [Kore.audio], don't call externally to ensure correct velocity calculation.
     */
    override fun update(delta: Float) {
        val transform = followedTransform ?: return

        val position = Vector3()

        transform.getTranslation(position)
        val rotation = Quaternion()
        transform.getRotation(rotation)

        if (delta > 0.0f)
            velocity.set(position).sub(this.position).div(delta)
        else
            velocity.set(0.0f)

        this.position.set(position)

        rotation.transform(direction.set(0.0f, 0.0f, 1.0f)).normalize()
        rotation.transform(up.set(0.0f, 1.0f, 0.0f)).normalize()
    }
}
