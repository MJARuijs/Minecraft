package graphics.model

import Triangle
import Vertex
import VertexData
import graphics.model.mesh.Attribute
import graphics.model.mesh.Layout
import graphics.model.mesh.Mesh
import graphics.model.mesh.Primitive
import math.matrices.Matrix3
import math.matrices.Matrix4
import math.vectors.Vector2
import math.vectors.Vector3
import org.lwjgl.assimp.AIMesh
import org.lwjgl.assimp.Assimp
import resources.Loader
import util.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AdjacentMeshLoader: Loader<Mesh> {

    private val vertexMap = HashMap<VertexData, Int>()

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

        val containsTextureCoordinates = aiTexCoords?.capacity() != null
        val containsNormals = aiNormals?.capacity() != null

        val attributes = arrayListOf(Attribute(0, 3))
        var stride = 3
        if (containsTextureCoordinates) {
            stride += 2
            attributes += Attribute(2, 2)
        }
        if (containsNormals) {
            stride += 3
            attributes += Attribute(1, 3)
        }

        val layout = Layout(Primitive.TRIANGLE_ADJACENCY, attributes)
//        val buffer = ByteBuffer.allocateDirect(aiMesh.mNumVertices() * layout.stride).order(ByteOrder.nativeOrder())


        for (i in 0 until aiMesh.mNumVertices()) {

            val aiVertex = aiVertices.get()
            val aiTexture = aiTexCoords?.get()
            val aiNormal = aiNormals?.get()

            val position = transformation.dot(Vector3(aiVertex.x(), aiVertex.y(), aiVertex.z()))
            vertices += position.x
            vertices += position.y
            vertices += position.z

//            var normal: Vector3? = null
//            var textureCoordinate: Vector2? = null

            if (aiNormal != null) {
                val normal = Matrix3(transformation).dot(Vector3(aiNormal.x(), aiNormal.y(), aiNormal.z()))
                vertices += normal.x
                vertices += normal.y
                vertices += normal.z
            }
            if (aiTexture != null) {
                val textureCoordinate = Vector2(aiTexture.x(), aiTexture.y())
                vertices += textureCoordinate.x
                vertices += textureCoordinate.y
            }
        }

        for (i in 0 until aiMesh.mNumFaces()) {
            val face = aiMesh.mFaces().get(i)

            val intBuffer = face.mIndices()

            while (intBuffer.remaining() > 0) {
                val index = intBuffer.get()
                indices += index
                val vertexData = VertexData(vertices.copyOfRange(index * stride, index * stride + stride), containsNormals, containsTextureCoordinates)
                vertexMap[vertexData] = index

//                println("${vertexData.getPosition()} ${vertexData.getNormal()} ${index} ${index + stride} $i")
//                println(index)
            }
        }

//        for (i in vertices.indices step stride) {
//            val newIndex = indices.distinct().size
//            indices += newIndex
//
//            val vertexData = VertexData(vertices.copyOfRange(i, i + stride), containsNormals, containsTextureCoordinates)
//            vertexMap[vertexData] = newIndex
//        }

        val triangles = ArrayList<Triangle>()

        for (i in indices.indices step 3) {
            val v1Index = indices[i]
            val v2Index = indices[i + 1]
            val v3Index = indices[i + 2]

            val position1 = Vector3(vertices[v1Index * stride], vertices[v1Index * stride + 1], vertices[v1Index * stride + 2])
            val position2 = Vector3(vertices[v2Index * stride], vertices[v2Index * stride + 1], vertices[v2Index * stride + 2])
            val position3 = Vector3(vertices[v3Index * stride], vertices[v3Index * stride + 1], vertices[v3Index * stride + 2])

            val normal1 = Vector3(vertices[v1Index * stride + 3], vertices[v1Index * stride + 4], vertices[v1Index * stride + 5])
            val normal2 = Vector3(vertices[v2Index * stride + 3], vertices[v2Index * stride + 4], vertices[v2Index * stride + 5])
            val normal3 = Vector3(vertices[v3Index * stride + 3], vertices[v3Index * stride + 4], vertices[v3Index * stride + 5])

            val v1 = Vertex(position1, normal1)
            val v2 = Vertex(position2, normal2)
            val v3 = Vertex(position3, normal3)

            triangles += Triangle(v1, v2, v3)
        }

        val buffer = ByteBuffer.allocateDirect(triangles.size * stride * 3 * 4).order(ByteOrder.nativeOrder())
        for (triangle in triangles) {
            for (value in triangle.toArray()) {
                buffer.putFloat(value)
            }
        }

        var allIndices = IntArray(0)

        for ((triangleIndex, i) in (indices.indices step 3).withIndex()) {
            val triangle = triangles[triangleIndex]
            val adjacentIndices = findAdjacentIndices(triangles, triangle)

            allIndices += indices[i]
            allIndices += adjacentIndices[0]

            allIndices += indices[i + 1]
            if (adjacentIndices.size > 1) {
                allIndices += adjacentIndices[1]
            } else {
                allIndices += 0
            }

            allIndices += indices[i + 2]
            if (adjacentIndices.size > 2) {
                allIndices += adjacentIndices[2]
            } else {
                allIndices += 0
            }
        }

        println()

        return Mesh(layout, buffer, allIndices)
    }

    private fun findAdjacentIndices(triangles: List<Triangle>, triangle: Triangle): IntArray {
        var adjacentIndices = IntArray(0)

        val adjacentTriangle1 = findAdjacentTriangle(triangles, triangle.v1, triangle.v2, triangle.v3)
        val adjacentTriangle2 = findAdjacentTriangle(triangles, triangle.v1, triangle.v3, triangle.v2)
        val adjacentTriangle3 = findAdjacentTriangle(triangles, triangle.v2, triangle.v3, triangle.v1)

        if (adjacentTriangle1 != null) {
            if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle1.v1.position)) {
                adjacentIndices += vertexMap[VertexData(adjacentTriangle1.v1)]!!
            }
            if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle1.v2.position)) {
//                if (vertexMap[VertexData(adjacentTriangle1.v2)] == null) {
//                    println("ERROR ${VertexData(adjacentTriangle1.v2)}")
//                    println(adjacentTriangle1)
//                }
                adjacentIndices += vertexMap[VertexData(adjacentTriangle1.v2)]!!
            }
            if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle1.v3.position)) {
                adjacentIndices += vertexMap[VertexData(adjacentTriangle1.v3)]!!
            }
        }

        if (adjacentTriangle2 != null) {
            if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle2.v1.position)) {
                adjacentIndices += vertexMap[VertexData(adjacentTriangle2.v1)]!!
            }
            if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle2.v2.position)) {
                adjacentIndices += vertexMap[VertexData(adjacentTriangle2.v2)]!!
            }
            if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle2.v3.position)) {
                adjacentIndices += vertexMap[VertexData(adjacentTriangle2.v3)]!!
            }
        }

        if (adjacentTriangle3 != null) {
            if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle3.v1.position)) {
                adjacentIndices += vertexMap[VertexData(adjacentTriangle3.v1)]!!
            }
            if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle3.v2.position)) {
                adjacentIndices += vertexMap[VertexData(adjacentTriangle3.v2)]!!
            }
            if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle3.v3.position)) {
                adjacentIndices += vertexMap[VertexData(adjacentTriangle3.v3)]!!
            }
        }

        return adjacentIndices
    }

    private fun findAdjacentTriangle(triangles: List<Triangle>, v1: Vertex, v2: Vertex, v3: Vertex): Triangle? {
        for (triangle in triangles) {
            val containsV1 = triangle.vertices.any { vertex -> vertex.position == v1.position }
            val containsV2 = triangle.vertices.any { vertex -> vertex.position == v2.position }
            val containsV3 = triangle.vertices.any { vertex -> vertex.position == v3.position }
            if (containsV1 && containsV2 && !containsV3) {
                return triangle
            }
        }

        return null
    }
}