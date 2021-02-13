package devices

import org.lwjgl.glfw.GLFW.*

enum class Action(private val int: Int) {

    PRESS(GLFW_PRESS),
    RELEASE(GLFW_RELEASE),
    REPEAT(GLFW_REPEAT);

    companion object {

        fun fromInt(int: Int) = values().firstOrNull {
            it.int == int
        }
    }
}