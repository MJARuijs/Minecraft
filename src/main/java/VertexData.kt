import math.vectors.Vector2
import math.vectors.Vector3

data class VertexData(var data: FloatArray = floatArrayOf(), var hasNormal: Boolean = false, var hasTextureCoordinates: Boolean = false) {

    constructor(vertex: Vertex) : this() {
        data += vertex.position.toArray()

        if (vertex.normal != null) {
            data += vertex.normal.toArray()
            hasNormal = true
        }

        if (vertex.textureCoordinates != null) {
            data += vertex.textureCoordinates.toArray()
            hasTextureCoordinates = true
        }
    }

    fun getPosition(): Vector3? {
        if (data.size < 3) {
            return null
        }
        return Vector3(data[0], data[1], data[2])
    }

    fun getNormal(): Vector3? {
        if (!hasNormal) {
            return null
        }

        return Vector3(data[3], data[4], data[5])
    }

    fun getTextureCoordinates(): Vector2? {
        if (!hasTextureCoordinates) {
            return null
        }

        return if (hasNormal) {
            Vector2(data[6], data[7])
        } else {
            Vector2(data[3], data[4])
        }
    }

    fun toArray(): FloatArray {
        var array = FloatArray(0)
        if (data.size >= 3) {
            array += getPosition()!!.toArray()
        }

        if (hasNormal) {
            array += getNormal()!!.toArray()
        }

        if (hasTextureCoordinates) {
            array += getTextureCoordinates()!!.toArray()
        }

        return array
    }

    fun equalToVertex(vertex: Vertex): Boolean {
        if (hasNormal != (vertex.normal != null)) {
            return false
        }

        return true
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other !is VertexData) return false

        for (i in data.indices) {
            if (data[i] != other.data[i]) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + hasNormal.hashCode()
        result = 31 * result + hasTextureCoordinates.hashCode()
        return result
    }

}