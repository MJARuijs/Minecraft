package chunks

import chunks.blocks.BlockType
import math.PerlinNoise
import math.vectors.Vector3

class ChunkGenerator {

    companion object {
        const val CHUNK_SIZE = 16
        const val TERRAIN_HEIGHT = 60
        const val MAX_HEIGHT = 256
    }

    fun generate(chunkX: Int, chunkZ: Int, biome: Biome, seed: Long): Chunk {
        val noise = PerlinNoise(biome.octaves, biome.amplitude, biome.roughness, seed)
        var highestBlock = 0

        val heights = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { 0 } }
        val blocks = ArrayList<Pair<BlockType, Vector3>>()

        for (x in 0 until CHUNK_SIZE) {
            for (z in 0 until CHUNK_SIZE) {
                val worldX = chunkX + x
                val worldZ = chunkZ + z
                val h = noise[worldX, worldZ].toInt()
                val height = TERRAIN_HEIGHT + h
                heights[x][z] = height
                if (height > highestBlock) {
                    highestBlock = height
                }

                if (x == 0 || x == CHUNK_SIZE - 1 || z == 0 || z == CHUNK_SIZE - 1) {
                    for (y in 0 .. height) {
                        val position = Vector3(worldX, y, worldZ)
                        blocks += when {
                            y == height -> Pair(BlockType.GRASS, position)
                            y < height - 4 -> Pair(BlockType.STONE, position)
                            y < 2 -> Pair(BlockType.BEDROCK, position)
                            else -> Pair(BlockType.DIRT, position)
                        }
                    }
                } else {
                    val position = Vector3(worldX, height, worldZ)
                    blocks += Pair(BlockType.GRASS, position)
                }
            }
        }

        val chunk = Chunk(chunkX, chunkZ, biome, blocks, highestBlock)

        Thread {
            val newBlocks = ArrayList<Pair<BlockType, Vector3>>()

            for (x in 1 until CHUNK_SIZE - 1) {
                for (z in 1 until CHUNK_SIZE - 1) {
                    val worldX = chunkX + x
                    val worldZ = chunkZ + z

                    val height = heights[x][z]

                    for (y in 0 until height) {
                        val position = Vector3(worldX, y, worldZ)
                        val newBlock = when {
                            y < height - 4 -> Pair(BlockType.STONE, position)
                            y < 2 -> Pair(BlockType.BEDROCK, position)
                            else -> Pair(BlockType.DIRT, position)
                        }
                        newBlocks += newBlock
                    }

                }
            }
            chunk.add(newBlocks)
            newBlocks.clear()
        }.start()

        return chunk
    }
}