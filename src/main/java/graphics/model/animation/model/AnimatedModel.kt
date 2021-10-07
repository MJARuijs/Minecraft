package graphics.model.animation.model

import graphics.model.Model
import graphics.model.Shape
import graphics.model.animation.*
import graphics.shaders.ShaderProgram
import math.matrices.Matrix4
import math.vectors.Vector3

data class AnimatedModel(private val shapes: List<Shape>, val rootJoint: Joint, private val poses: List<Pose>, private val animations: ArrayList<Animation> = ArrayList()): Model(shapes) {

    constructor(model: AnimatedModel) : this(model.shapes, model.rootJoint.copy(), model.poses, model.animations)

    private val animator = Animator(this)

    fun addAnimation(name: String, keyframes: List<Pair<Int, Int>>, loopEffect: LoopEffect = LoopEffect.NONE): AnimatedModel {
        val frames = ArrayList<KeyFrame>()
        var totalTime = 0
        for (pose in keyframes) {
            frames += KeyFrame(totalTime + pose.second, poses[pose.first])
            totalTime += pose.second
        }
        animations += Animation(name, frames, loopEffect)
        return this
    }

    fun update(delta: Float, transformation: Matrix4) {
        animator.update(delta, transformation)
    }

    fun animate(name: String) {
        for (animation in animations) {
            if (animation.name == name) {
                animator.startAnimation(animation)
                return
            }
        }
        println("No animation with name $name was found for this model..")
    }

    fun toggleAnimation() {
        animator.toggleAnimation()
    }

    fun stopAnimation() {
        animator.stopAnimation()
    }

    fun stopAnimating(transitionDuration: Int) {
        animator.stopAnimating(transitionDuration)
    }

    fun translateTo(transformation: Matrix4) {
        rootJoint.initWorldTransformation(transformation)
    }

    fun rotate(transformation: Matrix4) {
        rootJoint.initWorldTransformation(transformation)
    }

    fun scale(scale: Vector3) {
        rootJoint.initWorldTransformation(Matrix4().scale(scale))
    }

    fun getJoints(): List<Joint> {
        return rootJoint.getJoints()
    }

    override fun render(shaderProgram: ShaderProgram) {
        rootJoint.loadTransformation(shaderProgram)
        super.render(shaderProgram)
    }

}