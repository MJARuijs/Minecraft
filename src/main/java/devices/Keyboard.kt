package devices

import org.lwjgl.glfw.GLFW.glfwSetKeyCallback
import java.util.*
import kotlin.collections.HashSet

class Keyboard(window: Window) {

    init {

        glfwSetKeyCallback(window.handle) { _, keyInt: Int, _, actionInt: Int, _ ->

            val key = Key.fromInt(keyInt)
            val action = Action.fromInt(actionInt)

            if (key != null && action != null) {
                val event = Event(key, action)
                events.push(event)
            }
        }
    }

    private data class Event(val key: Key, val action: Action)

    private val events = ArrayDeque<Event>()

    private val pressed = HashSet<Key>()
    private val released = HashSet<Key>()
    private val repeated = HashSet<Key>()
    private val down = HashSet<Key>()

    fun isPressed(key: Key) = pressed.contains(key)

    fun isReleased(key: Key) = released.contains(key)

    fun isRepeated(key: Key) = repeated.contains(key)

    fun isDown(key: Key) = down.contains(key)

    fun update() {

        pressed.clear()
        released.clear()
        repeated.clear()

        while (events.isNotEmpty()) {
            val event = events.poll()
            when (event.action) {

                Action.PRESS -> {
                    pressed.add(event.key)
                    down.add(event.key)
                }

                Action.RELEASE -> {
                    released.add(event.key)
                    down.remove(event.key)
                }

                Action.REPEAT -> {
                    repeated.add(event.key)
                }
            }
        }
    }
}