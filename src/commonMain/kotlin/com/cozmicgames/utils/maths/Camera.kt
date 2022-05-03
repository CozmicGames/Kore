package com.cozmicgames.utils.maths

abstract class Camera {
    open val projection = Matrix4x4()
    open val view = Matrix4x4()
    open val projectionView = Matrix4x4()
    open val inverseProjectionView = Matrix4x4()

    open val position = Vector3()
    open val direction = Vector3(0.0f, 0.0f, -1.0f)
    open val up = Vector3(0.0f, 1.0f, 0.0f)

    open val frustum = Frustum()

    abstract fun update()
}

fun Camera.target(point: Vector3) = target(point.x, point.y, point.z)

fun Camera.target(x: Float, y: Float, z: Float) {
    val dx = x - position.x
    val dy = y - position.y
    val dz = z - position.z
    direction.set(-dx, -dy, -dz).normalize()
}

class OrthographicCamera(var width: Int, var height: Int) : Camera() {
    var zoom = 1.0f

    val rectangle = Rectangle()

    init {
        resetPosition()
        update()
    }

    override fun update() {
        val halfWidth = width * 0.5f
        val halfHeight = height * 0.5f

        view.setToLookAt(position.x, position.y, position.z, position.x + direction.x, position.y + direction.y, position.z + direction.z, up.x, up.y, up.z)
        projection.setToOrtho2D(-zoom * halfWidth, zoom * halfWidth, -zoom * halfHeight, zoom * halfHeight)

        projectionView.set(projection).mul(view)
        inverseProjectionView.set(projectionView).invert()

        inverseProjectionView.transform(-1.0f, -1.0f, 0.0f, 1.0f) { x0, y0, _, _ ->
            rectangle.x = x0
            rectangle.y = y0

            inverseProjectionView.transform(1.0f, 1.0f, 0.0f, 1.0f) { x1, y1, _, _ ->
                rectangle.width = x1 - x0
                rectangle.height = y1 - y0
            }
        }

        frustum.update(projectionView)
    }

    fun resetPosition() {
        position.x = zoom * width * 0.5f
        position.y = zoom * height * 0.5f
    }

    operator fun contains(rectangle: Rectangle) = rectangle intersects this.rectangle
}

class PerspectiveCamera(var fieldOfView: Float, var aspect: Float, var near: Float = 0.01f, var far: Float = 1000.0f) : Camera() {
    init {
        update()
    }

    override fun update() {
        view.setToLookAt(position.x, position.y, position.z, position.x + direction.x, position.y + direction.y, position.z + direction.z, up.x, up.y, up.z)
        projection.setToPerspective(fieldOfView, aspect, near, far)

        projectionView.set(projection).mul(view)
        inverseProjectionView.set(projectionView).invert()

        frustum.update(projectionView)
    }
}