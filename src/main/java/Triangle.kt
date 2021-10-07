data class Triangle(val v1: Vertex, val v2: Vertex, val v3: Vertex) {

    val vertices = listOf(v1, v2, v3)

    fun toArray(): FloatArray {
        var array = FloatArray(0)
        array += v1.toArray()
        array += v2.toArray()
        array += v3.toArray()
        return array
    }

}