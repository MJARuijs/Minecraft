package graphics

import math.Color
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import java.util.concurrent.atomic.AtomicBoolean

object GraphicsContext {

    private val initialised = AtomicBoolean(false)
    private var width = 0
    private var height = 0

    fun init(background: Color = Color(), vararg options: GraphicsOption) {
        if (!initialised.getAndSet(true)) {

            GL.createCapabilities()
            glClearColor(background.r, background.g, background.b, 1.0f)
            glClearDepth(1.0)

            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            options.forEach(GraphicsOption::enable)
            GraphicsOption.values().filter(options::contains).forEach(GraphicsOption::enable)
        }
    }

    fun enable(vararg options: GraphicsOption) = options.forEach(GraphicsOption::enable)

    fun disable(vararg options: GraphicsOption) = options.forEach(GraphicsOption::disable)

    fun clear(vararg options: GraphicsOption) {
        var bit = 0
        for (option in options) {
            bit = bit or option.index
        }

        glClear(bit)
    }

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        glViewport(0, 0, width, height)
    }
}