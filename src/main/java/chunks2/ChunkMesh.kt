package chunks2

import graphics.model.mesh.Layout
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*

class ChunkMesh(layout: Layout, vertices: FloatArray) {

    private val vao = glGenVertexArrays()
    private val vbo = glGenBuffers()

    private val count = vertices.size / 3

    init {
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        var offset = 0

        for (attribute in layout.attributes) {
            glVertexAttribPointer(attribute.location, attribute.size, GL_FLOAT, false, 4 * layout.stride, 4L * offset)
            glEnableVertexAttribArray(attribute.location)
            offset += attribute.size
        }

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    fun draw() {
        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, count)
        glBindVertexArray(0)
    }

    fun destroy() {
        glDeleteBuffers(vbo)
        glDeleteVertexArrays(vao)
    }
}