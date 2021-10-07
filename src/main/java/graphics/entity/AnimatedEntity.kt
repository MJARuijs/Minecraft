package graphics.entity

import graphics.model.animation.model.AnimatedModel
import graphics.shaders.ShaderProgram
import math.matrices.Matrix4
import math.vectors.Vector3

open class AnimatedEntity(final override val model: AnimatedModel, transformation: Matrix4) : Entity(transformation) {

    init {
        this.model.translateTo(transformation)
    }

    override fun isAnimated() = true

    fun animate(name: String) {
        model.animate(name)
    }

    fun toggleAnimation() {
        model.toggleAnimation()
    }

    fun stopAnimation() {
        model.stopAnimation()
    }

    fun stopAnimating(transitionDuration: Int) {
        model.stopAnimating(transitionDuration)
    }

    override fun translate(translation: Vector3) {
        super.translate(translation)
        model.translateTo(transformation)
    }

    override fun scale(scale: Vector3) {
        super.scale(scale)
        model.scale(scale)
    }

    override fun rotate(rotation: Vector3) {
        super.rotate(rotation)
        model.rotate(transformation)
        // TODO: rotate model as-well
    }

    override fun update(delta: Float) {
        model.update(delta, transformation)
    }

    override fun render(shaderProgram: ShaderProgram) {
//        shaderProgram.set("model", Matrix4())
        super.render(shaderProgram)
    }

}