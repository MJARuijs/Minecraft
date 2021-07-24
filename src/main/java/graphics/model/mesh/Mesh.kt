package graphics.model.mesh

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL45.*
import resources.Resource
import java.nio.ByteBuffer

open class Mesh(layout: Layout, vertices: ByteBuffer, indices: IntArray): Resource {

    private val vao = glCreateVertexArrays()
    private val vbo = glCreateBuffers()
    private val ebo = glCreateBuffers()

    private val code = layout.primitive.code
    private val count = indices.size

    init {
        glNamedBufferData(vbo, vertices.rewind(), GL_STATIC_DRAW)

        var offset = 0L

        for (attribute in layout.attributes) {
            glVertexArrayVertexBuffer(vao, attribute.location, vbo, offset, layout.stride)
            glEnableVertexArrayAttrib(vao, attribute.location)

            if (attribute.dataType == DataType.INT) {
                glVertexArrayAttribIFormat(vao, attribute.location, attribute.size, attribute.dataType.code, 0)
            } else {
                glVertexArrayAttribFormat(vao, attribute.location, attribute.size, attribute.dataType.code, false, 0)
            }
            offset += attribute.size * attribute.dataType.size
        }

        glNamedBufferData(ebo, indices, GL_STATIC_DRAW)
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