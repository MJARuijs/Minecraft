package environment.terrain.chunks

import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.shadows.ShadowData
import graphics.textures.ImageMap
import math.matrices.Matrix4
import math.vectors.Vector2
import resources.images.ImageCache

class ChunkRenderer {

    private val shaderProgram = ShaderProgram.load("shaders/environment/terrain/chunk.vert", "shaders/environment/terrain/chunk.geom", "shaders/environment/terrain/chunk.frag")
    private val shadowProgram = ShaderProgram.load("shaders/environment/terrain/shadowChunk.vert", "shaders/environment/terrain/shadowChunk.frag")

    private val blockTexture = ImageMap(ImageCache.get("textures/blocks/blocks.png"))
    private val blockSampler = Sampler(0)

    fun render(chunks: List<Chunk>, camera: Camera, ambientLight: AmbientLight, sun: Sun, shadows: List<ShadowData>) {
        GraphicsContext.enable(GraphicsOption.ALPHA_BLENDING, GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING)

        blockSampler.bind(blockTexture)
        shaderProgram.start()
        shaderProgram.set("projection", camera.projectionMatrix)
        shaderProgram.set("view", camera.viewMatrix)
        shaderProgram.set("textureMap", blockSampler.index)

        ambientLight.apply(shaderProgram)
        sun.apply(shaderProgram)

        if (shadows.isNotEmpty()) {
            val shadowData = shadows[0]
            val shadowSampler = Sampler(1)

            shadowSampler.bind(shadowData.shadowMap)

            shaderProgram.set("shadowDistance", shadowData.shadowDistance)
            shaderProgram.set("shadowMatrix", shadowData.getShadowMatrix())
            shaderProgram.set("shadowMap", shadowSampler.index)
            shaderProgram.set("shadowMapSize", Vector2(
                    shadowData.shadowMap.getWidth(),
                    shadowData.shadowMap.getHeight()
            ))
        }

        for (chunk in chunks) {
            chunk.render(shaderProgram)
        }

        shaderProgram.stop()
        GraphicsContext.disable(GraphicsOption.ALPHA_BLENDING, GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING)
    }

    fun renderBlack(chunks: List<Chunk>, projection: Matrix4, view: Matrix4) {
        GraphicsContext.enable(GraphicsOption.ALPHA_BLENDING, GraphicsOption.DEPTH_TESTING)
//        GraphicsContext.disable(GraphicsOption.FACE_CULLING)

        shadowProgram.start()
        shadowProgram.set("projection", projection)
        shadowProgram.set("view", view)

        for (chunk in chunks) {
            chunk.render(shadowProgram)
        }

        shadowProgram.stop()
//        GraphicsContext.enable(GraphicsOption.FACE_CULLING)

        GraphicsContext.disable(GraphicsOption.ALPHA_BLENDING, GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING)
    }
}