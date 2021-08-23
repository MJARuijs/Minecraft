package graphics.model.animation

import graphics.model.animation.model.JointTransformation

data class Pose(val jointTransformations: HashMap<String, JointTransformation> = HashMap())