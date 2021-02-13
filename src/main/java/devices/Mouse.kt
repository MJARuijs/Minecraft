package devices

import org.lwjgl.glfw.GLFW.*
import java.util.*
import kotlin.collections.HashSet

class Mouse(private val window: Window) {

    init {

        glfwSetMouseButtonCallback(window.handle) { _, buttonInt: Int, actionInt: Int, _ ->

            val button = Button.fromInt(buttonInt)
            val action = Action.fromInt(actionInt)

            if (button != null && action != null) {
                val event = Event(button, action)
                events.push(event)
            }
        }
    
        glfwSetScrollCallback(window.handle) { _: Long, xScroll: Double, yScroll: Double ->
            this.xScroll = xScroll.toFloat()
            this.yScroll = yScroll.toFloat()
        }
        
        glfwSetCursorPosCallback(window.handle) { _, newX: Double, newY: Double ->

            val scaledX = (newX - window.width / 2) / window.width
            val scaledY = -(newY - window.height / 2) / window.height

            moved = (x != scaledX) || (y != scaledY)

            dx = scaledX - x
            dy = scaledY - y

            x = scaledX
            y = scaledY
        }

        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    }

    private data class Event(val button: Button, val action: Action)

    private val events = ArrayDeque<Event>()

    private val pressed = HashSet<Button>()
    private val released = HashSet<Button>()
    private val down = HashSet<Button>()

    var x = 0.0
        internal set

    var y = 0.0
        internal set

    var dx = 0.0
        internal set

    var dy = 0.0
        internal set

    var xScroll = 0.0f
        private set
    
    var yScroll = 0.0f
        private set
    
    var moved = false
        internal set

    var captured = false
        internal set

    fun isPressed(button: Button) = pressed.contains(button)

    fun isReleased(button: Button) = released.contains(button)

    fun isDown(button: Button) = down.contains(button)

    internal fun post(button: Button, action: Action) = events.push(Event(button, action))

    fun capture() {
        captured = true
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    }

    fun release() {
        captured = false
        glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
    }

    fun toggle() {
        if (captured) {
            release()
        } else {
            capture()
        }
    }
    
    fun update() {
        pressed.clear()
        released.clear()
        
        xScroll = 0.0f
        yScroll = 0.0f
    
        while (events.isNotEmpty()) {
            val event = events.pop()
            when (event.action) {
            
                Action.PRESS -> {
                    pressed.add(event.button)
                    down.add(event.button)
                }
            
                Action.RELEASE -> {
                    released.add(event.button)
                    down.remove(event.button)
                }
            
                Action.REPEAT -> {
                    // ignore
                }
            }
        }
    }
}