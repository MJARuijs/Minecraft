package graphics.entity

import graphics.Camera
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.model.animation.AnimatedModel
import graphics.model.animation.Joint
import graphics.renderer.Renderable
import graphics.renderer.Renderer
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.shadows.ShadowData
import math.vectors.Vector2

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class EntityRenderer : Renderer() {

    private val staticProgram = ShaderProgram.load("shaders/entities/entity.vert", "shaders/entities/entityForwardRendering.frag")
    private val animatedProgram = ShaderProgram.load("shaders/entities/animatedEntity.vert", "shaders/entities/entityForwardRendering.frag")
    private val deferredGeometryProgram = ShaderProgram.load("shaders/entities/entity.vert", "shaders/entities/entityGeometryPass.frag")

    override val shadowProgram = ShaderProgram.load("shaders/entities/shadowEntity.vert", "shaders/entities/shadowEntity.frag")
    override val deferredLightingProgram = ShaderProgram.load("shaders/debug/2D.vert", "shaders/entities/entityLightingPass.frag")

    override fun render(camera: Camera, ambient: AmbientLight, sun: Sun, entities: List<Renderable>, shadows: List<ShadowData>) {

        val staticEntities = entities.filter { renderable ->
            (renderable as Entity).model !is AnimatedModel
        }

        val animatedEntities = entities.filter { renderable ->
            (renderable as Entity).model is AnimatedModel
        }

        render(staticProgram, camera, ambient, sun, staticEntities, shadows)
        render(animatedProgram, camera, ambient, sun, animatedEntities, shadows)
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

    private fun render(program: ShaderProgram, camera: Camera, ambient: AmbientLight, sun: Sun, entities: List<Renderable>, shadows: List<ShadowData>) {
        program.start()

        if (shadows.isNotEmpty()) {
            val shadowData = shadows[0]
            val shadowSampler = Sampler(0)

            shadowSampler.bind(shadowData.shadowMap)

            program.set("shadowDistance", shadowData.shadowDistance)
            program.set("shadowMatrix", shadowData.getShadowMatrix())
            program.set("shadowMapSize", Vector2(
                    shadowData.shadowMap.getWidth(),
                    shadowData.shadowMap.getHeight()
            ))
            program.set("shadowMap", shadowSampler.index)
        }

        program.set("projection", camera.projectionMatrix)
        program.set("view", camera.viewMatrix)
        program.set("cameraPosition", camera.position)

        ambient.apply(program)
        sun.apply(program)

        for (entity in entities) {
            entity.render(program)
        }

        program.stop()
    }
}