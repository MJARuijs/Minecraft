package chunks

import chunks.blocks.BlockType
import math.PerlinNoise
import math.vectors.Vector3
import util.Timer
import kotlin.math.max

class ChunkGenerator {

    companion object {
        const val CHUNK_SIZE = 16
        const val TERRAIN_HEIGHT = 60
        const val MAX_HEIGHT = 256
    }

    private val heights = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { 0 } }

    private var chunkX = 0
    private var chunkZ = 0
    private lateinit var noise: PerlinNoise

    fun generate(chunkX: Int, chunkZ: Int, biome: Biome, seed: Long): Chunk {
        println("Generating $chunkX $chunkZ")
        val id = Timer.start()
        this.chunkX = chunkX
        this.chunkZ = chunkZ
        this.noise = PerlinNoise(biome.octaves, biome.amplitude, biome.roughness, seed)

        for (x in 0 until CHUNK_SIZE) {
            for (z in 0 until CHUNK_SIZE) {
                heights[x][z] = 0
            }
        }

//        val noise =
        var highestBlock = 0

//        val blocks = ArrayList<Pair<BlockType, Vector3>>()
        val positions = ArrayList<Vector3>()
        var data = FloatArray(0)

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
                        data += position.toArray()
                        data +=  when {
                            y == height -> BlockType.GRASS
                            y < 2 -> BlockType.BEDROCK
                            y < height - 4 -> BlockType.STONE
                            else -> BlockType.DIRT
                        }.getOffsets()
                    }
                } else {
                    val position = Vector3(worldX, height, worldZ)
                    data += position.toArray()
                    data += BlockType.GRASS.getOffsets()
                    positions += position

                    val blocksBelow = addBlocksBelow(x, z)
                    for (blockBelow in blocksBelow) {
                        data += blockBelow.second.toArray()
                        data += blockBelow.first.getOffsets()

                        positions += blockBelow.second
                    }

//                    blocks += Pair(BlockType.GRASS, position)
//                    blocks += addBlocksBelow(x, z)
                }
            }
        }

        val chunk = Chunk(chunkX, chunkZ, biome, data, highestBlock)

        Thread {
            var newData = FloatArray(0)
            val newTimerId = Timer.start()
            for (x in 1 until CHUNK_SIZE - 1) {
                for (z in 1 until CHUNK_SIZE - 1) {
                    val worldX = chunkX + x
                    val worldZ = chunkZ + z

                    val height = get(x, z)

                    for (y in 0 until height) {
                        val position = Vector3(worldX, y, worldZ)

                        if (positions.none { blockPosition -> blockPosition == position }) {
                            newData += position.toArray()
                            newData += when {
                                y < 2 ->BlockType.BEDROCK
                                y < height - 4 -> BlockType.STONE
                                else -> BlockType.DIRT
                            }.getOffsets()
                        }

                    }
                }
            }

            chunk.add(newData, newTimerId)
        }.start()
        println("Done $chunkX, $chunkZ ${Timer.getDelay(id)}")

        return chunk
    }

    private fun addBlocksBelow(x: Int, z: Int): ArrayList<Pair<BlockType, Vector3>> {
        val blocksBelow = ArrayList<Pair<BlockType, Vector3>>()
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

        for (y in 1 until largestDifference) {
            val type = when {
                height - y < 2 -> BlockType.BEDROCK
                height - y < height - 4 -> BlockType.STONE
                else -> BlockType.DIRT
            }
            blocksBelow += Pair(type, Vector3(x + chunkX, height - y, z + chunkZ))
        }

        return blocksBelow
    }

    private fun get(x: Int, z: Int): Int {
        if (heights[x][z] == 0) {
            heights[x][z] = noise[x + chunkX, z + chunkZ].toInt() + TERRAIN_HEIGHT
        }
        return heights[x][z]
    }
}