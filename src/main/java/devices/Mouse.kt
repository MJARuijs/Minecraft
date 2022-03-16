package devices

import messages.Message
import messages.MessageClient
import messages.MessageTopics
import org.lwjgl.glfw.GLFW.*
import java.util.*
import kotlin.collections.HashSet

class Mouse(private val windowHandle: Long, windowWidth: Int, windowHeight: Int) : MessageClient() {

    init {

        glfwSetMouseButtonCallback(windowHandle) { _, buttonInt: Int, actionInt: Int, _ ->

            val button = Button.fromInt(buttonInt)
            val action = Action.fromInt(actionInt)

            if (button != null && action != null) {
                val event = Event(button, action)
                events.push(event)
            }

        }
    
        glfwSetScrollCallback(windowHandle) { _: Long, xScroll: Double, yScroll: Double ->
            this.xScroll = xScroll.toFloat()
            this.yScroll = yScroll.toFloat()

            data["xScroll"] = xScroll
            data["yScroll"] = yScroll

//            sendMessage(Message(MessageTopics.MOUSE_INPUT, data))
        }
        
        glfwSetCursorPosCallback(windowHandle) { _, xPixel: Double, yPixel: Double ->

            val scaledX = (xPixel - windowWidth / 2) / windowWidth
            val scaledY = -(yPixel - windowHeight / 2) / windowHeight

            moved = (x != scaledX) || (y != scaledY)

            dx = scaledX - x
            dy = scaledY - y

            x = scaledX
            y = scaledY

            data["x"] = scaledX
            data["y"] = scaledY

//            sendMessage(Message(MessageTopics.MOUSE_INPUT, data))
        }

        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
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
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    }

    fun release() {
        captured = false
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
    }

    fun toggle() {
        if (captured) {
            release()
        } else {
            capture()
        }
    }

    fun isCaptured() = captured

    fun update() {
        pressed.clear()
        released.clear()
        
        xScroll = 0.0f
        yScroll = 0.0f

        dx = 0.0
        dy = 0.0

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

        data["pressed"] = pressed
        data["down"] = down
        data["released"] = released

//        sendMessage(Message(MessageTopics.MOUSE_INPUT, data))
    }

    override fun receiveMessage(message: Message) {}

}