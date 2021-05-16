package environment.terrain.chunks

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.opengl.GL45.*
import java.nio.ByteBuffer

class ChunkMesh(vertices: ByteBuffer, vertexCount: Int) {

    private var vao = 0
    private var vbo = 0
    private var count = vertexCount

    init {
        vao = glCreateVertexArrays()
        vbo = glCreateBuffers()

        glNamedBufferData(vbo, vertices.rewind(), GL_DYNAMIC_DRAW)

        glVertexArrayVertexBuffer(vao, 0, vbo, 0, 24)
        glVertexArrayAttribFormat(vao, 0, 3, GL_FLOAT, false, 0)
        glEnableVertexArrayAttrib(vao, 0)

        glVertexArrayVertexBuffer(vao, 1, vbo, 12, 24)
        glVertexArrayAttribIFormat(vao, 1, 1, GL_INT, 0)
        glEnableVertexArrayAttrib(vao, 1)

        glVertexArrayVertexBuffer(vao, 2, vbo, 16, 24)
        glVertexArrayAttribIFormat(vao, 2, 1, GL_INT, 0)
        glEnableVertexArrayAttrib(vao, 2)

        glVertexArrayVertexBuffer(vao, 3, vbo, 20, 24)
        glVertexArrayAttribIFormat(vao, 3, 1, GL_INT, 0)
        glEnableVertexArrayAttrib(vao, 3)
    }

    fun updateInstanceData(data: ByteBuffer, vertexCount: Int) {
        count = vertexCount
        glNamedBufferData(vbo, data.rewind(), GL_DYNAMIC_DRAW)
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