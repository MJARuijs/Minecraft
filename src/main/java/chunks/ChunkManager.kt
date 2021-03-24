package chunks

import chunks.ChunkGenerator.Companion.CHUNK_SIZE
import chunks.blocks.Face
import math.vectors.Vector2
import math.vectors.Vector3
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.*

class ChunkManager {

    private val maxDistance = 4

    private val chunks = ArrayList<Chunk>()
    private val locked = AtomicBoolean(false)

    private var currentX = 0
    private var currentZ = 0

    private var renderDistance = 2

    private var chunksInProgress = HashSet<Vector2>()

    init {
        update()
    }

    fun updatePosition(position: Vector3) {
        val chunkX = floor((position.x + (CHUNK_SIZE / 2)) / CHUNK_SIZE).toInt()
        val chunkZ = floor((position.z + (CHUNK_SIZE / 2)) / CHUNK_SIZE).toInt()
        if (chunkX != currentX || chunkZ != currentZ) {
            currentX = chunkX
            currentZ = chunkZ
            Thread {
                update()
            }.start()
        }
    }

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

    fun determineVisibleChunks(): ArrayList<Chunk> {
        val visibleChunks = ArrayList<Chunk>()

        for (x in (currentX - renderDistance) * CHUNK_SIZE until (currentX + renderDistance) * CHUNK_SIZE step CHUNK_SIZE) {
            for (z in (currentZ - renderDistance) * CHUNK_SIZE until (currentZ + renderDistance) * CHUNK_SIZE step CHUNK_SIZE) {
                var chunk: Chunk? = null

                for (i in 0 until chunks.size) {
                    if (chunks[i].chunkX == x && chunks[i].chunkZ == z) {
                        chunk = chunks[i]
                        break
                    }
                }

                if (chunk != null) {
                    visibleChunks += chunk
                }
            }
        }

        return visibleChunks
    }

    private fun update() {

        //TODO: Implement removing chunks that are too far away
//        for (chunk in chunks) {
//
//        }

        for (x in 0 until (maxDistance) * CHUNK_SIZE step CHUNK_SIZE) {
            for (z in 0 until (maxDistance) * CHUNK_SIZE step CHUNK_SIZE) {
                Thread {
                    generate(currentX * CHUNK_SIZE + x, currentZ * CHUNK_SIZE + z)
                }.start()
                Thread {
                    generate(currentX * CHUNK_SIZE + x, currentZ * CHUNK_SIZE - z)
                }.start()
                Thread {
                    generate(currentX * CHUNK_SIZE - x, currentZ * CHUNK_SIZE - z)
                }.start()
                Thread {
                    generate(currentX * CHUNK_SIZE - x, currentZ * CHUNK_SIZE - z)
                }.start()
            }
        }
    }

    private fun generate(x: Int, z: Int) {
        if (!chunksInProgress.contains(Vector2(x, z))) {
            requestLock()
            val chunk = chunks.findLast { chunk ->
                chunk.chunkX == x && chunk.chunkZ == z
            }
            releaseLock()

            if (chunk == null) {
                chunksInProgress.add(Vector2(x, z))
                val newChunk = ChunkGenerator().generate(x, z, Biome.PLANES, 0)

                requestLock()
                chunks += newChunk
                releaseLock()

                chunksInProgress.remove(Vector2(x, z))
            }
        }
    }

    private fun requestLock() {
        while (locked.get()) {
            Thread.sleep(10)
        }

        locked.set(true)
    }

    private fun releaseLock() {
        locked.set(false)
    }
}