import graphics.model.mesh.Layout
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL45
import java.nio.ByteBuffer

class AdjacencyMesh(layout: Layout, vertices: ByteBuffer, indices: IntArray) {

    private val vao = GL45.glCreateVertexArrays()
    private val vbo = GL45.glCreateBuffers()
    private val ebo = GL45.glCreateBuffers()

    private val code = layout.primitive.code
    private val count = indices.size

    init {
        GL45.glNamedBufferData(vbo, vertices.rewind(), GL15.GL_STATIC_DRAW)

        var offset = 0L

        for (attribute in layout.attributes) {
            GL45.glVertexArrayVertexBuffer(vao, attribute.location, vbo, offset, layout.stride)
            GL45.glEnableVertexArrayAttrib(vao, attribute.location)
            GL45.glVertexArrayAttribFormat(vao, attribute.location, attribute.size, attribute.dataType.code, false, 0)
            offset += attribute.size * attribute.dataType.size
        }

        GL45.glNamedBufferData(ebo, indices, GL15.GL_STATIC_DRAW)
    }

    fun draw() {
        GL30.glBindVertexArray(vao)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo)
        GL11.glDrawElements(code, count, GL11.GL_UNSIGNED_INT, 0)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
        GL30.glBindVertexArray(0)
    }

    fun destroy() {
        GL15.glDeleteBuffers(ebo)
        GL15.glDeleteBuffers(vbo)
        GL30.glDeleteVertexArrays(vao)
    }
}