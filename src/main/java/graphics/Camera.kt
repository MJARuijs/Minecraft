package graphics

import devices.Key
import devices.Keyboard
import devices.Mouse
import math.matrices.Matrix4
import math.vectors.Vector3
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

class Camera(
        var fieldOfView: Float = 70.0f,
        var aspectRatio: Float = 1.0f,
        var zNear: Float = 0.01f,
        var zFar: Float = 1000.0f,

        var position: Vector3 = Vector3(),
        var rotation: Vector3 = Vector3()
) {

    val projectionMatrix: Matrix4
        get() = Matrix4(floatArrayOf(
                1.0f / (aspectRatio * tan((PI.toFloat() / 180.0f) * fieldOfView / 2.0f)), 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f / tan((PI.toFloat() / 180.0f) * fieldOfView / 2.0f), 0.0f, 0.0f,
                0.0f, 0.0f, -(zFar + zNear) / (zFar - zNear), -(2.0f * zFar * zNear) / (zFar - zNear),
                0.0f, 0.0f, -1.0f, 0.0f
        ))

    val viewMatrix: Matrix4
        get() = Matrix4()
            .rotate(rotation)
            .translate(-position)

    val rotationMatrix: Matrix4
        get() = Matrix4().rotateY(-rotation.y).rotateX(-rotation.x)

    fun update(keyboard: Keyboard, mouse: Mouse, delta: Float) {

        val translation = Vector3()

        val mouseSpeed = 1.75f
        var moveSpeed = 5.0f

        if (keyboard.isDown(Key.LEFT_CONTROL)) {
            moveSpeed = 100.0f
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

        if (translation.length() > 0.0f) {
            val rotationMatrix = Matrix4().rotateY(-rotation.y)
            position += rotationMatrix.dot(-translation.unit()) * delta * moveSpeed
        }

        rotation.x = (-mouse.y.toFloat() * mouseSpeed) % (2.0f * PI.toFloat())
        rotation.x = min(max(-PI.toFloat() / 2.0f, rotation.x), PI.toFloat() / 2.0f)
        rotation.y = (mouse.x.toFloat() * mouseSpeed) % (2.0f * PI.toFloat())
    }

    fun place(position: Vector3) {
        this.position = position
    }

    fun rotate(rotation: Vector3) {
        this.rotation = rotation
    }
}