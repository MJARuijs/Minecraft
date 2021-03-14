package chunks.blocks

import chunks.Chunk
import chunks.ChunkGenerator
import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.lights.AmbientLight
import graphics.lights.DirectionalLight
import graphics.shaders.ShaderProgram

object ChunkRenderer {

    private val shaderProgram = ShaderProgram.load("shaders/entities/block.vert", "shaders/entities/block.frag")
    private val colorCodedProgram = ShaderProgram.load("shaders/entities/colorCodedBlock.vert", "shaders/entities/colorCodedBlock.frag")

    private val chunks = ArrayList<Chunk>()

    operator fun plusAssign(chunk: Chunk) {
        chunks += chunk
    }

    operator fun get(i: Int) = chunks[i]

    fun render(camera: Camera, ambientLight: AmbientLight, directionalLight: DirectionalLight) {
        GraphicsContext.enable(GraphicsOption.ALPHA_BLENDING)
        shaderProgram.start()
        shaderProgram.set("projection", camera.projectionMatrix)
        shaderProgram.set("view", camera.viewMatrix)
        shaderProgram.set("chunkHeight", ChunkGenerator.MAX_HEIGHT)
        shaderProgram.set("chunkSize", ChunkGenerator.CHUNK_SIZE)
        shaderProgram.set("blockTextures[0]", 0)
        shaderProgram.set("blockTextures[1]", 1)

        ambientLight.apply(shaderProgram)
        directionalLight.apply(shaderProgram)

        for (chunk in chunks) {
            chunk.render(shaderProgram)
        }

        shaderProgram.stop()
        GraphicsContext.disable(GraphicsOption.ALPHA_BLENDING)
    }

    fun renderColorCoded(camera: Camera) {

        var totalNumberOfSolidBlocks = 0
        for (chunk in chunks) {
            totalNumberOfSolidBlocks += chunk.numberOfBlocks
        }

        println(totalNumberOfSolidBlocks)

        val stepSize = (1.0f / totalNumberOfSolidBlocks)

        colorCodedProgram.start()
        colorCodedProgram.set("projection", camera.projectionMatrix)
        colorCodedProgram.set("view", camera.viewMatrix)
        colorCodedProgram.set("chunkHeight", ChunkGenerator.MAX_HEIGHT)
        colorCodedProgram.set("chunkSize", ChunkGenerator.CHUNK_SIZE)
        colorCodedProgram.set("stepSize", stepSize)

        for (chunk in chunks) {
            chunk.render(colorCodedProgram)
        }

        colorCodedProgram.stop()
    }
}