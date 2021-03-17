package chunks

import chunks.ChunkGenerator.Companion.CHUNK_SIZE
import chunks.blocks.Face
import math.vectors.Vector2
import math.vectors.Vector3
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

object ChunkManager {

    private const val MAX_DISTANCE = 2

    private val chunks = ArrayList<Chunk>()

    var chunkRenderDistance = 1

    private var chunksInProgress = HashSet<Vector2>()
    private val generator = ChunkGenerator()
//    private val generatedChunks = ArrayList<ChunkData>()
    private val generatedChunks2 = ArrayList<Chunk>()
//    private val newChunks = ArrayList<Vector2>()

    private val locked = AtomicBoolean(false)

    fun newBlockPosition(position: Vector3, face: Face): Vector3 {
        return when (face) {
            Face.FRONT  -> position + Vector3(0, 0, 1)
            Face.BACK   -> position + Vector3(0, 0, -1)
            Face.LEFT   -> position + Vector3(-1, 0, 0)
            Face.RIGHT  -> position + Vector3(1, 0, 0)
            Face.TOP    -> position + Vector3(0, 1, 0)
            Face.BOTTOM -> position + Vector3(0, -1, 0)
            Face.ALL -> position
        }
    }

    operator fun plusAssign(chunk: Chunk) {
        chunks += chunk
    }

    operator fun minusAssign(chunk: Chunk) {
        chunks -= chunk
    }

    fun update(position: Vector3): ArrayList<Chunk> {
        val start = System.currentTimeMillis()
        val visibleChunks = ArrayList<Chunk>()

//        if (c.isNotEmpty()) {
            while (locked.get()) {
                Thread.sleep(1)
            }

            locked.set(true)
            for (chunk in chunks) {
                if (!chunk.initialized) {
                    chunk.initBlock()
                }
            }
//            for (data in generatedChunks) {
//                val chunk = Chunk(data)
//                chunks += chunk
//                visibleChunks += chunk
//            }
            locked.set(false)

//            generatedChunks.clear()
//        }

        val end = System.currentTimeMillis()

        val removableChunks = ArrayList<Chunk>()
        val renderDistance = (chunkRenderDistance + 1) * CHUNK_SIZE

        for (chunk in chunks) {
            if ((chunk.getCenter() - position.xz()).length() > CHUNK_SIZE * (MAX_DISTANCE + 1)) {
                removableChunks += chunk
            }
        }

        chunks.removeAll(removableChunks)

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
                    if (!chunksInProgress.contains(Vector2(x, z))) {
                        chunksInProgress.add(Vector2(x, z))
                        Thread {
//                            println("${Thread.currentThread().id} creating $x $z")
                            val newData = generator.generateChunkData(x, z, Biome.PLANES, 0)
//                            println("Done generating $x $z ${chunksInProgress.size}")

                            while (locked.get()) {
                                Thread.sleep(10)
                            }

                            locked.set(true)

                            chunks += Chunk(newData)

//                            generatedChunks += newData
                            locked.set(false)
                        }.start()
//                    val newChunk = generator.generateChunk(x, z, Biome.PLANES, 0)
//                    chunks += newChunk
//                    visibleChunks += newChunk
                    }

                } else {
                    visibleChunks += chunk
                }
            }
        }

        val delay = end - start
        if (delay > 100) {
            println("hoi ${end - start}")
        }


        return visibleChunks
    }
}