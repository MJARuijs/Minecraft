package graphics.model

import graphics.model.mesh.Layout
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL30.glBindVertexArray

class TestMesh(val layout: Layout, val vertices: FloatArray, val indices: IntArray) {

    private val vao = GL30.glGenVertexArrays()
    private val vbo = GL15.glGenBuffers()
    private val ebo = GL15.glGenBuffers()

    private var ibo = 0

    private val code = layout.primitive.code
    private val count = indices.size

    init {

        GL30.glBindVertexArray(vao)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        var offset = 0
        for (attribute in layout.attributes) {
            GL20.glVertexAttribPointer(attribute.location, attribute.size, GL11.GL_FLOAT, false, 4 * layout.stride, 0)
            GL20.glEnableVertexAttribArray(attribute.location)
            offset += attribute.size
        }

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo)
        GL30.glBindVertexArray(0)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun draw() {
        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, count)
        glBindVertexArray(0)
    }

}