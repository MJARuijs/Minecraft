package game.player.controller

import devices.Key
import devices.Keyboard
import devices.Mouse
import game.camera.Camera
import math.vectors.Vector3

abstract class Controller() {

    val mouseSpeed = 1.75f
    val moveSpeed = 5.0f

    var moveSpeedAmplifier = 1.0f

    var translation = Vector3()
        private set

    open fun update(camera: Camera, keyboard: Keyboard, mouse: Mouse, delta: Float) {
        translation = Vector3()

        moveSpeedAmplifier = if (keyboard.isDown(Key.LEFT_CONTROL)) {
            5.0f
        } else {
            1.0f
        }

        if (keyboard.isDown(Key.W)) {
            translation.z += 1.0f
        }

        if (keyboard.isDown(Key.S)) {
            translation.z -= 1.0f
        }

        if (keyboard.isDown(Key.D)) {
            translation.x -= 1.0f
        }

        if (keyboard.isDown(Key.A)) {
            translation.x += 1.0f
        }

        if (keyboard.isDown(Key.SPACE)) {
            translation.y -= 1.0f
        }

        if (keyboard.isDown(Key.LEFT_SHIFT)) {
            translation.y += 1.0f
        }
    }
}