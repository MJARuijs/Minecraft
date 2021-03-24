package chunks

import chunks.blocks.BlockType
import math.PerlinNoise
import math.vectors.Vector3
import kotlin.math.max

class ChunkGenerator {

    companion object {
        const val CHUNK_SIZE = 16
        const val TERRAIN_HEIGHT = 50
        const val MAX_HEIGHT = 256
    }

    private val heights = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { 0 } }
    private val data = FloatArray(CHUNK_SIZE * CHUNK_SIZE * MAX_HEIGHT * 21)

    private var chunkX = 0
    private var chunkZ = 0

    private lateinit var noise: PerlinNoise

    fun generate(chunkX: Int, chunkZ: Int, biome: Biome, seed: Long): Chunk {
        this.chunkX = chunkX
        this.chunkZ = chunkZ
        this.noise = PerlinNoise(biome.octaves, biome.amplitude, biome.roughness, seed)

        for (x in 0 until CHUNK_SIZE) {
            for (z in 0 until CHUNK_SIZE) {
                heights[x][z] = 0
            }
        }

        var highestBlock = 0
        val positions = ArrayList<Vector3>()

        var i = 0

        for (x in 0 until CHUNK_SIZE) {
            for (z in 0 until CHUNK_SIZE) {
                val worldX = chunkX + x
                val worldZ = chunkZ + z

                val height = get(x, z)

                if (height > highestBlock) {
                    highestBlock = height
                }

                if (x == 0 || x == CHUNK_SIZE - 1 || z == 0 || z == CHUNK_SIZE - 1) {
                    for (y in 0 .. height) {
                        val position = Vector3(worldX, y, worldZ)
                        positions += position
                        data[i] = worldX.toFloat()
                        data[i + 1] = y.toFloat()
                        data[i + 2] = worldZ.toFloat()

                        val blockData = when {
                            y == height -> BlockType.GRASS
                            y < 2 -> BlockType.BEDROCK
                            y < height - 4 -> BlockType.STONE
                            else -> BlockType.DIRT
                        }.getOffsets()

                        for (j in blockData.indices) {
                            data [i + j + 3] = blockData[j]
                        }
                        i += 21
                    }
                } else {
                    val position = Vector3(worldX, height, worldZ)
                    positions += position

                    data[i] = worldX.toFloat()
                    data[i + 1] = height.toFloat()
                    data[i + 2] = worldZ.toFloat()
                    val blockData = BlockType.GRASS.getOffsets()
                    for (j in blockData.indices) {
                        data[i + j + 3] = blockData[j]
                    }

                    i += 21
                    i = addBlocksBelow(x, z, i)
                }
            }
        }

        val chunk = Chunk(chunkX, chunkZ, biome, data.sliceArray(0 until i), highestBlock)

        Thread {
            val newData = FloatArray(CHUNK_SIZE * CHUNK_SIZE * MAX_HEIGHT * 21 - i)
            i = 0

            for (x in 1 until CHUNK_SIZE - 1) {
                for (z in 1 until CHUNK_SIZE - 1) {
                    val worldX = chunkX + x
                    val worldZ = chunkZ + z

                    val height = get(x, z)

                    for (y in 0 until height) {
                        val position = Vector3(worldX, y, worldZ)

                        if (positions.none { blockPosition -> blockPosition == position }) {
                            newData[i] = worldX.toFloat()
                            newData[i + 1] = y.toFloat()
                            newData[i + 2] = worldZ.toFloat()
                            val blockData = when {
                                y < 2 -> BlockType.BEDROCK
                                y < height - 4 -> BlockType.STONE
                                else -> BlockType.DIRT
                            }.getOffsets()

                            for (j in blockData.indices) {
                                newData[i + j + 3] = blockData[j]
                            }
                            i += 21
                        }
                    }
                }
            }

            chunk.add(newData.sliceArray(0 until i))
        }.start()

        return chunk
    }

    private fun addBlocksBelow(x: Int, z: Int, i: Int): Int {
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
            return i
        }

        var j = i

        for (y in 0 until largestDifference - 1) {
            val typeData = when {
                height - y < 2 -> BlockType.BEDROCK
                height - y < height - 4 -> BlockType.STONE
                else -> BlockType.DIRT
            }.getOffsets()

            data[j ] = (x + chunkX).toFloat()
            data[j  + 1] = (height - y - 1).toFloat()
            data[j  + 2] = (z + chunkZ).toFloat()

            for (k in typeData.indices) {
                data[j + k + 3] = typeData[k]
            }

            j += 21
        }

        return j
    }

    private fun get(x: Int, z: Int): Int {
        if (heights[x][z] == 0) {
            heights[x][z] = noise[x + chunkX, z + chunkZ].toInt() + TERRAIN_HEIGHT
        }
        return heights[x][z]
    }
}