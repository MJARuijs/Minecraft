package chunks2

import chunks.ChunkGenerator.Companion.CHUNK_SIZE
import chunks.blocks.Face
import math.vectors.Vector3
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.floor

class ChunkManager(x: Int, z: Int) {

    constructor(position: Vector3) : this(position.x.toInt(), position.z.toInt())

    private var maxDistance = 0
    private var preGenerateDistance = 0

    private val chunks = ArrayList<Chunk>()

    private var renderDistance = 0

    private var currentX = 0
    private var currentZ = 0

    init {
        currentX = floor((x.toFloat() + (CHUNK_SIZE / 2)) / CHUNK_SIZE).toInt()
        currentZ = floor((z.toFloat() + (CHUNK_SIZE / 2)) / CHUNK_SIZE).toInt()
        update()
    }

    fun getRenderDistance() = renderDistance

    fun setRenderDistance(newRenderDistance: Int) {
        if (newRenderDistance < 0) {
            return
        }
        renderDistance = newRenderDistance

        if (preGenerateDistance < renderDistance) {
            preGenerateDistance = renderDistance
        }
        if (maxDistance < renderDistance) {
            maxDistance = renderDistance
        }

        println("New render distance: $renderDistance")

        update()
    }

    fun updatePosition(position: Vector3) {
        val chunkX = floor((position.x + (CHUNK_SIZE / 2)) / CHUNK_SIZE).toInt()
        val chunkZ = floor((position.z + (CHUNK_SIZE / 2)) / CHUNK_SIZE).toInt()

        for (chunk in chunks) {
            chunk.update()
        }

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

        for (x in (currentX - renderDistance) * CHUNK_SIZE .. (currentX + renderDistance) * CHUNK_SIZE step CHUNK_SIZE) {
            for (z in (currentZ - renderDistance) * CHUNK_SIZE .. (currentZ + renderDistance) * CHUNK_SIZE step CHUNK_SIZE) {
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

    fun stopBreaking() {
        for (chunk in chunks) {
            chunk.stopBreaking()
        }
    }

    private fun update() {
        val removableChunks = ArrayList<Chunk>()
        for (chunk in chunks) {
            val xDistance = abs(chunk.chunkX - currentX * CHUNK_SIZE)
            val zDistance = abs(chunk.chunkZ - currentZ * CHUNK_SIZE)
            if (xDistance > maxDistance * CHUNK_SIZE || zDistance > maxDistance * CHUNK_SIZE) {
                removableChunks += chunk
            }
        }

        chunks.removeAll(removableChunks)

        val distance = preGenerateDistance * CHUNK_SIZE
        for (x in -distance .. distance step CHUNK_SIZE) {
            for (z in -distance .. distance step CHUNK_SIZE) {
                Thread {
                    generate(currentX * CHUNK_SIZE + x, currentZ * CHUNK_SIZE + z)
                }.start()
            }
        }
    }

    private fun generate(x: Int, z: Int) {
        try {
            if (chunks.none { chunk -> chunk.chunkX == x && chunk.chunkZ == z }) {
                chunks += ChunkGenerator().generate(x, z, Biome.HILLS, 0)
            }
        } catch (e: ConcurrentModificationException) {
            generate(x, z)
        }
    }
}