package chunks2

import graphics.Camera
import math.vectors.Vector3
import math.vectors.Vector4
import kotlin.math.roundToInt

class Selector {

    private val maxDistance = 8.0f

    fun getSelected(chunks: List<Chunk>, camera: Camera, position: Vector3): Vector3? {
        val clipCoords = Vector4(0f, 0f, -1f, 1f)
        val eyeSpace = camera.projectionMatrix.inverse().dot(clipCoords)
        eyeSpace.z = -1f
        eyeSpace.w = 0f

        val rayDirection = camera.viewMatrix.inverse().dot(eyeSpace).xyz().normal()

        val nearbyBlocks = ArrayList<Vector3>()
        for (chunk in chunks) {
            nearbyBlocks += chunk.getBlocksNearPosition(position, maxDistance)
        }

        for (min in nearbyBlocks) {
            val max = min + Vector3(1, 1, 1)

            var xMin = (min.x - position.x) / rayDirection.x
            var xMax = (max.x - position.x) / rayDirection.x

            if (xMin > xMax) {
                val temp = xMin
                xMin = xMax
                xMax = temp
            }

            var yMin = (min.y - position.y) / rayDirection.y
            var yMax = (max.y - position.y) / rayDirection.y

            if (yMin > yMax) {
                val temp = yMin
                yMin = yMax
                yMax = temp
            }

            if (xMin > yMax || yMin > xMax) {
                continue
            }

            if (yMin > xMin) {
                xMin = yMin
            }

            if (yMax < xMax) {
                xMax = yMax
            }

            var zMin = (min.z - position.z) / rayDirection.z
            var zMax = (max.z - position.z) / rayDirection.z

            if (zMin > zMax) {
                val temp = zMin
                zMin = zMax
                zMax = temp
            }

            if (xMin > zMax || zMin > xMax) {
                continue
            }

            println("YUP $min")
            return min
        }
        return null
    }

}