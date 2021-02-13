package devices

import org.lwjgl.glfw.GLFW.*

enum class Button(private val int: Int) {

    LEFT(GLFW_MOUSE_BUTTON_LEFT),
    RIGHT(GLFW_MOUSE_BUTTON_RIGHT),
    MIDDLE(GLFW_MOUSE_BUTTON_MIDDLE);

    companion object {

        fun fromInt(int: Int) = values().firstOrNull() {
            it.int == int
        }
    }
}