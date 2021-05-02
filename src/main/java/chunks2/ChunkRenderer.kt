package chunks2

import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.textures.ImageMap
import resources.images.ImageCache

class ChunkRenderer {

    private val shaderProgram = ShaderProgram.load("shaders/environment/terrain/chunk.vert", "shaders/environment/terrain/chunk.frag")

    private val blockTexture = ImageMap(ImageCache.get("textures/blocks/blocks.png"))
    private val blockSampler = Sampler(0)

    fun render(chunks: List<Chunk>, camera: Camera, ambientLight: AmbientLight, sun: Sun) {
        GraphicsContext.enable(GraphicsOption.ALPHA_BLENDING, GraphicsOption.DEPTH_TESTING)
        GraphicsContext.disable(GraphicsOption.FACE_CULLING)
        blockSampler.bind(blockTexture)
        shaderProgram.start()
        shaderProgram.set("projection", camera.projectionMatrix)
        shaderProgram.set("view", camera.viewMatrix)
        shaderProgram.set("textureMap", blockSampler.index)

        ambientLight.apply(shaderProgram)
        sun.apply(shaderProgram)

        for (chunk in chunks) {
            chunk.render(shaderProgram)
        }

        shaderProgram.stop()
        GraphicsContext.enable(GraphicsOption.FACE_CULLING)
        GraphicsContext.disable(GraphicsOption.ALPHA_BLENDING, GraphicsOption.DEPTH_TESTING)
    }
}