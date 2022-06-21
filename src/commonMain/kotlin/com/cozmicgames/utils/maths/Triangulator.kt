package com.cozmicgames.utils.maths

import kotlin.math.max
import kotlin.math.sign

/**
 * Adapted from https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/EarClippingTriangulator.java
 */
class Triangulator {
    companion object {
        private const val DEFAULT_CAPACITY = Short.MAX_VALUE.toInt()
    }

    private val CONCAVE = -1
    private val CONVEX = 1

    private val indicesArray = arrayListOf<Int>()
    private var indices = arrayListOf<Int>()

    private var vertices = Array(DEFAULT_CAPACITY * 2) { 0.0f }

    private var vertexCount = 0
    private val vertexTypes = arrayListOf<Int>()
    private val triangles = arrayListOf<Int>()

    fun computeTriangles(path: VectorPath, offset: Int = 0, count: Int = path.count): List<Int> {
        return computeTriangles({ path[it].x }, { path[it].y }, offset, count)
    }

    fun computeTriangles(vertices: Array<Vector2>, offset: Int = 0, count: Int = vertices.size): List<Int> {
        return computeTriangles({ vertices[it].x }, { vertices[it].y }, offset, count)
    }

    fun computeTriangles(vertices: Array<Float>, offset: Int = 0, count: Int = vertices.size / 2): List<Int> {
        return computeTriangles({ vertices[it * 2] }, { vertices[it * 2 + 1] }, offset, count)
    }

    fun computeTriangles(getX: (Int) -> Float, getY: (Int) -> Float, offset: Int, count: Int): List<Int> {
        if (vertices.size < count * 2)
            vertices = Array(count * 2) { 0.0f }

        repeat(count) {
            vertices[it * 2] = getX(it + offset)
            vertices[it * 2 + 1] = getY(it + offset)
        }

        vertexCount = count / 2
        val vertexCount = vertexCount
        val vertexOffset = offset / 2

        indices.clear()
        indices.ensureCapacity(vertexCount)

        if (isCW(vertices, offset, count)) {
            for (i in 0 until vertexCount)
                indices[i] = vertexOffset + i
        } else {
            var i = 0
            val n = vertexCount - 1
            while (i < vertexCount) {
                indices[i] = vertexOffset + n - i
                i++
            }
        }

        vertexTypes.clear()
        vertexTypes.ensureCapacity(vertexCount)

        var i = 0
        val n = vertexCount

        while (i < n) {
            vertexTypes.add(classifyVertex(i))
            ++i
        }

        triangles.clear()
        triangles.ensureCapacity(max(0, vertexCount - 2) * 3)
        triangulate()

        return triangles
    }

    private fun triangulate() {
        while (vertexCount > 3) {
            val earTipIndex = findEarTip()
            cutEarTip(earTipIndex)

            // The type of the two vertices adjacent to the clipped vertex may have changed.
            val previousIndex = previousIndex(earTipIndex)
            val nextIndex = if (earTipIndex == vertexCount) 0 else earTipIndex
            vertexTypes[previousIndex] = classifyVertex(previousIndex)
            vertexTypes[nextIndex] = classifyVertex(nextIndex)
        }
        if (vertexCount == 3) {
            val triangles = triangles
            val indices = this.indices
            triangles.add(indices[0])
            triangles.add(indices[1])
            triangles.add(indices[2])
        }
    }

    private fun classifyVertex(index: Int): Int {
        val previous = indices[previousIndex(index)] * 2
        val current = indices[index] * 2
        val next = indices[nextIndex(index)] * 2

        return computeSpannedAreaSign(
            vertices[previous], vertices[previous + 1], vertices[current], vertices[current + 1],
            vertices[next], vertices[next + 1]
        )
    }

    private fun findEarTip(): Int {
        for (i in 0 until vertexCount) if (isEarTip(i)) return i

        // Desperate mode: if no vertex is an ear tip, we are dealing with a degenerate polygon (e.g. nearly collinear).
        // Note that the input was not necessarily degenerate, but we could have made it so by clipping some valid ears.

        // Idea taken from Martin Held, "FIST: Fast industrial-strength triangulation of polygons", Algorithmica (1998),
        // http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.115.291

        // Return a convex or tangential vertex if one exists.
        for (i in 0 until vertexCount) if (vertexTypes[i] != CONCAVE) return i
        return 0 // If all vertices are concave, just return the first one.
    }

    private fun isEarTip(earTipIndex: Int): Boolean {
        if (vertexTypes[earTipIndex] == CONCAVE) return false
        val previousIndex = previousIndex(earTipIndex)
        val nextIndex = nextIndex(earTipIndex)
        val p1 = indices[previousIndex] * 2
        val p2 = indices[earTipIndex] * 2
        val p3 = indices[nextIndex] * 2
        val p1x = vertices[p1]
        val p1y = vertices[p1 + 1]
        val p2x = vertices[p2]
        val p2y = vertices[p2 + 1]
        val p3x = vertices[p3]
        val p3y = vertices[p3 + 1]

        // Check if any point is inside the triangle formed by previous, current and next vertices.
        // Only consider vertices that are not part of this triangle, or else we'll always find one inside.
        var i = nextIndex(nextIndex)
        while (i != previousIndex) {

            // Concave vertices can obviously be inside the candidate ear, but so can tangential vertices
            // if they coincide with one of the triangle's vertices.
            if (vertexTypes[i] != CONVEX) {
                val v = indices[i] * 2
                val vx = vertices[v]
                val vy = vertices[v + 1]
                // Because the polygon has clockwise winding order, the area sign will be positive if the point is strictly inside.
                // It will be 0 on the edge, which we want to include as well.
                // note: check the edge defined by p1->p3 first since this fails _far_ more then the other 2 checks.
                if (computeSpannedAreaSign(p3x, p3y, p1x, p1y, vx, vy) >= 0) {
                    if (computeSpannedAreaSign(p1x, p1y, p2x, p2y, vx, vy) >= 0) {
                        if (computeSpannedAreaSign(p2x, p2y, p3x, p3y, vx, vy) >= 0) return false
                    }
                }
            }
            i = nextIndex(i)
        }
        return true
    }

    private fun cutEarTip(earTipIndex: Int) {
        triangles.add(indices[previousIndex(earTipIndex)])
        triangles.add(indices[earTipIndex])
        triangles.add(indices[nextIndex(earTipIndex)])
        indicesArray.removeAt(earTipIndex)
        vertexTypes.removeAt(earTipIndex)
        vertexCount--
    }

    private fun previousIndex(index: Int): Int {
        return (if (index == 0) vertexCount else index) - 1
    }

    private fun nextIndex(index: Int): Int {
        return (index + 1) % vertexCount
    }

    private fun computeSpannedAreaSign(p1x: Float, p1y: Float, p2x: Float, p2y: Float, p3x: Float, p3y: Float): Int {
        var area = p1x * (p3y - p2y)
        area += p2x * (p1y - p3y)
        area += p3x * (p2y - p1y)
        return sign(area).toInt()
    }
}