package chunks2

import graphics.Camera
import math.vectors.Vector3
import math.vectors.Vector4
import org.joml.Math.abs
import util.FloatUtils
import java.lang.Float.max
import java.lang.Float.min

class Selector {

    private val maxDistance = 8.0f

    fun getSelected(chunks: List<Chunk>, camera: Camera, position: Vector3): Pair<Vector3, FaceDirection>? {
        val clipCoords = Vector4(0f, 0f, -1f, 1f)
        val eyeSpace = camera.projectionMatrix.inverse().dot(clipCoords)
        eyeSpace.z = -1f
        eyeSpace.w = 0f

        val rayDirection = camera.viewMatrix.inverse().dot(eyeSpace).xyz().normal()

        val nearbyBlocks = ArrayList<Vector3>()
        for (chunk in chunks) {
            nearbyBlocks += chunk.getBlocksNearPosition(position, maxDistance)
        }

        var smallestDistance = Float.MAX_VALUE
        var selectedPoint = Vector3()
        var selectedBlock = Vector3()

        for (min in nearbyBlocks) {
            val hitPoint = determineBlockHit(min, position, rayDirection) ?: continue

            val distance = (hitPoint - position).length()
            if (distance < smallestDistance) {
                smallestDistance = distance
                selectedBlock = min
                selectedPoint = hitPoint
            }
        }

        if (smallestDistance == Float.MAX_VALUE) {
            return null
        }

        val selectedFace = determineFace(selectedBlock, selectedPoint)

        return Pair(selectedBlock, selectedFace)
    }

    private fun determineBlockHit(min: Vector3, position: Vector3, rayDirection: Vector3): Vector3? {
        val max = min + Vector3(1, 1, 1)
        var tMin = Float.MIN_VALUE
        var tMax = Float.MAX_VALUE

        for (i in 0 until 3) {

            val ood = 1.0f / rayDirection[i]
            var t1 = (min[i] - position[i]) * ood
            var t2 = (max[i] - position[i]) * ood

            if (t1 > t2) {
                val temp = t1
                t1 = t2
                t2 = temp
            }

            tMin = max(tMin, t1)
            tMax = min(tMax, t2)

            if (tMin > tMax) {
                return null
            }
        }

        return position + rayDirection * tMin
    }

    private fun determineFace(blockPosition: Vector3, hitPoint: Vector3): FaceDirection {
        val epsilon = 0.00005f
        if (abs(blockPosition.x - hitPoint.x) < epsilon) {
            return FaceDirection.LEFT
        }
        if (abs(hitPoint.x - (blockPosition.x + 1.0f)) < epsilon) {
            return FaceDirection.RIGHT
        }
        if (abs(hitPoint.y - blockPosition.y) < epsilon) {
            return FaceDirection.BOTTOM
        }
        if (abs(hitPoint.y - (blockPosition.y + 1.0f)) < epsilon) {
            return FaceDirection.TOP
        }
        if (abs(hitPoint.z - blockPosition.z) < epsilon) {
            return FaceDirection.BACK
        }
        if (abs(hitPoint.z - (blockPosition.z + 1.0f)) < epsilon) {
            return FaceDirection.FRONT
        }
        throw IllegalArgumentException("Point $hitPoint does not belong on block: $blockPosition")
    }

}