package graphics.entity

import graphics.model.Model
import graphics.renderer.Renderable
import graphics.shaders.ShaderProgram
import math.matrices.Matrix4
import math.vectors.Vector3

abstract class Entity(protected var transformation: Matrix4): Renderable {

    abstract val model: Model

    abstract fun isAnimated(): Boolean

    fun getPosition() = transformation.getPosition()

//    fun getRotation() = transformation.getRotation()

    open fun update(delta: Float) {}

    open fun translate(translation: Vector3) {
        transformation = transformation.translate(translation)
    }

    open fun scale(scale: Vector3) {
        transformation = transformation.scale(scale)
    }

    open fun rotate(rotation: Vector3) {
        transformation = transformation.rotate(rotation)
    }
    override fun render(shaderProgram: ShaderProgram) {
        shaderProgram.set("model", Matrix4())
        model.render(shaderProgram)
    }

}