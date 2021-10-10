import graphics.model.mesh.Attribute
import graphics.model.mesh.Layout
import graphics.model.mesh.Primitive
import math.vectors.Vector3
import java.nio.ByteBuffer
import java.nio.ByteOrder

object MeshCreator {

    private val vertexMap = HashMap<VertexData, Int>()

    fun create(): AdjacencyMesh {
        val containsNormals = true
        val containsTextureCoordinates = false

        var stride = 3

        if (containsNormals) {
            stride += 3
        }
        if (containsTextureCoordinates) {
            stride += 2
        }

        val cubeVertices = floatArrayOf(
            // Front
            -1f, 1f, 1f, 0f, 0f, -1f,
            -1f, -1f, 1f, 0f, 0f, -1f,
            1f, -1f, 1f, 0f, 0f, -1f,
            1f, -1f, 1f, 0f, 0f, -1f,
            1f, 1f, 1f, 0f, 0f, -1f,
            -1f, 1f, 1f, 0f, 0f, -1f,

            // Back
            1f, 1f, -1f, 0f, 0f, 1f,
            1f, -1f, -1f, 0f, 0f, 1f,
            -1f, -1f, -1f, 0f, 0f, 1f,
            -1f, -1f, -1f, 0f, 0f, 1f,
            -1f, 1f, -1f, 0f, 0f, 1f,
            1f, 1f, -1f, 0f, 0f, 1f,

            // Right
            1f, 1f, 1f, 1f, 0f, 0f,
            1f, -1f, 1f, 1f, 0f, 0f,
            1f, -1f, -1f, 1f, 0f, 0f,
            1f, -1f, -1f, 1f, 0f, 0f,
            1f, 1f, -1f, 1f, 0f, 0f,
            1f, 1f, 1f, 1f, 0f, 0f,

            // Left
            -1f, 1f, -1f, -1f, 0f, 0f,
            -1f, -1f, -1f, -1f, 0f, 0f,
            -1f, -1f, 1f, -1f, 0f, 0f,
            -1f, -1f, 1f, -1f, 0f, 0f,
            -1f, 1f, 1f, -1f, 0f, 0f,
            -1f, 1f, -1f, -1f, 0f, 0f,

            // Top
            -1f, 1f, -1f, 0f, 1f, 0f,
            -1f, 1f, 1f, 0f, 1f, 0f,
            1f, 1f, 1f, 0f, 1f, 0f,
            1f, 1f, 1f, 0f, 1f, 0f,
            1f, 1f, -1f, 0f, 1f, 0f,
            -1f, 1f, -1f, 0f, 1f, 0f,

            // Bottom
            -1f, -1f, 1f, 0f, -1f, 0f,
            -1f, -1f, -1f, 0f, -1f, 0f,
            1f, -1f, -1f, 0f, -1f, 0f,
            1f, -1f, -1f, 0f, -1f, 0f,
            1f, -1f, 1f, 0f, -1f, 0f,
            -1f, -1f, 1f, 0f, -1f, 0f
        )

        var indices = IntArray(0)
        for (i in cubeVertices.indices step stride) {
            val vertexData = VertexData(cubeVertices.copyOfRange(i, i + stride), containsNormals, containsTextureCoordinates)
            val newIndex = indices.distinct().size
            indices += newIndex
            vertexMap[vertexData] = newIndex
        }

        val triangles = ArrayList<Triangle>()

        for (i in indices.indices step 3) {
            val v1Index = indices[i]
            val v2Index = indices[i + 1]
            val v3Index = indices[i + 2]

            val position1 = Vector3(cubeVertices[v1Index * stride], cubeVertices[v1Index * stride + 1], cubeVertices[v1Index * stride + 2])
            val position2 = Vector3(cubeVertices[v2Index * stride], cubeVertices[v2Index * stride + 1], cubeVertices[v2Index * stride + 2])
            val position3 = Vector3(cubeVertices[v3Index * stride], cubeVertices[v3Index * stride + 1], cubeVertices[v3Index * stride + 2])

            val normal1 = Vector3(cubeVertices[v1Index * stride + 3], cubeVertices[v1Index * stride + 4], cubeVertices[v1Index * stride + 5])
            val normal2 = Vector3(cubeVertices[v2Index * stride + 3], cubeVertices[v2Index * stride + 4], cubeVertices[v2Index * stride + 5])
            val normal3 = Vector3(cubeVertices[v3Index * stride + 3], cubeVertices[v3Index * stride + 4], cubeVertices[v3Index * stride + 5])

            val v1 = Vertex(position1, normal1)
            val v2 = Vertex(position2, normal2)
            val v3 = Vertex(position3, normal3)

            triangles += Triangle(v1, v2, v3)
        }

        val buffer = ByteBuffer.allocateDirect(cubeVertices.size * 4).order(ByteOrder.nativeOrder())
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
            allIndices += adjacentIndices[1]
            allIndices += indices[i + 2]
            allIndices += adjacentIndices[2]
        }

        val layout = Layout(Primitive.TRIANGLE_ADJACENCY, listOf(Attribute(0, 3), Attribute(1, 3)))
        return AdjacencyMesh(layout, buffer, allIndices)
    }

    private fun findAdjacentIndices(triangles: List<Triangle>, triangle: Triangle): IntArray {
        var adjacentIndices = IntArray(0)

        val adjacentTriangle1 = findAdjacentTriangle(triangles, triangle.v2, triangle.v1, triangle.v3)!!
        val adjacentTriangle2 = findAdjacentTriangle(triangles, triangle.v1, triangle.v3, triangle.v2)!!
        val adjacentTriangle3 = findAdjacentTriangle(triangles, triangle.v2, triangle.v3, triangle.v1)!!

        if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle1.v1.position)) {
            adjacentIndices += vertexMap[VertexData(adjacentTriangle1.v1)]!!
        }
        if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle1.v2.position)) {
            adjacentIndices += vertexMap[VertexData(adjacentTriangle1.v2)]!!
        }
        if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle1.v3.position)) {
            adjacentIndices += vertexMap[VertexData(adjacentTriangle1.v3)]!!
        }

        if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle2.v1.position)) {
            adjacentIndices += vertexMap[VertexData(adjacentTriangle2.v1)]!!
        }
        if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle2.v2.position)) {
            adjacentIndices += vertexMap[VertexData(adjacentTriangle2.v2)]!!
        }
        if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle2.v3.position)) {
            adjacentIndices += vertexMap[VertexData(adjacentTriangle2.v3)]!!
        }

        if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle3.v1.position)) {
            adjacentIndices += vertexMap[VertexData(adjacentTriangle3.v1)]!!
        }
        if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle3.v2.position)) {
            adjacentIndices += vertexMap[VertexData(adjacentTriangle3.v2)]!!
        }
        if (!triangle.vertices.map { vertex -> vertex.position }.contains(adjacentTriangle3.v3.position)) {
            adjacentIndices += vertexMap[VertexData(adjacentTriangle3.v3)]!!
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