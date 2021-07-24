package graphics.entity

import graphics.model.Model
import graphics.model.animation.AnimatedModel
import graphics.renderer.Renderable
import graphics.shaders.ShaderProgram
import math.matrices.Matrix4

class Entity(private val model: Model, private var transformation: Matrix4): Renderable {

    fun isAnimated() = model is AnimatedModel

    fun animate(name: String) {
        if (model is AnimatedModel) {
            model.animate(name)
        }
    }

    fun update(delta: Float) {
        if (model is AnimatedModel) {
            model.update(delta)
        }
    }

    override fun render(shaderProgram: ShaderProgram) {
        shaderProgram.set("model", transformation)
        model.render(shaderProgram)
    }

}