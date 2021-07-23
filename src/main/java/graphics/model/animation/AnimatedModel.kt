package graphics.model.animation

import graphics.model.Model
import graphics.model.Shape
import graphics.shaders.ShaderProgram
import math.matrices.Matrix4
import kotlin.math.PI

class AnimatedModel(shapes: List<Shape>, val rootJoint: Joint, private val poses: List<Pose>): Model(shapes) {

    private val animator = Animator(this)
    private val animations = ArrayList<Animation>()
//    private var currentPose = poses[0]

    var print = false

    init {
//        rootJoint.calculateWorldTransformation(Matrix4().rotateX(PI.toFloat() / -2.0f))
//        rootJoint.calculateWorldTransformation(Matrix4())
    }

    fun addAnimation(name: String, loop: Boolean, keyframes: List<Pair<Int, Int>>) {
        val frames = ArrayList<Pair<Int, Pose>>()
        for (pose in keyframes) {
            frames += Pair(pose.second, this.poses[pose.first])
        }
        animations += Animation(name, loop, frames)
    }

    fun update(delta: Float) {
        animator.update(delta)
    }

    fun animate(name: String) {
        for (animation in animations) {
            if (animation.name == name) {
                animator.startAnimation(animation)
                return
            }
        }
    }

    override fun render(shaderProgram: ShaderProgram) {
        rootJoint.loadTransformation(shaderProgram, print)

        if (print) {
            print = false
        }
        super.render(shaderProgram)
    }

    fun getJoints(): List<Joint> {
        return rootJoint.getJoints()
    }
}