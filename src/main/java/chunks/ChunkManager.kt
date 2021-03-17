package chunks

import chunks.ChunkGenerator.CHUNK_SIZE
import chunks.blocks.BlockType
import math.vectors.Vector2
import math.vectors.Vector3
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

object ChunkManager {

    private const val MAX_DISTANCE = 2

    private val chunks = ArrayList<Chunk>()

    private val requiredChunks = ArrayList<Vector2>()
    private val newChunks = ConcurrentHashMap<Vector2, ArrayList<Pair<BlockType, Vector3>>>()

    var chunkRenderDistance = 1

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
                println("Removing ${chunk.getPosition()} $position")
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
//                    println("computing $x $z")
//                    if (!requiredChunks.contains(Vector2(x, z))) {
//                        requiredChunks += Vector2(x, z)
//                        Thread {
//                            newChunks[Vector2(x, z)] = ChunkGenerator.generateChunkData(x, z, Biome.PLANES, 0, ::onChunkGenerated).third
////                            println("Finished $x $z")
//                        }.start()
//                    }

//                    Thread {
//                        ChunkGenerator.generateChunkData(x, z, Biome.PLANES, 0)
//                    }.start()

                    val newChunk = ChunkGenerator.generateChunk(x, z, Biome.PLANES, 0)
//                    val newChunk = Chunk(x, z, ArrayList())
                    chunks += newChunk
                    visibleChunks += newChunk
                } else {
                    visibleChunks += chunk
                }
            }
        }

        return visibleChunks
    }

    fun processNewChunks(): Boolean {
        if (requiredChunks.isEmpty()) {
            return true
        }

        for (chunk in newChunks) {
            chunks += Chunk(chunk.key, chunk.value)
            requiredChunks -= chunk.key
        }

        if (requiredChunks.isEmpty()) {
            return true
        }

        return false
    }

    private fun onChunkGenerated(x: Int, z: Int, blocks: ArrayList<Pair<BlockType, Vector3>>) {
//        requiredChunks.remove(Vector2(x, z))
//        chunks += Chunk(x, z, blocks)
    }

}