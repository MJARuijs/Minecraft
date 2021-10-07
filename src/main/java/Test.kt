import math.vectors.Vector3

object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        val e4 = Vector3(1.0f, 1.0f, -1.0f) - Vector3(-1.0f, 1.0f, -1.0f)
        val e5 = Vector3(1.0f, 1.0f, 1.0f) - Vector3(-1.0f, 1.0f, -1.0f)

        val cross = e4.cross(e5)
        val dot = cross.dot(-Vector3(0.0f, 1.0f, 0.0f))
        println(cross)
        println(dot)
    }

}