package graphics.model.animation

import graphics.model.Model
import graphics.model.Shape
import graphics.shaders.ShaderProgram

class AnimatedModel(shapes: List<Shape>, private val rootJoint: Bone): Model(shapes) {

    override fun render(shaderProgram: ShaderProgram) {
        rootJoint.loadTransformation(shaderProgram)

        super.render(shaderProgram)
    }
}