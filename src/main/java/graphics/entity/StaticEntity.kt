package graphics.entity

import graphics.model.Model
import math.matrices.Matrix4

class StaticEntity(model: Model, transformation: Matrix4) : Entity(transformation) {

    override val model = (model)

    override fun isAnimated() = false

}