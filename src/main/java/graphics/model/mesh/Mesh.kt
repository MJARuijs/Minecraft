package graphics.model.mesh

import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL11.GL_UNSIGNED_INT
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL31.glDrawElementsInstanced
import org.lwjgl.opengl.GL33.glVertexAttribDivisor
import resources.Resource

open class Mesh(layout: Layout, vertices: FloatArray, indices: IntArray): Resource {

    private val vao = glGenVertexArrays()
    private val vbo = glGenBuffers()
    private val ebo = glGenBuffers()

    private var ibo = 0

    private val code = layout.primitive.code
    private val count = indices.size

    init {

        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        var offset = 0
        for (attribute in layout.attributes) {
            glVertexAttribPointer(attribute.location, attribute.size, GL_FLOAT, false, 4 * layout.stride, 4L * offset)
            glEnableVertexAttribArray(attribute.location)
            offset += attribute.size
        }

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBindVertexArray(0)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun initInstancedBuffers(instancedLayout: Layout) {
        ibo = glGenBuffers()
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, ibo)

        var instancedOffset = 0
        for (attribute in instancedLayout.attributes) {
            glVertexAttribPointer(attribute.location, attribute.size, GL_FLOAT, false, 4 * instancedLayout.stride, 4L * instancedOffset)
            glVertexAttribDivisor(attribute.location, 1)
            glEnableVertexAttribArray(attribute.location)
            instancedOffset += attribute.size
        }

        glBindVertexArray(0)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    fun updateInstanceData(data: FloatArray) {
        glBindBuffer(GL_ARRAY_BUFFER, ibo)
        glBufferData(GL_ARRAY_BUFFER, data, GL_STREAM_DRAW)
        glBufferSubData(GL_ARRAY_BUFFER, 0, data)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    fun draw(instances: Int = 1) {
        glBindVertexArray(vao)
        glDrawElementsInstanced(code, count, GL_UNSIGNED_INT, 0, instances)
        glBindVertexArray(0)
    }

    override fun destroy() {
        glDeleteBuffers(ebo)
        glDeleteBuffers(vbo)
        if (ibo != 0) {
            glDeleteBuffers(ibo)
        }
        glDeleteVertexArrays(vao)
    }
}