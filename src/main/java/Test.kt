import math.vectors.Vector3

object Test {

    @JvmStatic
    fun main(args: Array<String>) {

        val positions = ArrayList<Vector3>()
        positions += Vector3(0.0f, 0.0f, 1.0f)
        positions += Vector3(0.0f, 2.0f, 1.0f)
        positions += Vector3(0.0f, 0.0f, 3.0f)

        val list = ArrayList<FloatArray>(0)
        val arr = FloatArray(0)

        val t: FloatArray = positions.map {
            it.toArray()
        }.flatMap { it.toList() }.toFloatArray()

        println(list.size)
        println(t.size)

    }

}