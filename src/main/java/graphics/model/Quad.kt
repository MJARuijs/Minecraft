package graphics.model

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*

class Quad {

    private val vertices = floatArrayOf(
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f
    )

    private val indices = intArrayOf(
            0, 1, 2,
            0, 2, 3
    )

    private val vao = glGenVertexArrays()
    private val vbo = glGenBuffers()
    private val ebo = glGenBuffers()

    init {
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(0)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBindVertexArray(0)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun draw() {
        glBindVertexArray(vao)
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
    }

    fun destroy() {
        glDeleteBuffers(ebo)
        glDeleteBuffers(vbo)
        glDeleteVertexArrays(vao)
    }
}