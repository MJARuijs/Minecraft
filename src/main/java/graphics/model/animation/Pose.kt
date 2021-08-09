package graphics.model.animation

import graphics.model.animation.model.JointTransformation
import math.Quaternion
import math.matrices.Matrix4

data class Pose(val jointTransformations: HashMap<String, JointTransformation> = HashMap()) {

    constructor(transformations: Map<String, Matrix4>): this() {
        for (entry in transformations) {
            val position = entry.value.getPosition()
            val rotation = Quaternion.fromMatrix(entry.value)
            val scale = entry.value.getScale()
            jointTransformations[entry.key] = JointTransformation(position, rotation, scale)
        }
    }

    operator fun set(key: String, value: JointTransformation) {
        jointTransformations[key] = value
    }

}