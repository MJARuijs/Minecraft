package chunks2

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

        glVertexArrayVertexBuffer(vao, 0, vbo, 0, 16)
        glVertexArrayAttribFormat(vao, 0, 3, GL_FLOAT, false, 0)
        glEnableVertexArrayAttrib(vao, 0)

        glVertexArrayVertexBuffer(vao, 1, vbo, 12, 16)
        glVertexArrayAttribIFormat(vao, 1, 1, GL_INT, 0)
        glEnableVertexArrayAttrib(vao, 1)
    }

    fun updateInstanceData(data: ByteBuffer) {
        glNamedBufferData(vbo, data.rewind(), GL_DYNAMIC_DRAW)

        glVertexArrayVertexBuffer(vao, 0, vbo, 0, 16)
        glVertexArrayAttribFormat(vao, 0, 3, GL_FLOAT, false, 0)
        glEnableVertexArrayAttrib(vao, 0)

        glVertexArrayVertexBuffer(vao, 1, vbo, 12, 16)
        glVertexArrayAttribIFormat(vao, 1, 1, GL_INT, 0)
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

//    companion object {
//
//        fun create(data: List<BlockData>): ChunkMesh {
//            val visibleFaces = ArrayList<FaceData>()
//
//            for (blockData in data) {
//                val position = blockData.position
//
//                for ((i, direction) in FaceDirection.values().withIndex()) {
//                    if (data.none { neighbourData -> neighbourData.position == position + direction.normal }) {
//                        visibleFaces += FaceData(position, direction, blockData.type.textureIndices[i])
//                    }
//                }
//            }
//
//            var vertices = FloatArray(0)
//            var textureIndices = IntArray(0)
//
//            for (visibleFace in visibleFaces) {
//                val faceVertices = visibleFace.direction.vertices
//                for (i in faceVertices.indices step 3) {
//                    vertices += faceVertices[i] + visibleFace.position.x
//                    vertices += faceVertices[i + 1] + visibleFace.position.y
//                    vertices += faceVertices[i + 2] + visibleFace.position.z
//                    textureIndices += visibleFace.textureIndex
//                }
//            }
//
//            return ChunkMesh(vertices, textureIndices)
//        }
//    }
}