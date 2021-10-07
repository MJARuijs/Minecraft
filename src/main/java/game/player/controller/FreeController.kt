package game.player.controller

import devices.Keyboard
import devices.Mouse
import game.camera.Camera
import math.matrices.Matrix4
import math.vectors.Vector3
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

class FreeController(private var position: Vector3 = Vector3()) : Controller() {

    private var rotation = Vector3()

    override fun update(camera: Camera, keyboard: Keyboard, mouse: Mouse, delta: Float) {
        super.update(camera, keyboard, mouse, delta)

        if (translation.length() > 0.0f) {
            val rotationMatrix = Matrix4().rotateY(-rotation.y)
            position += rotationMatrix.dot(-translation.unit()) * delta * moveSpeed
        }

        rotation.x += -mouse.dy.toFloat() * mouseSpeed
        rotation.x = min(max(-PI.toFloat() / 2.0f, rotation.x), PI.toFloat() / 2.0f)
        rotation.y += mouse.dx.toFloat() * mouseSpeed
        rotation %= 2.0f * PI.toFloat()

        camera.position = position
        camera.rotation = rotation
    }

}