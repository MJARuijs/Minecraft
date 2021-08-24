package game.player

import devices.Key
import devices.Keyboard
import devices.Mouse
import graphics.Camera
import math.vectors.Vector3
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

class Controller(private val player: Player, private val camera: Camera) {

    fun update(keyboard: Keyboard, mouse: Mouse, delta: Float) {
        val translation = Vector3()

        val mouseSpeed = 1.75f

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

//        if (translation.length() > 0.0f) {
//            val rotationMatrix = Matrix4().rotateY(-rotation.y)
//            position += rotationMatrix.dot(-translation.unit()) * delta * moveSpeed
//        }

        val rotation = Vector3()

        rotation.x = (-mouse.y.toFloat() * mouseSpeed) % (2.0f * PI.toFloat())
        rotation.x = min(max(-PI.toFloat() / 2.0f, rotation.x), PI.toFloat() / 2.0f)
        rotation.y = (mouse.x.toFloat() * mouseSpeed) % (2.0f * PI.toFloat())

        player.walk(translation)
        player.turn(rotation)

        camera.place(player.getPosition())
//        camera.rotate(player.getRotation())
    }

    fun updatePlayer() {

    }

    fun updateCamera() {

    }

}
