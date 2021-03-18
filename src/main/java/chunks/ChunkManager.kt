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

    private var chunksInProgress = HashSet<Vector2>()
    private val generator = ChunkGenerator()
    private val newChunks = ArrayList<Vector2>()

    private val locked = AtomicBoolean(false)

    var chunkRenderDistance = 2

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

    fun update(position: Vector3): ArrayList<Chunk> {
        val start = System.currentTimeMillis()
        val visibleChunks = ArrayList<Chunk>()

        val removableChunks = ArrayList<Chunk>()
        val renderDistance = (chunkRenderDistance + 1) * CHUNK_SIZE

        if (newChunks.isNotEmpty()) {
            while (locked.get()) {
                Thread.sleep(1)
            }

            locked.set(true)
            val initializedChunks = ArrayList<Vector2>()
            for (chunk in chunks) {
                if (newChunks.contains(chunk.getPosition())) {
                    chunk.initBlock()
                    initializedChunks += chunk.getPosition()
                }
            }

            newChunks.removeAll(initializedChunks)

            for (chunk in chunks) {
                if ((chunk.getCenter() - position.xz()).length() > CHUNK_SIZE * (MAX_DISTANCE + 1)) {
                    removableChunks += chunk
                }
            }

            chunks.removeAll(removableChunks)

            locked.set(false)
        }

        val end = System.currentTimeMillis()

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
                            val newData = generator.generateChunkData(x, z, Biome.PLANES, 0)
                            newChunks += Vector2(x, z)
                            while (locked.get()) {
                                Thread.sleep(10)
                            }

                            locked.set(true)
                            chunks += Chunk(newData)
                            locked.set(false)
                        }.start()
                    }
                } else {
                    visibleChunks += chunk
                }
            }
        }

        val delay = end - start
        if (delay > 100) {
//            println("hoi ${end - start}")
        }

        return visibleChunks
    }
}