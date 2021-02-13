package devices

import org.lwjgl.glfw.GLFW.glfwGetTime

/**
 * Construct w timer instance that, hold-it, keeps track of time...
 */
class Timer {

    private var initial = glfwGetTime().toFloat()
    private var current = 0.0f
    private var delta = 0.1f

    /**
     * @return the time elapsed since construction or the last reset call, iff reset was ever called.
     */
    fun getCurrent() = current

    /**
     * @return the time delta since the last update call.
     */
    fun getDelta() = delta

    /**
     * Update the timer values.
     */
    fun update() {
        val old = current
        current = glfwGetTime().toFloat() - initial
        delta = current - old
    }

    /**
     * Reset the current and delta time values.
     */
    fun reset() {
        initial = glfwGetTime().toFloat()
        current = 0.0f
        delta = 0.1f
    }

}