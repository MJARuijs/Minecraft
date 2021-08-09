package graphics.model.animation

import graphics.model.animation.model.AnimatedModel
import graphics.model.animation.model.Joint
import graphics.model.animation.model.JointTransformation
import math.Quaternion
import math.matrices.Matrix4

data class Animator(private val model: AnimatedModel) {

    private val currentPose = getDefaultPose()
    private val defaultPose = getDefaultPose()

    private var currentAnimation: Animation? = null
    private var previousFrame: KeyFrame? = null
    private var nextFrame: KeyFrame? = null
    private var currentFrameIndex = 0
    private var animationTime = 0f
    private var reversing = false

    private fun getDefaultPose(): HashMap<String, JointTransformation> {
        val defaultTransformations = model.rootJoint.getJoints()
        val defaultPose = HashMap<String, JointTransformation>()

        for (jointTransformation in defaultTransformations) {
            val defaultTransformation = jointTransformation.getDefaultTransformation()
            val position = defaultTransformation.getPosition()
            val rotation = Quaternion.fromMatrix(defaultTransformation)
            val scale = defaultTransformation.getScale()
            defaultPose[jointTransformation.name] = JointTransformation(position, rotation, scale)
        }

        return defaultPose
    }

    fun startAnimation(animation: Animation) {
        if (currentAnimation == null) {
            resetAnimator()
            currentAnimation = animation

            if (animation.keyFrames.first().timeStamp == 0f) {
                previousFrame = animation.keyFrames[currentFrameIndex++]
                nextFrame = animation.keyFrames[currentFrameIndex++]
            } else {
                previousFrame = KeyFrame(0, currentPose)
                nextFrame = animation.keyFrames.first()

                val startOffset = nextFrame!!.timeStamp

                startAnimation(Animation("transition_to_${animation.name}", listOf(previousFrame!!, nextFrame!!), LoopEffect.NONE) {
                    animation.keyFrames.forEach { frame ->
                        frame.timeStamp -= startOffset
                    }
                    startAnimation(animation)
                })
            }
        } else {
            resetAnimator()
            currentAnimation = animation

            if (animation.keyFrames.first().timeStamp == 0f) {
                previousFrame = animation.keyFrames[currentFrameIndex++]
                nextFrame = animation.keyFrames[currentFrameIndex++]
            } else {
                previousFrame = KeyFrame(0, defaultPose)
                nextFrame = animation.keyFrames.first()

                val startOffset = nextFrame!!.timeStamp

                startAnimation(Animation("transition_to_${animation.name}", listOf(previousFrame!!, nextFrame!!), LoopEffect.NONE) {
                    animation.keyFrames.forEach { frame ->
                        frame.timeStamp -= startOffset
                    }
                    startAnimation(animation)
                })
            }
        }
    }

    var print = false

    fun stopAnimating(transitionDuration: Int) {
        previousFrame = KeyFrame(0, currentPose)
//        nextFrame = KeyFrame(transitionDuration, defaultPose)
        nextFrame = KeyFrame(transitionDuration, currentPose)
        currentAnimation = Animation("stop_animating", listOf(previousFrame!!, nextFrame!!))
        print = true
        resetAnimator()

//        startAnimation(currentAnimation!!)
    }

    fun update(delta: Float, transformation: Matrix4) {
        if (currentAnimation == null) {
            return
        }

        updateTimer(delta)
        updateFrames()

        if (currentAnimation == null) {
            return
        }

        val currentPose = calculateCurrentPose()
        applyPoseToJoints(currentPose, model.rootJoint, transformation)
    }

    private fun resetAnimator() {
        animationTime = 0f
        currentFrameIndex = 0
        reversing = false
    }

    private fun updateTimer(delta: Float) {
        if (reversing) {
            animationTime -= delta * 1000
        } else {
            animationTime += delta * 1000
        }
    }

