package chunks

import chunks.blocks.BlockType
import math.vectors.Vector3

object ChunkGenerator {

    private const val CHUNK_SIZE = 16
    private const val CHUNK_HEIGHT = 70

    fun generateChunk(place: Vector3): Chunk {
        val blocks = ArrayList<Pair<BlockType, Vector3>>()
        for (x in 0 until CHUNK_SIZE) {
            for (y in 0 until CHUNK_HEIGHT) {
                for (z in 0 until CHUNK_SIZE) {
//                    blocks += if (y < 1) {
                    val position = place * CHUNK_SIZE + Vector3(x, CHUNK_HEIGHT, z)
                    if (blocks.none { block -> block.second == position }) {
                        blocks += Pair(BlockType.BEDROCK, position)
                    }
//                    } else {
//                        Pair(BlockType.DIRT, place * CHUNK_SIZE + Vector3(x, y, z))
//                    }
                }
            }
        }

        return Chunk(blocks)
    }
}