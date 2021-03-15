package chunks

import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.lights.AmbientLight
import graphics.lights.DirectionalLight
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.textures.ImageMap
import resources.images.ImageCache

class ChunkRenderer {

    private val shaderProgram = ShaderProgram.load("shaders/entities/block.vert", "shaders/entities/block.frag")
    private val colorCodedProgram = ShaderProgram.load("shaders/entities/colorCodedBlock.vert", "shaders/entities/colorCodedBlock.frag")
    private val blockTexture = ImageMap(ImageCache.get("textures/blocks/blocks.png"))

    private val sampler = Sampler(0)

    fun render(chunks: ArrayList<Chunk>, camera: Camera, ambientLight: AmbientLight, directionalLight: DirectionalLight) {
        GraphicsContext.enable(GraphicsOption.ALPHA_BLENDING)

        sampler.bind(blockTexture)

        shaderProgram.start()
        shaderProgram.set("projection", camera.projectionMatrix)
        shaderProgram.set("view", camera.viewMatrix)
//        shaderProgram.set("chunkHeight", ChunkGenerator.MAX_HEIGHT)
//        shaderProgram.set("chunkSize", ChunkGenerator.CHUNK_SIZE)
        shaderProgram.set("textureMap", sampler.index)

        ambientLight.apply(shaderProgram)
        directionalLight.apply(shaderProgram)

        for (chunk in chunks) {
            chunk.render()
        }

        shaderProgram.stop()
        GraphicsContext.disable(GraphicsOption.ALPHA_BLENDING)
    }

    fun renderColorCoded(chunks: ArrayList<Chunk>, camera: Camera): Float {

        var totalNumberOfSolidBlocks = 0

        for (chunk in chunks) {
            totalNumberOfSolidBlocks += chunk.getNumberOfBlocks()
        }

        val stepSize = (1.0f / totalNumberOfSolidBlocks)

        colorCodedProgram.start()
        colorCodedProgram.set("projection", camera.projectionMatrix)
        colorCodedProgram.set("view", camera.viewMatrix)
//        colorCodedProgram.set("chunkHeight", ChunkGenerator.MAX_HEIGHT)
//        colorCodedProgram.set("chunkSize", ChunkGenerator.CHUNK_SIZE)
        colorCodedProgram.set("stepSize", stepSize)

        var previousBlockCount = 0f

        for (chunk in chunks) {
            colorCodedProgram.set("idOffset", previousBlockCount)
            previousBlockCount += chunk.getNumberOfBlocks()
            chunk.renderUnTextured()
        }

        colorCodedProgram.stop()

        return stepSize
    }
}