    private fun updateFrames() {
        if (reversing) {
            if (animationTime < nextFrame!!.timeStamp) {
                if (currentFrameIndex <= 0) {

                    when (currentAnimation!!.loopEffect) {
                        LoopEffect.NONE -> {
                            currentAnimation!!.onFinish()
                        }
                        LoopEffect.START_OVER -> {

                        }
                        LoopEffect.REVERSE -> {
                            reversing = false
                            currentFrameIndex = 0
                            animationTime = 0f

                            previousFrame = currentAnimation!!.keyFrames[currentFrameIndex++]
                            nextFrame = currentAnimation!!.keyFrames[currentFrameIndex++]
                        }
                    }

                    if (currentAnimation!!.loopEffect == LoopEffect.NONE) {
                        currentAnimation!!.onFinish()
                    } else {

                    }
                } else {
                    previousFrame = nextFrame
                    nextFrame = currentAnimation!!.keyFrames[currentFrameIndex--]
                }
            }
        } else {
            if (animationTime > nextFrame!!.timeStamp) {
                if (currentFrameIndex >= currentAnimation!!.numberOfFrames()) {

                    when (currentAnimation!!.loopEffect) {
                        LoopEffect.NONE -> {
                            val onFinish = currentAnimation!!.onFinish
                            resetAnimator()
                            currentAnimation = null
                            onFinish()
                        }
                        LoopEffect.START_OVER -> {
                            currentFrameIndex = 0
                            animationTime = 0f
                        }
                        LoopEffect.REVERSE -> {
                            reversing = true
                            currentFrameIndex = currentAnimation!!.numberOfFrames() - 1
                            animationTime = nextFrame!!.timeStamp

                            previousFrame = currentAnimation!!.keyFrames[currentFrameIndex--]
                            nextFrame = currentAnimation!!.keyFrames[currentFrameIndex--]
                        }
                    }
                } else {
                    previousFrame = nextFrame
                    nextFrame = currentAnimation!!.keyFrames[currentFrameIndex++]
                }
            }
        }
    }

    private fun calculateCurrentPose(): HashMap<String, JointTransformation> {
        if (previousFrame == null) {
            throw IllegalArgumentException("Cannot play animation when previous keyframe is null..")
        }
        if (nextFrame == null) {
            throw IllegalArgumentException("Cannot play animation when next keyframe is null..")
        }

        var progression = calculateProgression(previousFrame!!.timeStamp, nextFrame!!.timeStamp)

        if (progression <= 0.0f) {
            progression = 0.0f
        }
        if (progression >= 0.999f) {
            progression = 1.0f
        }

//        if (print || progression == 1.0f) {
//            for (j in currentPose) {
//                println(j.key)
//                println(j.value)
//            }
//            print = false
//        }
        return interpolatePoses(previousFrame!!, nextFrame!!, progression)
    }

    private fun applyPoseToJoints(currentPose: HashMap<String, JointTransformation>, joint: Joint, parentTransformation: Matrix4) {
        val currentLocalTransformation = currentPose[joint.name] ?: throw IllegalArgumentException("No joint with id: ${joint.name} was found for the current pose..")
        val currentWorldTransformation = parentTransformation dot currentLocalTransformation.getTransformationMatrix()

        joint.calculateAnimatedTransformation(currentWorldTransformation)

        for (child in joint.children) {
            applyPoseToJoints(currentPose, child, currentWorldTransformation)
        }
    }

    private fun calculateProgression(previousFrameTime: Float, nextFrameTime: Float): Float {
        val totalTime = nextFrameTime - previousFrameTime
        val currentTime = animationTime - previousFrameTime
        return currentTime / totalTime
    }

    private fun interpolatePoses(previousFrame: KeyFrame, nextFrame: KeyFrame, progression: Float): HashMap<String, JointTransformation> {
        for (jointName in previousFrame.pose.jointTransformations.keys) {
            val previousTransformation = previousFrame.pose.jointTransformations[jointName] ?: throw IllegalArgumentException("No joint with id: $jointName found for previous frame")
            val nextTransformation = nextFrame.pose.jointTransformations[jointName] ?: throw IllegalArgumentException("No joint with id: $jointName found for next frame")
            val currentTransformation = JointTransformation.interpolate(previousTransformation, nextTransformation, progression)
//            if (print) {
//                println(jointName)
//                println(previousTransformation.scale)
//                println(nextTransformation.scale)
//                println(previousTransformation.getTransformationMatrix())
//            println(nextTransformation.getTransformationMatrix())
//            println()
//            println()
//            println()
//            }
            currentPose[jointName] = currentTransformation
        }
        if (print) {
            print = false
        }

        return currentPose
    }
}