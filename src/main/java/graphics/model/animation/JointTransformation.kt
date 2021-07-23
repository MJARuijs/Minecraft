package graphics.model.animation

import math.Quaternion
import math.matrices.Matrix4
import math.vectors.Vector3

class JointTransformation(val position: Vector3, val rotation: Quaternion) {

    fun getTransformationMatrix(): Matrix4 {
        val matrix = Matrix4().translate(position)
        return matrix dot rotation.toMatrix()
    }

    companion object {

        fun interpolate(a: JointTransformation, b: JointTransformation, progress: Float, print: Boolean): JointTransformation {
            val position = (b.position - a.position) * progress + a.position
            if (print) {
//                println("a: ${a.position} b: ${b.position} $progress")
            }
            val rotation = Quaternion.interpolate(a.rotation, b.rotation, progress)
            return JointTransformation(position, rotation)
        }
    }
}