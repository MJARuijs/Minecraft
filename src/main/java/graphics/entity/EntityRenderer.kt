package graphics.entity

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
import math.vectors.Vector2

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class EntityRenderer : Renderer() {

    private val entityProgram = ShaderProgram.load("shaders/entities/entity.vert", "shaders/entities/entityForwardRendering.frag")
    private val deferredGeometryProgram = ShaderProgram.load("shaders/entities/entity.vert", "shaders/entities/entityGeometryPass.frag")

    override val shadowProgram = ShaderProgram.load("shaders/entities/shadowEntity.vert", "shaders/entities/shadowEntity.frag")
    override val deferredLightingProgram = ShaderProgram.load("shaders/debug/2D.vert", "shaders/entities/entityLightingPass.frag")

    override fun render(camera: Camera, ambient: AmbientLight, sun: Sun, entities: List<Renderable>, shadows: List<ShadowData>) {
        entityProgram.start()

        if (shadows.isNotEmpty()) {

            val shadowData = shadows[0]
            val shadowSampler = Sampler(0)

            shadowSampler.bind(shadowData.shadowMap)

            entityProgram.set("shadowDistance", shadowData.shadowDistance)
            entityProgram.set("shadowMatrix", shadowData.getShadowMatrix())
            entityProgram.set("shadowMapSize", Vector2(
                    shadowData.shadowMap.getWidth(),
                    shadowData.shadowMap.getHeight()
            ))
            entityProgram.set("shadowMap", shadowSampler.index)
        }

        entityProgram.set("projection", camera.projectionMatrix)
        entityProgram.set("view", camera.viewMatrix)
        entityProgram.set("cameraPosition", camera.position)

        ambient.apply(entityProgram)
        sun.apply(entityProgram)

        for (entity in entities) {
            entity.render(entityProgram)
        }

        entityProgram.stop()
    }

    override fun renderDeferredGeometry(camera: Camera, ambient: AmbientLight, entities: List<Renderable>, shadows: List<ShadowData>) {
        deferredGeometryProgram.start()
        deferredGeometryProgram.set("projection", camera.projectionMatrix)
        deferredGeometryProgram.set("view", camera.viewMatrix)

        ambient.apply(deferredGeometryProgram)

        for (entity in entities) {
            entity.render(deferredGeometryProgram)
        }

        deferredGeometryProgram.stop()
    }
}