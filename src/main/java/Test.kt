import math.vectors.Vector3

object Test {

    @JvmStatic
    fun main(args: Array<String>) {

        val floats = floatArrayOf(1.0f, 0.0f, 2.0f)

        val data = floats + floatArrayOf(2.0f, 5.0f)

        for (v in data) {
            println(v)
        }

    }

}