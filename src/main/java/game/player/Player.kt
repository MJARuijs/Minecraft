package game.player

import graphics.entity.AnimatedEntity
import graphics.entity.Entity
import graphics.model.Model
import graphics.model.animation.model.AnimatedModel
import math.matrices.Matrix4
import math.vectors.Vector3

class Player(model: AnimatedModel, transformation: Matrix4) : AnimatedEntity(model, transformation) {

    private var walking = false
    private var crouching = false

    fun isWalking() = walking

    fun isCrouching() = crouching

    fun walk(translation: Vector3) {
        println("walking")
        if (!walking) {
            animate("walking")
            walking = true
        }

        translate(translation)
    }

    fun stopWalking() {
        println("stop walking")
        walking = false
        stopAnimating(250)
    }

    fun turn(rotation: Vector3) {
        println("turning")
        rotate(rotation)
    }

    fun crouch() {
        println("crouching")
        crouching = true
    }

    fun standUp() {
        println("standing up")
        crouching = false
    }

    fun mine() {
        println("mining")
    }

}