package graphics.entity

import graphics.model.Model
import graphics.shaders.ShaderProgram
import math.matrices.Matrix4

class StaticEntity(override val model: Model, transformation: Matrix4) : Entity(transformation) {

    override fun isAnimated() = false

    override fun render(shaderProgram: ShaderProgram) {
        shaderProgram.set("model", transformation)
        super.render(shaderProgram)
    }

}