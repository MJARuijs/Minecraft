package chunks

import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.lights.AmbientLight
import graphics.lights.DirectionalLight
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.textures.ImageMap
import math.vectors.Vector2
import math.vectors.Vector3
import resources.images.ImageCache

class ChunkRenderer {

    private val shaderProgram = ShaderProgram.load("shaders/entities/block.vert", "shaders/entities/block.frag")
    private val colorCodedProgram = ShaderProgram.load("shaders/entities/colorCodedBlock.vert", "shaders/entities/colorCodedBlock.frag")
    private val blockTexture = ImageMap(ImageCache.get("textures/blocks/blocks.png"))
    private val breakTextures = ArrayList<Vector2>()

    private val blockSampler = Sampler(0)

    init {
        for (i in 0 until 9) {
            breakTextures += Vector2(i.toFloat() / 16f, 15f / 16f)
        }
    }

    fun render(chunks: ArrayList<Chunk>, camera: Camera, ambientLight: AmbientLight, directionalLight: DirectionalLight, selectedBlock: Pair<Chunk, Vector3>? = null) {
        GraphicsContext.enable(GraphicsOption.ALPHA_BLENDING, GraphicsOption.DEPTH_TESTING)

        blockSampler.bind(blockTexture)

        shaderProgram.start()
        shaderProgram.set("projection", camera.projectionMatrix)
        shaderProgram.set("view", camera.viewMatrix)
        shaderProgram.set("textureMap", blockSampler.index)

        if (selectedBlock == null) {
            shaderProgram.set("selected", false)
        } else {
            shaderProgram.set("selected", true)
            shaderProgram.set("selectedBlockPosition", selectedBlock.second)
        }

        ambientLight.apply(shaderProgram)
        directionalLight.apply(shaderProgram)

        for (chunk in chunks) {
            chunk.render(shaderProgram, breakTextures)
        }

        shaderProgram.stop()
        GraphicsContext.disable(GraphicsOption.ALPHA_BLENDING, GraphicsOption.DEPTH_TESTING)
    }

    fun renderSubset(camera: Camera, chunks: ArrayList<Chunk>, constraint: (Vector3) -> Boolean): Float {
        var totalNumberOfVisibleBlocks = 0

        for (chunk in chunks) {
            val size = chunk.determineSubset(constraint)
            totalNumberOfVisibleBlocks += size
        }

        val stepSize = (1.0f / totalNumberOfVisibleBlocks)

        colorCodedProgram.start()
        colorCodedProgram.set("projection", camera.projectionMatrix)
        colorCodedProgram.set("view", camera.viewMatrix)
        colorCodedProgram.set("stepSize", stepSize)

        var previousBlockCount = 0f

        for (chunk in chunks) {
            colorCodedProgram.set("idOffset", previousBlockCount)
            previousBlockCount += chunk.getSubsetSize()
            chunk.renderSubset()
        }

        colorCodedProgram.stop()

        return stepSize
    }
}