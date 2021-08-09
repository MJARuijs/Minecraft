package graphics.model.animation.model

import math.Quaternion
import math.matrices.Matrix4
import math.vectors.Vector3

class JointTransformation(var position: Vector3, val rotation: Quaternion, val scale: Vector3 = Vector3(1f, 1f, 1f)) {

    fun getTransformationMatrix(): Matrix4 {
        return Matrix4().translate(position).rotate(rotation).scale(scale)
    }

    companion object {

        fun interpolate(a: JointTransformation, b: JointTransformation, progress: Float): JointTransformation {
            val position = (b.position - a.position) * progress + a.position
            val rotation = Quaternion.interpolate(a.rotation, b.rotation, progress)
            val scale = (b.scale - a.scale) * progress + a.scale
            return JointTransformation(position, rotation, scale)
        }
    }
}