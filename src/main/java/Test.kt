import math.matrices.Matrix4
import math.vectors.Vector3
import kotlin.math.PI
import kotlin.math.acos

object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        val lightDirection = Vector3(1.0f, 1.0f, 0.0f)

        val xRotation = acos(lightDirection.dot(Vector3(1.0f, 0.0f, 0.0f)) / (lightDirection.length()))
        val yRotation = acos(lightDirection.dot(Vector3(0.0f, 1.0f, 0.0f)) / (lightDirection.length()))

        println(xRotation)
        println(yRotation)

    }

}