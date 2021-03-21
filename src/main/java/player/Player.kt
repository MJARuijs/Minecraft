package player

import devices.Key
import devices.Keyboard
import devices.Mouse
import math.matrices.Matrix4
import math.vectors.Vector3
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

class Player(var position: Vector3 = Vector3(0, 18, 0), val height: Float = 1.8f) {

    var rotation = Vector3()
        private set

    private val actions = ArrayList<Action>()

    fun getYLevel() = position.y

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
//            if (actions.none { action -> action is Jump }) {
//                actions += Jump(getYLevel() + 1.1f)
//            }
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

//        performActions(delta)
    }

    private fun performActions(delta: Float) {
        for (action in actions) {
            action.perform(this, delta)
        }
    }

}