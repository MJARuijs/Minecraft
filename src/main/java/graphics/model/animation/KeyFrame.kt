package graphics.model.animation

import graphics.model.animation.model.JointTransformation

class KeyFrame(var timeStamp: Float = 0f, val pose: Pose = Pose()) {

    constructor(timeStamp: Int, pose: Pose) : this(timeStamp.toFloat(), pose)

    constructor(timeStamp: Int, transformations: HashMap<String, JointTransformation>) : this(timeStamp.toFloat(), Pose(transformations))

}