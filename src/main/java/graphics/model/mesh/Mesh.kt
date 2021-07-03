package graphics.model.mesh

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*
import resources.Resource

open class Mesh(layout: Layout, vertices: FloatArray, indices: IntArray): Resource {

    private val vao = glGenVertexArrays()
    private val vbo = glGenBuffers()
    private val ebo = glGenBuffers()

    private val code = layout.primitive.code
    private val count = indices.size

    init {
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        var offset = 0

        for (attribute in layout.attributes) {
            glVertexAttribPointer(attribute.location, attribute.size, GL_FLOAT, false, 4 * layout.stride, 4L * offset)
            glEnableVertexAttribArray(attribute.location)
            offset += attribute.size
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    fun draw() {
        glBindVertexArray(vao)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glDrawElements(code, count, GL_UNSIGNED_INT, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    override fun destroy() {
        glDeleteBuffers(ebo)
        glDeleteBuffers(vbo)
        glDeleteVertexArrays(vao)
    }
}