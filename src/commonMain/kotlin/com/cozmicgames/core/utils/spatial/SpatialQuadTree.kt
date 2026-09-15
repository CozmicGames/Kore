package com.cozmicgames.core.utils.spatial

import com.cozmicgames.core.utils.extensions.nextPowerOfTwo

class SpatialQuadTree(size: Float = 1024.0f, x: Float = 0.0f, y: Float = 0.0f, minNodeSize: Float = 16.0f) {
    companion object {
        private const val CHILD_NODE_NXNY = 0
        private const val CHILD_NODE_PXNY = 1
        private const val CHILD_NODE_NXPY = 2
        private const val CHILD_NODE_PXPY = 3
    }

    private inner class Node(val x: Float, val y: Float, val size: Float, val tree: SpatialQuadTree, var parent: Node? = null, var parentDirectionIndex: Int = 0) {
        val minX = x - size * 0.5f
        val minY = y - size * 0.5f
        val maxX = x + size * 0.5f
        val maxY = y + size * 0.5f
        var children: Array<Node?>? = null
        val hasChildren get() = !children.isNullOrEmpty()
        val isRootNode get() = parent == null
        val spatials = arrayListOf<Spatial>()

        private fun getChildNode(index: Int, centerX: Float, centerY: Float): Node {
            if (children == null)
                children = arrayOfNulls(4)

            var childNode = requireNotNull(children)[index]

            return if (childNode == null) {
                childNode = Node(centerX, centerY, size * 0.5f, tree, this, index)
                requireNotNull(children)[index] = childNode
                childNode
            } else
                childNode
        }

        private fun fitsInThisNode(spatial: Spatial): Boolean {
            val spatialMinX = spatial.x
            val spatialMinY = spatial.y
            val spatialMaxX = spatial.x + spatial.width
            val spatialMaxY = spatial.y + spatial.height

            return spatialMinX >= minX && spatialMinY >= minY && spatialMaxX <= maxX && spatialMaxY <= maxY
        }

        private fun fitsInChildNode(spatial: Spatial, centerX: Float, centerY: Float): Boolean {
            val halfSize = size * 0.5f
            val minX = centerX - halfSize * 0.5f
            val minY = centerY - halfSize * 0.5f
            val maxX = centerX + halfSize * 0.5f
            val maxY = centerY + halfSize * 0.5f

            val spatialMinX = spatial.x
            val spatialMinY = spatial.y
            val spatialMaxX = spatial.x + spatial.width
            val spatialMaxY = spatial.y + spatial.height

            return spatialMinX >= minX && spatialMinY >= minY && spatialMaxX <= maxX && spatialMaxY <= maxY
        }

        private fun tryCollapseToParent() {
            val parent = parent ?: return
            val siblings = requireNotNull(parent.children)

            var canCollapse = true

            for (sibling in siblings)
                if (sibling != null && sibling != this) {
                    canCollapse = false
                    break
                }

            if (canCollapse)
                parent.children = null
            else
                siblings[parentDirectionIndex] = null
        }

        private fun addToParent(spatial: Spatial) {
            if (isRootNode) {
                val dirX = spatial.centerX - x
                val dirY = spatial.centerY - y
                val halfSize = size * 0.5f

                parent = when {
                    dirX < 0.0f && dirY < 0.0f -> {
                        val centerX = x - halfSize
                        val centerY = y - halfSize

                        parentDirectionIndex = CHILD_NODE_NXNY
                        Node(centerX, centerY, size * 2.0f, tree)
                    }

                    dirX >= 0.0f && dirY < 0.0f -> {
                        val centerX = x + halfSize
                        val centerY = y - halfSize

                        parentDirectionIndex = CHILD_NODE_PXNY
                        Node(centerX, centerY, size * 2.0f, tree)
                    }

                    dirX < 0.0f && dirY >= 0.0f -> {
                        val centerX = x - halfSize
                        val centerY = y + halfSize

                        parentDirectionIndex = CHILD_NODE_NXPY
                        Node(centerX, centerY, size * 2.0f, tree)
                    }

                    dirX >= 0.0f && dirY >= 0.0f -> {
                        val centerX = x + halfSize
                        val centerY = y + halfSize

                        parentDirectionIndex = CHILD_NODE_PXPY
                        Node(centerX, centerY, size * 2.0f, tree)
                    }

                    else -> throw IllegalStateException()
                }

                tree.rootNode = requireNotNull(parent)
            }

            val parent = requireNotNull(parent)

            if (parent.fitsInThisNode(spatial))
                parent.add(spatial)
            else
                parent.addToParent(spatial)
        }

        private fun tryAddToChild(spatial: Spatial): Boolean {
            val dirX = spatial.centerX - x
            val dirY = spatial.centerY - y

            val halfSize = size * 0.5f

            when {
                dirX < 0.0f && dirY < 0.0f -> {
                    val centerX = x - halfSize
                    val centerY = y - halfSize
                    if (fitsInChildNode(spatial, centerX, centerY)) {
                        getChildNode(CHILD_NODE_NXNY, centerX, centerY).add(spatial)
                        return true
                    }
                }

                dirX >= 0.0f && dirY < 0.0f -> {
                    val centerX = x + halfSize
                    val centerY = y - halfSize
                    if (fitsInChildNode(spatial, centerX, centerY)) {
                        getChildNode(CHILD_NODE_PXNY, centerX, centerY).add(spatial)
                        return true
                    }
                }

                dirX < 0.0f && dirY >= 0.0f -> {
                    val centerX = x - halfSize
                    val centerY = y + halfSize
                    if (fitsInChildNode(spatial, centerX, centerY)) {
                        getChildNode(CHILD_NODE_NXPY, centerX, centerY).add(spatial)
                        return true
                    }
                }

                dirX >= 0.0f && dirY >= 0.0f -> {
                    val centerX = x + halfSize
                    val centerY = y + halfSize
                    if (fitsInChildNode(spatial, centerX, centerY)) {
                        getChildNode(CHILD_NODE_PXPY, centerX, centerY).add(spatial)
                        return true
                    }
                }
            }

            return false
        }

        fun add(spatial: Spatial) {
            if (!fitsInThisNode(spatial)) {
                addToParent(spatial)
                return
            }

            if (size >= tree.minNodeSize * 2.0f)
                if (spatial.width <= size * 0.5f && spatial.height <= size * 0.5f)
                    if (tryAddToChild(spatial))
                        return

            spatials += spatial
            spatial.cachedStructureData = this
        }

        fun remove(spatial: Spatial): Boolean {
            if (spatials.remove(spatial)) {
                spatial.cachedStructureData = null

                if (!hasChildren && spatials.isEmpty())
                    tryCollapseToParent()

                return true
            }
            return false
        }

        fun clear() {
            spatials.forEach {
                it.cachedStructureData = null
            }
            spatials.clear()

            children?.forEach {
                it?.clear()
            }
            children = null
        }

        fun forEach(callback: (Spatial) -> Unit) {
            spatials.forEach(callback)
            children?.forEach {
                it?.forEach(callback)
            }
        }

        fun query(x: Float, y: Float, width: Float, height: Float, callback: (Spatial) -> Boolean) {
            val queryMinX = x
            val queryMinY = y
            val queryMaxX = x + width
            val queryMaxY = y + height

            for (spatial in spatials) {
                val spatialMinX = spatial.x
                val spatialMinY = spatial.y
                val spatialMaxX = spatial.x + spatial.width
                val spatialMaxY = spatial.y + spatial.height

                if (spatialMinX <= queryMaxX && spatialMaxX >= queryMinX && spatialMinY <= queryMaxY && spatialMaxY >= queryMinY)
                    if (!callback(spatial))
                        return
            }

            children?.forEach {
                it?.let {
                    if (it.minX >= x && it.minY >= y && it.maxX <= x + width && it.maxY <= y + height)
                        it.query(x, y, width, height, callback)
                }
            }
        }
    }

    private val minNodeSize = minNodeSize.toInt().nextPowerOfTwo.toFloat()
    private var rootNode = Node(x, y, size, this)

    fun add(spatial: Spatial) {
        rootNode.add(spatial)
    }

    fun remove(spatial: Spatial): Boolean {
        val isRemoved = (spatial.cachedStructureData as? Node)?.remove(spatial)
        return isRemoved ?: false
    }

    fun clear() {
        rootNode.clear()
    }

    fun forEach(callback: (Spatial) -> Unit) {
        rootNode.forEach(callback)
    }

    fun query(x: Float, y: Float, width: Float, height: Float, callback: (Spatial) -> Boolean) {
        rootNode.query(x, y, width, height, callback)
    }
}