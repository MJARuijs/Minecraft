package chunks2

import math.Noise
import math.vectors.Vector3
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

class ChunkGenerator {

    companion object {
        const val CHUNK_SIZE = 16
        const val HALF_CHUNK_SIZE = CHUNK_SIZE / 2
        const val TERRAIN_HEIGHT = 15
        const val MAX_HEIGHT = 256
    }

    private val heights = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { 0 } }

    private val positions = ArrayList<Vector3>()

    private var chunkX = 0
    private var chunkZ = 0

    private lateinit var noise: Noise

    private var floats = FloatArray(0)
    private var ints = IntArray(0)
    private var vertexCount = 0

    fun generate(chunkX: Int, chunkZ: Int, biome: Biome, seed: Long): Chunk {
        this.chunkX = chunkX - HALF_CHUNK_SIZE
        this.chunkZ = chunkZ - HALF_CHUNK_SIZE
        this.noise = Noise(biome.octaves, biome.amplitude, biome.smoothness, seed)

        for (x in 0 until CHUNK_SIZE) {
            for (z in 0 until CHUNK_SIZE) {
                heights[x][z] = 0
            }
        }

        val blockData = ArrayList<BlockData>()

        for (x in 0 until CHUNK_SIZE) {
            for (z in 0 until CHUNK_SIZE) {
                val worldX = chunkX + x - HALF_CHUNK_SIZE
                val worldZ = chunkZ + z - HALF_CHUNK_SIZE
                val height = get(x, z)

                val blockType = determineBlockType(height, height, biome)

                val leftHeight = get(x - 1, z)
                val rightHeight = get(x + 1, z)
                val backHeight = get(x, z - 1)
                val frontHeight = get(x, z + 1)

                addFaceData(worldX, height, worldZ, FaceDirection.TOP, blockType)
                if (height == 0) {
                    addFaceData(worldX, height, worldZ, FaceDirection.BOTTOM, blockType)
                }

                if (leftHeight < height) addFaceData(worldX, height, worldZ, FaceDirection.LEFT, blockType)
                if (rightHeight < height) addFaceData(worldX, height, worldZ, FaceDirection.RIGHT, blockType)
                if (backHeight < height) addFaceData(worldX, height, worldZ, FaceDirection.BACK, blockType)
                if (frontHeight < height) addFaceData(worldX, height, worldZ, FaceDirection.FRONT, blockType)

                val leftDifference = height - leftHeight
                val rightDifference = height - rightHeight
                val frontDifference = height - frontHeight
                val backDifference = height - backHeight

                addFacesBelow(worldX, height, worldZ, backDifference, FaceDirection.BACK, biome)
                addFacesBelow(worldX, height, worldZ, frontDifference, FaceDirection.FRONT, biome)
                addFacesBelow(worldX, height, worldZ, leftDifference, FaceDirection.LEFT, biome)
                addFacesBelow(worldX, height, worldZ, rightDifference, FaceDirection.RIGHT, biome)

                val position = Vector3(worldX, height, worldZ)
                positions += position
                blockData += BlockData(blockType, position)
                blockData += addBlocksBelow(x, z, biome)
            }
        }

//        val buffer = ByteBuffer.allocateDirect(vertexCount * 4 * 4).order(ByteOrder.nativeOrder())
//        for (i in 0 until vertexCount * 3 step 3) {
//            buffer.putFloat(floats[i])
//            buffer.putFloat(floats[i + 1])
//            buffer.putFloat(floats[i + 2])
//            buffer.putInt(ints[i / 3])
//        }

        val chunk = Chunk(chunkX, chunkZ, biome, blockData, floats, ints, vertexCount)

        Thread {
            val newBlocks = ArrayList<BlockData>()
            for (x in 0 until CHUNK_SIZE) {
                for (z in 0 until CHUNK_SIZE) {
                    val worldX = chunkX + x - HALF_CHUNK_SIZE
                    val worldZ = chunkZ + z - HALF_CHUNK_SIZE

                    val height = get(x, z)

                    for (y in 0 until height) {
                        val blockType = determineBlockType(y, height, biome)
                        val position = Vector3(worldX, y, worldZ)
                        newBlocks += BlockData(blockType, position)
                    }
                }
            }
            chunk.add(newBlocks)
        }.start()

        return chunk
    }

    private fun get(x: Int, z: Int): Int {
        if (x < 0 || z < 0 || x >= CHUNK_SIZE || z >= CHUNK_SIZE) {
            return noise[x + chunkX, z + chunkZ].toInt() + TERRAIN_HEIGHT
        }
        if (heights[x][z] == 0) {
            heights[x][z] = noise[x + chunkX, z + chunkZ].toInt() + TERRAIN_HEIGHT
        }
        return heights[x][z]
    }

    private fun determineBlockType(y: Int, maxY: Int, biome: Biome): BlockType2 {
        var blockType = BlockType2.STONE
        var typeHeight = 0
        for (type in biome.blocks) {
            if (y > maxY - type.second - typeHeight) {
                blockType = type.first
                break
            }
            typeHeight += type.second
        }
        return blockType
    }

    private fun addFaceData(x: Int, height: Int, z: Int, face: FaceDirection, blockType: BlockType2) {
        val faceVertices = face.vertices
        for (i in faceVertices.indices step 3) {
            floats += faceVertices[i] + x
            floats += faceVertices[i + 1] + height.toFloat()
            floats += faceVertices[i + 2] + z
            ints += blockType.textureIndices[FaceDirection.values().indexOf(face)]

            vertexCount += 1
        }
    }

    private fun addFacesBelow(x: Int, height: Int, z: Int, heightDifference: Int, face: FaceDirection, biome: Biome) {
        if (heightDifference > 0) {
            for (y in 0 until heightDifference - 1) {
                val extraBlockType = determineBlockType(height - y - 1, height, biome)
                addFaceData(x, height - y - 1, z, face, extraBlockType)
            }
        }
    }

    private fun addBlocksBelow(x: Int, z: Int, biome: Biome): List<BlockData> {
        val height = get(x, z)
        val leftHeight = get(x - 1, z)
        val rightHeight = get(x + 1, z)
        val frontHeight = get(x, z + 1)
        val behindHeight = get(x, z - 1)

        val leftDifference = height - leftHeight
        val rightDifference = height - rightHeight
        val frontDifference = height - frontHeight
        val behindDifference = height - behindHeight

        val largestDifference = max(max(max(leftDifference, rightDifference), frontDifference), behindDifference)

        if (largestDifference <= 0) {
            return arrayListOf()
        }

        val extraBlocks = ArrayList<BlockData>()

        for (y in 0 until largestDifference - 1) {
            val blockType = determineBlockType(height - y - 1, height, biome)
            val position = Vector3(x + chunkX, height - y - 1, z + chunkZ)
            positions += position
            extraBlocks += BlockData(blockType, position)
        }

        return extraBlocks
    }
}