package chunks2

import chunks.blocks.BlockData
import chunks.blocks.Direction
import chunks.blocks.FaceData
import graphics.model.mesh.Attribute
import graphics.model.mesh.Layout
import graphics.model.mesh.Primitive
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_STATIC_DRAW
import org.lwjgl.opengl.GL15.glDeleteBuffers
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.opengl.GL45.*
import java.nio.ByteBuffer

class ChunkMesh(layout: Layout, vertices: FloatArray, textureIndices: FloatArray) {

    private val vao = glCreateVertexArrays()
    private val vbo = glCreateBuffers()
    private val tbo = glCreateBuffers()

    private val count = vertices.size

    init {
        glNamedBufferData(vbo, vertices, GL_STATIC_DRAW)
        glNamedBufferData(tbo, textureIndices, GL_STATIC_DRAW)

        glVertexArrayVertexBuffer(vao, 0, vbo, 0, 12)
        glVertexArrayAttribFormat(vao, 0, 3, GL_FLOAT, false, 0)
        glEnableVertexArrayAttrib(vao, 0)

        glVertexArrayVertexBuffer(vao, 1, tbo, 0, 4)
        glVertexArrayAttribFormat(vao, 1, 1, GL_FLOAT, false, 0)
        glEnableVertexArrayAttrib(vao, 1)
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

            val bytes = ByteBuffer.allocate(36)

            bytes.putFloat(0.0f)
            bytes.putFloat(0.0f)
            bytes.putFloat(0.0f)

            bytes.putFloat(1.0f)
            bytes.putFloat(0.0f)
            bytes.putFloat(0.0f)

            bytes.putFloat(1.0f)
            bytes.putFloat(1.0f)
            bytes.putFloat(0.0f)

            val ints = intArrayOf(
                    0, 0, 0,
                    1, 0, 0,
                    1, 1, 0
            )
//            println(bytes.size)
//            bytes += 1.toByte()
//            bytes += 0.toByte()
//            bytes += 0.toByte()
//            bytes += 1.toByte()
//            bytes += 1.toByte()
//            bytes += 0.toByte()


            val layout = Layout(Primitive.TRIANGLE, Attribute(0, 3))

            return ChunkMesh(layout, vertices, textureIndices)
        }

    }
}