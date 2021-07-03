package graphics.model.mesh

import math.matrices.Matrix3
import math.matrices.Matrix4
import math.vectors.Vector2
import math.vectors.Vector3
import org.lwjgl.assimp.*
import resources.Loader
import util.File
import java.nio.charset.StandardCharsets

class MeshLoader: Loader<Mesh> {

    private val boneIds = HashMap<String, Pair<Int, Matrix4>>()

    override fun load(path: String): Mesh {
        val scene = loadScene(path)
        val aiMeshes = scene.mMeshes()
        return parseData(AIMesh.create(aiMeshes!!.get(0)), Matrix4(), false)
    }

    private fun loadScene(path: String) = Assimp.aiImportFile(
            File(path).getPath(),
            Assimp.aiProcess_Triangulate or Assimp.aiProcess_OptimizeGraph or Assimp.aiProcess_RemoveRedundantMaterials
    ) ?: throw Exception("Could not load scene: $path")

    fun parseData(aiMesh: AIMesh, transformation: Matrix4, isRigged: Boolean): Mesh {
        var containsTexCoords = false
        var containsNormals = false

        var vertices = FloatArray(0)
        var indices = IntArray(0)

        val aiVertices = aiMesh.mVertices()
        val aiTexCoords = aiMesh.mTextureCoords(0)
        val aiNormals = aiMesh.mNormals()
//        val aiBones = aiMesh.mBones()


        for (i in 0 until aiMesh.mNumVertices()) {

            val aiVertex = aiVertices.get()
            val aiTexture = aiTexCoords?.get()
            val aiNormal = aiNormals?.get()

            val position = transformation.dot(Vector3(aiVertex.x(), aiVertex.y(), aiVertex.z()))
            vertices += position.x
            vertices += position.y
            vertices += position.z

            if (aiTexture != null) {
                val texCoord = Vector2(aiTexture.x(), aiTexture.y())
                vertices += texCoord.x
                vertices += texCoord.y

                containsTexCoords = true
            }

            if (aiNormal != null) {
                val normal = Matrix3(transformation).dot(Vector3(aiNormal.x(), aiNormal.y(), aiNormal.z()))
                vertices += normal.x
                vertices += normal.y
                vertices += normal.z

                containsNormals = true
            }

            if (aiMesh.mNumBones() > 0) {
                val bone = AIBone.create(aiMesh.mBones()!!.get())

                val boneName = StandardCharsets.UTF_8.decode(bone.mName().data()).toString()
                if (!boneIds.containsKey(boneName)) {
                    boneIds[boneName] = Pair(boneIds.size, parseMatrix(bone.mOffsetMatrix()))
                }

                for (weight in bone.mWeights()) {
                    if (weight.mVertexId() == i) {
                        vertices += boneIds[boneName]!!.first.toFloat()
                        vertices += weight.mWeight()
                    }
                }
//                println()
            }
//            println(i)
        }

        for (i in 0 until aiMesh.mNumFaces()) {
            val face = aiMesh.mFaces().get(i)

            val intBuffer = face.mIndices()

            while (intBuffer.remaining() > 0) {
                val index = intBuffer.get()
                indices += index
            }
        }

        val attributes = arrayListOf(Attribute(0, 3))

        if (containsTexCoords) {
            attributes += Attribute(1, 2)
        }
        if (containsNormals) {
            attributes += Attribute(2, 3)
        }
        if (isRigged) {
            attributes += Attribute(3, 4)
            attributes += Attribute(4, 4)
        }

        for (bone in boneIds) {
//            println("${bone.key} :: ${bone.value.second}")
//            println()
        }

        val layout = Layout(Primitive.TRIANGLE, attributes)
        return Mesh(layout, vertices, indices)
    }

    private fun parseMatrix(aiMatrix: AIMatrix4x4): Matrix4 {
        return Matrix4(floatArrayOf(
                aiMatrix.a1(), aiMatrix.a2(), aiMatrix.a3(), aiMatrix.a4(),
                aiMatrix.b1(), aiMatrix.b2(), aiMatrix.b3(), aiMatrix.b4(),
                aiMatrix.c1(), aiMatrix.c2(), aiMatrix.c3(), aiMatrix.c4(),
                aiMatrix.d1(), aiMatrix.d2(), aiMatrix.d3(), aiMatrix.d4())
        )
    }

}