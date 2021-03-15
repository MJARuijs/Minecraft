package chunks

import chunks.ChunkGenerator.CHUNK_SIZE
import math.vectors.Vector3
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

object ChunkManager {

    private const val MAX_DISTANCE = 10

    private val chunks = ArrayList<Chunk>()

    var chunkRenderDistance = 3

    operator fun plusAssign(chunk: Chunk) {
        chunks += chunk
    }

    operator fun minusAssign(chunk: Chunk) {
        chunks -= chunk
    }

    fun update(position: Vector3): ArrayList<Chunk> {
        val removableChunks = ArrayList<Chunk>()
        val renderDistance = (chunkRenderDistance + 1) * CHUNK_SIZE

        for (chunk in chunks) {
            if ((chunk.getCenter() - position.xz()).length() > CHUNK_SIZE * (MAX_DISTANCE + 1)) {
                removableChunks += chunk
            }
        }

        chunks.removeAll(removableChunks)

        val visibleChunks = ArrayList<Chunk>()

        val roundedX = position.x.roundToInt()
        val roundedZ = position.z.roundToInt()

        val minX = (abs(roundedX) / CHUNK_SIZE) * sign(position.x).toInt() * CHUNK_SIZE
        val minZ = (abs(roundedZ) / CHUNK_SIZE) * sign(position.z).toInt() * CHUNK_SIZE

        for (x in minX - renderDistance until minX + renderDistance step CHUNK_SIZE) {
            for (z in minZ - renderDistance until minZ + renderDistance step CHUNK_SIZE) {
                val chunk = chunks.findLast { chunk ->
                    chunk.chunkX == x && chunk.chunkZ == z
                }

                if (chunk == null) {
                    val newChunk = ChunkGenerator.generateChunk(x, z, Biome.PLANES, 0)
                    chunks += newChunk
                    visibleChunks += newChunk
                } else {
                    visibleChunks += chunk
                }
            }
        }

        return visibleChunks
    }

}