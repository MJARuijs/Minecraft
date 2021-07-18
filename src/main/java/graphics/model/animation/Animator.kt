package graphics.model.animation

import math.matrices.Matrix4

class Animator(private val model: AnimatedModel) {

    private var currentAnimation: Animation? = null
    private var animationTime = 0f

    fun startAnimation(animation: Animation) {
        println("Start ${model.rootJoint.transformation.getPosition()}")
        println(model.rootJoint.name)
        println()
        println()
        println()
        animationTime = 0f
        currentAnimation = animation
    }

    fun update(delta: Float) {
        if (currentAnimation == null) {
            return
        }

        increaseTimer(delta)

        if (currentAnimation == null) {
            return
        }
//        println(model.rootJoint.transformation.getPosition())

        val currentPose = calculateCurrentPose()
//        println(currentPose[model.rootJoint.name]!!.getPosition())
        applyPoseToJoints(currentPose, model.rootJoint, Matrix4())
//        println(model.rootJoint.transformation.getPosition())
//        println()
    }

    private fun increaseTimer(delta: Float) {
        animationTime += delta * 1000
        if (animationTime > currentAnimation!!.getLength()) {
            if (currentAnimation!!.loop) {
                animationTime %= currentAnimation!!.getLength()
            } else {
                println("Done ${model.rootJoint.transformation.getPosition()}")
                animationTime = 0f
                currentAnimation = null
            }
        }
        println(" ${model.rootJoint.transformation.getPosition()}")
    }

    private fun calculateCurrentPose(): HashMap<String, Matrix4> {
        val frames = getFrames()
        val progression = calculateProgression(frames[0].first, frames[1].first)
        return interpolatePoses(frames[0], frames[1], progression)
    }

    private fun applyPoseToJoints(currentPose: HashMap<String, Matrix4>, joint: Joint, parentTransformation: Matrix4) {

        val currentLocalTransformation = currentPose[joint.name] ?: throw IllegalArgumentException("No joint with id: ${joint.name} was found for the current pose..")
        val currentTransformation = parentTransformation dot currentLocalTransformation

//        if (joint.name == "Torso_Bone") {
//            println("${joint.name} ${currentTransformation dot joint.inverseBindMatrix}")
//        }
        for (child in joint.children) {
            applyPoseToJoints(currentPose, child, currentTransformation)
        }

        joint.transformation = currentTransformation dot joint.inverseBindMatrix
    }

    private fun getFrames(): List<Pair<Int, Pose>> {
        val keyFrames = currentAnimation!!.keyFrames
        var previousFrame = keyFrames[0]
        var nextFrame = keyFrames[0]
        for (i in 1 until keyFrames.size) {
            nextFrame = keyFrames[i]
            if (nextFrame.first > animationTime) {
                break
            }
            previousFrame = keyFrames[i]
        }
        return listOf(previousFrame, nextFrame)
    }

    private fun calculateProgression(previousFrameTime: Int, nextFrameTime: Int): Float {
        val totalTime = nextFrameTime - previousFrameTime
        val currentTime = animationTime - previousFrameTime
        return currentTime / totalTime
    }

    private fun interpolatePoses(previousFrame: Pair<Int, Pose>, nextFrame: Pair<Int, Pose>, progression: Float): HashMap<String, Matrix4> {
        val currentPose = HashMap<String, Matrix4>()

        for (jointName in previousFrame.second.jointTransformations.keys) {
            val previousTransformation = previousFrame.second.jointTransformations[jointName] ?: throw IllegalArgumentException("No joint with id: $jointName found for previous frame")
            val nextTransformation = nextFrame.second.jointTransformations[jointName] ?: throw IllegalArgumentException("No joint with id: $jointName found for next frame")
            val currentTransformation = JointTransformation.interpolate(previousTransformation, nextTransformation, progression, jointName == "Torso_Bone")
            if (jointName == "Torso_Bone") {
                println(previousTransformation.position)
                println(currentTransformation.position)
                println()
            }
            currentPose[jointName] = currentTransformation.getTransformationMatrix()
//            currentPose[jointName] = previousTransformation.getTransformationMatrix()
        }

        return currentPose
    }
}