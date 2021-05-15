package environment.terrain.chunks

import environment.terrain.FaceTextures
import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.renderer.Renderable
import graphics.renderer.Renderer
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.shadows.ShadowData
import graphics.textures.ImageMap
import math.vectors.Vector2
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL30
import resources.images.ImageCache

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class ChunkRenderer : Renderer() {

    private val shaderProgram = ShaderProgram.load("shaders/environment/terrain/chunk.vert", "shaders/environment/terrain/chunk.geom", "shaders/environment/terrain/chunkForwardRendering.frag")
    private val deferredGeometryProgram = ShaderProgram.load("shaders/environment/terrain/chunk.vert", "shaders/environment/terrain/chunk.geom", "shaders/environment/terrain/chunkGeometryPass.frag")

    private val blockTexture = ImageMap(ImageCache.get("textures/blocks.png"))

    private val colorMaps = FaceTextures.colorMaps
    private val normalMaps = FaceTextures.normalMaps

    private val blockSampler = Sampler(0)
    private val normalSampler = Sampler(2)

    override val shadowProgram = ShaderProgram.load("shaders/environment/terrain/shadowChunk.vert", "shaders/environment/terrain/shadowChunk.frag")
    override val deferredLightingProgram = ShaderProgram.load("shaders/debug/2D.vert", "shaders/environment/terrain/chunkLightingPass.frag")

    override fun render(camera: Camera, ambient: AmbientLight, sun: Sun, chunks: List<Renderable>, shadows: List<ShadowData>) {
        GraphicsContext.enable(GraphicsOption.ALPHA_BLENDING, GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING)

        shaderProgram.start()
        shaderProgram.set("projection", camera.projectionMatrix)
        shaderProgram.set("view", camera.viewMatrix)
        shaderProgram.set("textureMap", blockSampler.index)

        blockSampler.bind(FaceTextures.t!!)

        ambient.apply(shaderProgram)
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

    override fun renderDeferredGeometry(camera: Camera, ambient: AmbientLight, chunks: List<Renderable>) {
        blockSampler.bind(blockTexture)

        deferredGeometryProgram.start()
        deferredGeometryProgram.set("projection", camera.projectionMatrix)
        deferredGeometryProgram.set("view", camera.viewMatrix)
        deferredGeometryProgram.set("textureMap", blockSampler.index)

        ambient.apply(deferredGeometryProgram)

        for (chunk in chunks) {
            chunk.render(deferredGeometryProgram)
        }

        deferredGeometryProgram.stop()
    }

}