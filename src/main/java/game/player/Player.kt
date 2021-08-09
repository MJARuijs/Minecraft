package game.player

import graphics.entity.AnimatedEntity
import graphics.entity.Entity
import graphics.model.Model
import graphics.model.animation.model.AnimatedModel
import math.matrices.Matrix4

class Player(model: AnimatedModel, transformation: Matrix4) : AnimatedEntity(model, transformation)