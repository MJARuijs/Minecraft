package chunks2

import chunks.blocks.BlockData
import chunks.blocks.Direction
import chunks.blocks.FaceData
import graphics.model.mesh.Attribute
import graphics.model.mesh.DataType
import graphics.model.mesh.Layout
import graphics.model.mesh.Primitive
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*

class ChunkMesh(layout: Layout, vertices: FloatArray, textureIndices: FloatArray) {

    private val vao = glGenVertexArrays()
    private val vbo = glGenBuffers()
    private val tbo = glGenBuffers()

    private val count = vertices.size / 3

    init {
        glBindVertexArray(vao)
        var offset = 0L

//        for (attribute in layout.attributes) {
//            glVertexAttribPointer(attribute.location, attribute.size, attribute.dataType.code, false, 4 * layout.stride, offset)
//            glEnableVertexAttribArray(attribute.location)
//            offset += attribute.dataType.size * attribute.size
//        }

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(0)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        glBindBuffer(GL_ARRAY_BUFFER, tbo)
        glBufferData(GL_ARRAY_BUFFER, textureIndices, GL_STATIC_DRAW)
        glVertexAttribPointer(1, 1, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(1)
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

    companion object {

        private val FACE_VERTICES = arrayOf(
                floatArrayOf(
                        0.0f, 1.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 1.0f,
                        1.0f, 0.0f, 1.0f,
                        1.0f, 1.0f, 1.0f
                ),
                floatArrayOf(
                        1.0f, 1.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 0.0f,
                        1.0f, 1.0f, 0.0f,
                        0.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f
                ),
                floatArrayOf(
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f,
                        0.0f, 1.0f, 0.0f,
                        1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, 0.0f
                ),
                floatArrayOf(
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 1.0f
                ),
                floatArrayOf(
                        1.0f, 1.0f, 1.0f,
                        1.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 1.0f, 1.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 1.0f, 0.0f
                ),
                floatArrayOf(
                        0.0f, 1.0f, 0.0f,
                        0.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 1.0f
                )
        )

        fun create(data: List<BlockData>): ChunkMesh {
            var vertices = FloatArray(0)
            var textureIndices = FloatArray(0)

            val visibleFaces = ArrayList<FaceData>()
            for (blockData in data) {
                val position = blockData.position

                for ((i, direction) in Direction.values().withIndex()) {
                    if (data.none { neighbourData -> neighbourData.position == position + direction.normal }) {
                        visibleFaces += FaceData(position, direction, blockData.type.textureIndices[i])
                    }
                }
            }

            for (visibleFace in visibleFaces) {
                val directionIndex = Direction.values().indexOf(visibleFace.direction)
                val faceVertices = FACE_VERTICES[directionIndex]
                for (i in faceVertices.indices step 3) {
                    vertices += faceVertices[i] + visibleFace.position.x
                    vertices += faceVertices[i + 1] + visibleFace.position.y
                    vertices += faceVertices[i + 2] + visibleFace.position.z
                    textureIndices += visibleFace.textureIndex.toFloat()
                }

            }

            val layout = Layout(Primitive.TRIANGLE, Attribute(0, 3), Attribute(1, 1))

            return ChunkMesh(layout, vertices, textureIndices)
        }

    }
}