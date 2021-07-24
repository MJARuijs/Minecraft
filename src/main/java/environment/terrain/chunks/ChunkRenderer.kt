package environment.terrain.chunks

import environment.terrain.FaceTextures
import graphics.Camera
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.renderer.Renderable
import graphics.renderer.Renderer
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.shadows.ShadowData
import math.matrices.Matrix4
import math.vectors.Vector2

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class ChunkRenderer : Renderer() {

    private val shaderProgram = ShaderProgram.load("shaders/environment/terrain/chunk.vert", "shaders/environment/terrain/chunk.geom", "shaders/environment/terrain/chunkForwardRendering.frag")
    private val deferredGeometryProgram = ShaderProgram.load("shaders/environment/terrain/chunk.vert", "shaders/environment/terrain/chunk.geom", "shaders/environment/terrain/chunkGeometryPass.frag")

    private val blockSampler = Sampler(0)
    private val normalSampler = Sampler(1)
    private val specularSampler = Sampler(2)

    private val shadowProgram = ShaderProgram.load("shaders/environment/terrain/shadowChunk.vert", "shaders/environment/terrain/shadowChunk.frag")

    override val deferredLightingProgram = ShaderProgram.load("shaders/debug/2D.vert", "shaders/environment/terrain/chunkLightingPass.frag")

    override fun render(camera: Camera, ambient: AmbientLight, sun: Sun, chunks: List<Renderable>, shadows: List<ShadowData>) {
        blockSampler.bind(FaceTextures.textures!!)
        normalSampler.bind(FaceTextures.normals!!)
        specularSampler.bind(FaceTextures.speculars!!)

        shaderProgram.start()
        shaderProgram.set("projection", camera.projectionMatrix)
        shaderProgram.set("view", camera.viewMatrix)
        shaderProgram.set("textureMap", blockSampler.index)
        shaderProgram.set("normalMap", normalSampler.index)
        shaderProgram.set("specularMap", specularSampler.index)
        shaderProgram.set("cameraPosition", camera.position)

        ambient.apply(shaderProgram)
        sun.apply(shaderProgram)

        if (shadows.isNotEmpty()) {
            val shadowData = shadows[0]
            val shadowSampler = Sampler(4)

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
    }

    override fun renderDeferredGeometry(camera: Camera, ambient: AmbientLight, chunks: List<Renderable>, shadows: List<ShadowData>) {
        blockSampler.bind(FaceTextures.textures!!)
        normalSampler.bind(FaceTextures.normals!!)
        specularSampler.bind(FaceTextures.speculars!!)

        deferredGeometryProgram.start()
        deferredGeometryProgram.set("projection", camera.projectionMatrix)
        deferredGeometryProgram.set("view", camera.viewMatrix)
        deferredGeometryProgram.set("textureMap", blockSampler.index)
        deferredGeometryProgram.set("normalMap", normalSampler.index)
        deferredGeometryProgram.set("specularMap", specularSampler.index)

        if (shadows.isNotEmpty()) {
            val shadowData = shadows[0]
            val shadowSampler = Sampler(6)

            shadowSampler.bind(shadowData.shadowMap)

            deferredGeometryProgram.set("shadowDistance", shadowData.shadowDistance)
            deferredGeometryProgram.set("shadowMatrix", shadowData.getShadowMatrix())
            deferredGeometryProgram.set("shadowMap", shadowSampler.index)
            deferredGeometryProgram.set("shadowMapSize", Vector2(
                    shadowData.shadowMap.getWidth(),
                    shadowData.shadowMap.getHeight()
            ))
        }
        ambient.apply(deferredGeometryProgram)

        for (chunk in chunks) {
            chunk.render(deferredGeometryProgram)
        }

        deferredGeometryProgram.stop()
    }

    override fun renderForShadowMap(items: List<Renderable>, projection: Matrix4, view: Matrix4) {
        shadowProgram.start()
        shadowProgram.set("projection", projection)
        shadowProgram.set("view", view)

        for (item in items) {
            item.render(shadowProgram)
        }

        shadowProgram.stop()
    }

}