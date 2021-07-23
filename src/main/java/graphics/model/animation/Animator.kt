package graphics.model.animation

import math.matrices.Matrix4
import kotlin.math.PI

class Animator(private val model: AnimatedModel) {

    private var currentAnimation: Animation? = null
    private var animationTime = 0f
    private val rotationMatrix = Matrix4().rotateX(-PI.toFloat() / 2f)

    fun startAnimation(animation: Animation) {
        animationTime = 0f
        currentAnimation = animation

//        val transforms = animation.keyFrames[1].second
//        val currentPose = HashMap<String, Matrix4>()
//        for (transform in transforms.jointTransformations) {
//            currentPose[transform.key] = transform.value.getTransformationMatrix()
//        }
//
//        for (entry in currentPose) {
//            model.rootJoint.setLocalTransform(entry.key, entry.value)
//        }
//        model.rootJoint.setTransform(Matrix4())
    }

    fun update(delta: Float) {
        if (currentAnimation == null) {
            return
        }

        increaseTimer(delta)

        if (currentAnimation == null) {
            return
        }

        val currentPose = calculateCurrentPose()
        for (entry in currentPose) {
            model.rootJoint.setLocalTransform(entry.key, entry.value)
        }
        model.rootJoint.setTransform(Matrix4())
//        applyPoseToJoints(currentPose, model.rootJoint, Matrix4())
    }

    private fun increaseTimer(delta: Float) {
        animationTime += delta * 1000
        if (animationTime > currentAnimation!!.getLength()) {
            if (currentAnimation!!.loop) {
                animationTime %= currentAnimation!!.getLength()
            } else {
//                println("Done ${model.rootJoint.bindMatrix.getPosition()}")
                animationTime = 0f
                currentAnimation = null
            }
        }
//        println(" ${model.rootJoint.bindMatrix.getPosition()}")
    }

    private fun calculateCurrentPose(): HashMap<String, Matrix4> {
        val frames = getFrames()
        val progression = calculateProgression(frames[0].first, frames[1].first)
        return interpolatePoses(frames[0], frames[1], progression)
    }

    private fun applyPoseToJoints(currentPose: HashMap<String, Matrix4>, joint: Joint, parentTransformation: Matrix4) {
        val currentLocalTransformation = currentPose[joint.name] ?: throw IllegalArgumentException("No joint with id: ${joint.name} was found for the current pose..")
        val currentTransformation = parentTransformation dot currentLocalTransformation

        for (child in joint.children) {
            applyPoseToJoints(currentPose, child, currentTransformation)
        }

        joint.worldTransformation = currentTransformation
        joint.animatedTransform = currentTransformation dot joint.inverseBindMatrix
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
            currentPose[jointName] = currentTransformation.getTransformationMatrix()
//            currentPose[jointName] = previousTransformation.getTransformationMatrix()
        }

        return currentPose
    }
}