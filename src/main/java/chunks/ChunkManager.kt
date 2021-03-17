package chunks

import chunks.ChunkGenerator.Companion.CHUNK_SIZE
import chunks.blocks.Face
import math.vectors.Vector3
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

object ChunkManager {

    private const val MAX_DISTANCE = 2

    private val chunks = ArrayList<Chunk>()

    var chunkRenderDistance = 1

//    private var position = Vector3()
    private val generator = ChunkGenerator()

//    init {
//        startThread()
//    }
//
//    fun updatePosition(position: Vector3) {
//        this.position = position
//    }
//
//    fun getChunks() = chunks
//
//    fun startThread() {
//        println(Thread.currentThread().id)
////        val newChunk = ChunkSender.takeChunk()
////        if (newChunk != null) {
////            chunks += Chunk(newChunk)
////        }
////        update(position)
//        while (true) {
//
//            val newChunk = ChunkSender.takeChunk()
//            if (newChunk != null) {
//                chunks += Chunk(newChunk)
//            }
//            update(position)
//            if (!Main.doMainLoop()) {
//                return
//            }
//        }
//    }

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

    var first = true

    fun update(position: Vector3): ArrayList<Chunk> {
        val start = System.currentTimeMillis()
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
//                    ChunkSender.requestChunk(x, z)

                    val newChunk = generator.generateChunk(x, z, Biome.PLANES, 0)
                    chunks += newChunk
                    visibleChunks += newChunk
                } else {
                    visibleChunks += chunk
                }
            }
        }
        if (first) {
            first = false
            val end = System.currentTimeMillis()
            println(end - start)
        }
        return visibleChunks
    }
}