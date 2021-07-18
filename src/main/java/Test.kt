import math.matrices.Matrix4

object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        val mat1 = Matrix4(
                floatArrayOf(
                        2f, 3f, 4f, 5f,
                        2f, 3f, 4f, 5f,
                        2f, 3f, 4f, 5f,
                        2f, 3f, 4f, 5f
                )
        )

        val mat2 = Matrix4(
                floatArrayOf(
                        2f, 3f, 4f, 5f,
                        3f, 5f, 4f, 5f,
                        2f, 3f, 4f, 5f,
                        2f, 3f, 4f, 5f
                )
        )

        val res = mat1 dot mat2
        println(res)
    }

}