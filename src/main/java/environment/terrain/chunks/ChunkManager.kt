package environment.terrain.chunks

import devices.Button
import devices.Mouse
import environment.terrain.Biome
import environment.terrain.FaceTextures
import environment.terrain.Selector
import environment.terrain.blocks.BlockType
import environment.terrain.chunks.ChunkGenerator.Companion.CHUNK_SIZE
import graphics.Camera
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
    private val selector = Selector()

    private var renderDistance = 0

    private var currentX = 0
    private var currentZ = 0

    init {
        FaceTextures.load("src/main/resources/textures/blocks/")
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

    fun update(position: Vector3, mouse: Mouse, camera: Camera): ArrayList<Chunk> {
        if (mouse.isPressed(Button.LEFT)) {
            val selectedBlock = selector.getSelected(chunks, camera, camera.position)
            if (selectedBlock != null) {
                for (chunk in chunks) {
                    if (chunk.containsBlock(selectedBlock.first)) {
                        val updateOtherChunks = chunk.removeBlock(selectedBlock.first)
                        if (updateOtherChunks) {
                            updateOtherChunks(chunk, selectedBlock.first)
                        }
                    }
                }
            }
        }

        if (mouse.isPressed(Button.RIGHT)) {
            val selectedBlock = selector.getSelected(chunks, camera, camera.position)
            if (selectedBlock != null) {
                for (chunk in chunks) {
                    if (chunk.containsBlock(selectedBlock.first)) {
                        chunk.addBlock(selectedBlock.first + selectedBlock.second.normal, BlockType.DIAMOND_ORE)
                    }
                }
            }
        }

        val chunkX = floor((position.x + (CHUNK_SIZE / 2)) / CHUNK_SIZE).toInt()
        val chunkZ = floor((position.z + (CHUNK_SIZE / 2)) / CHUNK_SIZE).toInt()

        if (chunkX != currentX || chunkZ != currentZ) {
            currentX = chunkX
            currentZ = chunkZ
            Thread {
                update()
            }.start()
        }

        return determineVisibleChunks()
    }

    private fun updateOtherChunks(updatedChunk: Chunk, position: Vector3) {
        val normalizedChunkX = updatedChunk.normalizedX
        val normalizedChunkZ = updatedChunk.normalizedZ

        if (position.x - updatedChunk.x == 7.0f) {
            for (chunk in chunks) {
                if (chunk.normalizedX == normalizedChunkX + 1 && chunk.normalizedZ == normalizedChunkZ) {
                    chunk.addFaceData(position, FaceDirection.RIGHT)
                }
            }
        }


    }

    private fun determineVisibleChunks(): ArrayList<Chunk> {
        val visibleChunks = ArrayList<Chunk>()

        for (x in (currentX - renderDistance) * CHUNK_SIZE .. (currentX + renderDistance) * CHUNK_SIZE step CHUNK_SIZE) {
            for (z in (currentZ - renderDistance) * CHUNK_SIZE .. (currentZ + renderDistance) * CHUNK_SIZE step CHUNK_SIZE) {
                var chunk: Chunk? = null

                for (i in 0 until chunks.size) {
                    if (chunks[i].x == x && chunks[i].z == z) {
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
            val xDistance = abs(chunk.x - currentX * CHUNK_SIZE)
            val zDistance = abs(chunk.z - currentZ * CHUNK_SIZE)
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
            if (chunks.none { chunk -> chunk.x == x && chunk.z == z }) {
                chunks += ChunkGenerator().generate(x, z, Biome.PLANES, 0)
            }
        } catch (e: ConcurrentModificationException) {
            generate(x, z)
        }
    }
}