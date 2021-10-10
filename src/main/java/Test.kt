import math.vectors.Vector3

object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        val corner = Vector3(1, 1, 1)
        val lightPosition = Vector3(0, 1, 0) * 10000


        val lightDirection = (lightPosition - corner).normal()
        println(lightDirection)
    }

}