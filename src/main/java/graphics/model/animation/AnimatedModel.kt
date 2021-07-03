package graphics.model.animation

import graphics.model.Model
import graphics.model.Shape

class AnimatedModel(shapes: List<Shape>, private val rootJoint: Bone): Model(shapes)