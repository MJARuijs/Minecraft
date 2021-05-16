package devices

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL.createCapabilities
import org.lwjgl.system.MemoryUtil.NULL

class Window(title: String, private val onWindowResized: (Int, Int) -> Unit) {

    internal val handle: Long

    private val xOffset = 0

    var running = true
        private set

    var resized = true
        private set

    var width = 1280
        private set

    var height = 720
        private set

    val aspectRatio: Float
        get() = width.toFloat() / height.toFloat()

    val keyboard: Keyboard

    val mouse: Mouse

    init {

        GLFWErrorCallback.createPrint(System.err).set()

        if (!glfwInit()) {
            throw RuntimeException("Unable to initialize GLFW")
        }

        val monitor = glfwGetPrimaryMonitor()
        val video = glfwGetVideoMode(monitor)

        if (video != null) {
//            width = video.width() / 2
//            height = video.height() / 2
        }

        glfwDefaultWindowHints()
//        glfwWindowHint(GLFW_SAMPLES, 1)
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE)
//        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
//        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE)
        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)

        handle = glfwCreateWindow(width, height, title, NULL, NULL)
        if (handle == NULL) {
            throw RuntimeException("Failed to create GLFW window")
        }

        glfwSetWindowPos(handle, width / 4 + xOffset, height / 4)
        glfwSetWindowCloseCallback(handle) {
            running = false
        }

        glfwSetWindowSizeCallback(handle) { _, newWidth: Int, newHeight: Int ->
            resized = (newWidth != width) || (newHeight != height)
            width = newWidth
            height = newHeight
            onWindowResized(width, height)
        }

        keyboard = Keyboard(this)
        mouse = Mouse(this)

        glfwMakeContextCurrent(handle)
        glfwSwapInterval(1)
        createCapabilities()
    }
    
    fun poll() {
        resized = false
        mouse.moved = false
        
        glfwPollEvents()
    }
    
    fun synchronize() {
        glfwSwapBuffers(handle)
        mouse.update()
        keyboard.update()
    }

    fun resize(width: Int, height: Int) {
        glfwSetWindowSize(handle, width, height)
    }

    fun close() {
        glfwSetWindowShouldClose(handle, true)
    }

    fun isClosed(): Boolean {
        return glfwWindowShouldClose(handle)
    }

    fun destroy() {
        glfwDestroyWindow(handle)
        glfwTerminate()
    }
}
