package graphics.entity

import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.shadows.ShadowData
import math.matrices.Matrix4
import math.vectors.Vector2

class EntityRenderer {

    private val entityProgram = ShaderProgram.load("shaders/entities/entity.vert", "shaders/entities/entity.frag")

    fun render(camera: Camera, ambient: AmbientLight, sun: Sun, entities: List<Entity>, shadows: List<ShadowData>) {
        GraphicsContext.enable(GraphicsOption.ALPHA_BLENDING, GraphicsOption.DEPTH_TESTING)

        entityProgram.start()

        if (shadows.isNotEmpty()) {

            val shadowData = shadows[0]
            val shadowSampler = Sampler(0)

            shadowSampler.bind(shadowData.shadowMap)

            entityProgram.set("shadowPosition", shadowData.shadowDistance)
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

        GraphicsContext.disable(GraphicsOption.ALPHA_BLENDING, GraphicsOption.DEPTH_TESTING)
    }

    fun renderShadowed(projection: Matrix4, view: Matrix4, entities: List<Entity>) {
        GraphicsContext.disable(GraphicsOption.FACE_CULLING)
        entityProgram.start()

        entityProgram.set("projection", projection)
        entityProgram.set("view", view)

        for (entity in entities) {
            entity.render(entityProgram)
        }

        entityProgram.stop()
        GraphicsContext.enable(GraphicsOption.FACE_CULLING)

    }

}