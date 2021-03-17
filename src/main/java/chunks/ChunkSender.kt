package chunks

import math.vectors.Vector2
import java.util.concurrent.atomic.AtomicBoolean

object ChunkSender {

    private val requestedChunks = HashMap<Vector2, ChunkData?>()
    private val chunksInProcess = HashMap<Vector2, ChunkData?>()
    private val locked = AtomicBoolean(false)

    fun placeChunk(chunkData: ChunkData) {
        while (locked.get()) {
            Thread.sleep(10)
        }

        locked.set(true)

        chunksInProcess[Vector2(chunkData.x, chunkData.z)] = chunkData

//        println("Placed data: ${chunkData.x}, ${chunkData.z}")

        locked.set(false)
    }

    fun takeRequest(): Vector2? {
        while (locked.get()) {
            Thread.sleep(10)
        }

        locked.set(true)

        if (requestedChunks.isNotEmpty()) {
            val requestedChunk = requestedChunks.keys.elementAt(0)
            requestedChunks.remove(requestedChunk)

            chunksInProcess[requestedChunk] = null
            locked.set(false)
            return requestedChunk
        }

        locked.set(false)
        return null
    }

    fun requestChunk(x: Int, z: Int) = requestChunk(Vector2(x, z))

    fun requestChunk(position: Vector2) {
        if (!requestedChunks.containsKey(position) && !chunksInProcess.containsKey(position)) {
            println("Requested $position")
            requestedChunks[position] = null
        }
    }

    fun takeChunk(): ChunkData? {
        while (locked.get()) {
            Thread.sleep(10)
        }

        locked.set(true)

        for (chunk in chunksInProcess) {
            if (chunk.value != null) {
                locked.set(false)
//                println("Took chunk: ${chunk.key}")
                chunksInProcess.remove(chunk.key)
                return chunk.value
            }
        }

        locked.set(false)

        return null
    }
}