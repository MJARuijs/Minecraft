import math.vectors.Vector2
import math.vectors.Vector3
import util.FloatUtils

data class Vertex(val position: Vector3, val normal: Vector3? = null, val textureCoordinates: Vector2? = null) {

    fun toArray(): FloatArray {
        var array = FloatArray(0)
        array += position.toArray()

        if (normal != null) {
            array += normal.toArray()
        }
        if (textureCoordinates != null) {
            array += textureCoordinates.toArray()
        }
        return array
    }

    override fun toString(): String {
        return "Vertex(position=$position, normal=$normal)"
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other !is Vertex) return false

        if (!FloatUtils.compare(position, other.position)) return false

        if (normal == null && other.normal != null) {
            return false
        }
        if (normal != null && other.normal == null) {
            return false
        }
        if (normal != null && other.normal != null) {
            if (!FloatUtils.compare(normal, other.normal)) {
                return false
            }
        }

        if (textureCoordinates == null && other.textureCoordinates != null) {
            return false
        }
        if (textureCoordinates != null && other.textureCoordinates == null) {
            return false
        }
        if (textureCoordinates != null && other.textureCoordinates != null) {
            if (!FloatUtils.compare(textureCoordinates, other.textureCoordinates)) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        return position.hashCode()
    }

}