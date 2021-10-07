package game.player.controller

import devices.Keyboard
import devices.Mouse
import game.player.Player
import game.camera.Camera
import math.matrices.Matrix4
import math.vectors.Vector3
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

class FirstPersonController(private val player: Player) : Controller() {

    private val rotation = Vector3()

    override fun update(camera: Camera, keyboard: Keyboard, mouse: Mouse, delta: Float) {
        super.update(camera, keyboard, mouse, delta)

//        if (translation.length() > 0.0f) {
//            val rotationMatrix = Matrix4().rotateY(-rotation.y)
//            position += rotationMatrix.dot(-translation.unit()) * delta * moveSpeed
//        }

        rotation.x += -mouse.dy.toFloat() * mouseSpeed
        rotation.x = min(max(-PI.toFloat() / 2.0f, rotation.x), PI.toFloat() / 2.0f)
        rotation.y += mouse.dx.toFloat() * mouseSpeed
        rotation %= 2.0f * PI.toFloat()

        updatePlayer(translation, rotation)

//        camera.place(player.getPosition())
//        camera.viewMatrix = Matrix4().translate(-(player.getPosition() + Vector3(0f, 1.6f, 0f)))
//        camera.viewMatrix = player.getRotation() dot Matrix4().translate(-player.getPosition())
    }

    private fun updatePlayer(translation: Vector3, rotation: Vector3) {
        if (translation.length() != 0f) {
//            player.walk(translation)
        }

        if (player.isWalking() && translation.length() == 0f) {
//            player.stopWalking()
        }

        if (rotation.length() != 0f) {
            player.turn(rotation)
        }
    }

    fun updateCamera() {

    }

}
