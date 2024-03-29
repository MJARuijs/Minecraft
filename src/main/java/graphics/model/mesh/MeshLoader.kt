package graphics.model.mesh

import math.matrices.Matrix3
import math.matrices.Matrix4
import math.vectors.Vector2
import math.vectors.Vector3
import org.lwjgl.assimp.*
import resources.Loader
import util.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MeshLoader: Loader<Mesh> {

    override fun load(path: String): Mesh {
        val scene = loadScene(path)
        val aiMeshes = scene.mMeshes()
        return parseData(AIMesh.create(aiMeshes!!.get(0)), Matrix4())
    }

    private fun loadScene(path: String) = Assimp.aiImportFile(
            File(path).getPath(),
            Assimp.aiProcess_Triangulate or Assimp.aiProcess_OptimizeGraph or Assimp.aiProcess_RemoveRedundantMaterials
    ) ?: throw Exception("Could not load scene: $path")

    fun parseData(aiMesh: AIMesh, transformation: Matrix4): Mesh {
        var vertices = FloatArray(0)
        var indices = IntArray(0)

        val aiVertices = aiMesh.mVertices()
        val aiTexCoords = aiMesh.mTextureCoords(0)
        val aiNormals = aiMesh.mNormals()

        val containsTexCoords = aiTexCoords?.capacity() != null
        val containsNormals = aiNormals?.capacity() != null

        var stride = 3
        if (containsTexCoords) {
            stride += 2
        }
        if (containsNormals) {
            stride += 3
        }

        val attributes = arrayListOf(Attribute(0, 3))

        if (containsTexCoords) {
            attributes += Attribute(1, 2)
        }
        if (containsNormals) {
            attributes += Attribute(2, 3)
        }

        val layout = Layout(Primitive.TRIANGLE, attributes)
        val buffer = ByteBuffer.allocateDirect(aiMesh.mNumVertices() * layout.stride).order(ByteOrder.nativeOrder())

        for (i in 0 until aiMesh.mNumVertices()) {

            val aiVertex = aiVertices.get()
            val aiTexture = aiTexCoords?.get()
            val aiNormal = aiNormals?.get()

            val position = transformation.dot(Vector3(aiVertex.x(), aiVertex.y(), aiVertex.z()))
            vertices += position.x
            vertices += position.y
            vertices += position.z

            buffer.putFloat(position.x)
            buffer.putFloat(position.y)
            buffer.putFloat(position.z)

            if (aiTexture != null) {
                val texCoord = Vector2(aiTexture.x(), aiTexture.y())
                vertices += texCoord.x
                vertices += texCoord.y

                buffer.putFloat(texCoord.x)
                buffer.putFloat(texCoord.y)
            }

            if (aiNormal != null) {
                val normal = Matrix3(transformation).dot(Vector3(aiNormal.x(), aiNormal.y(), aiNormal.z()))
                vertices += normal.x
                vertices += normal.y
                vertices += normal.z

                buffer.putFloat(normal.x)
                buffer.putFloat(normal.y)
                buffer.putFloat(normal.z)
            }
        }

        for (i in 0 until aiMesh.mNumFaces()) {
            val face = aiMesh.mFaces().get(i)

            val intBuffer = face.mIndices()

            while (intBuffer.remaining() > 0) {
                val index = intBuffer.get()
                indices += index
            }
        }

        return Mesh(layout, buffer, indices)
    }
}