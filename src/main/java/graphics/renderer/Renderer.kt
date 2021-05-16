package graphics.renderer

import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.model.Quad
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.shadows.ShadowData
import math.matrices.Matrix4
import math.vectors.Vector2

abstract class Renderer {

    abstract val shadowProgram: ShaderProgram
    abstract val deferredLightingProgram: ShaderProgram

    private val quad = Quad()

    abstract fun render(camera: Camera, ambient: AmbientLight, sun: Sun, items: List<Renderable>, shadows: List<ShadowData>)

    abstract fun renderDeferredGeometry(camera: Camera, ambient: AmbientLight, items: List<Renderable>, shadows: List<ShadowData>)

    fun renderDeferredLighting(camera: Camera, sun: Sun, vararg samplers: Pair<String, Int>, shadows: List<ShadowData>) {
        deferredLightingProgram.start()
        deferredLightingProgram.set("cameraPosition", camera.position)

        if (shadows.isNotEmpty()) {
            val shadowData = shadows[0]
            val shadowSampler = Sampler(6)

            shadowSampler.bind(shadowData.shadowMap)

            deferredLightingProgram.set("shadowMap", shadowSampler.index)
            deferredLightingProgram.set("shadowMapSize", Vector2(
                    shadowData.shadowMap.getWidth(),
                    shadowData.shadowMap.getHeight()
            ))
        }

        sun.apply(deferredLightingProgram)

        for (sampler in samplers) {
            deferredLightingProgram.set(sampler.first, sampler.second)
        }

        quad.draw()
        deferredLightingProgram.stop()
    }

    fun renderBlack(items: List<Renderable>, projection: Matrix4, view: Matrix4) {
        GraphicsContext.enable(GraphicsOption.ALPHA_BLENDING, GraphicsOption.DEPTH_TESTING)

        shadowProgram.start()
        shadowProgram.set("projection", projection)
        shadowProgram.set("view", view)

        for (item in items) {
            item.render(shadowProgram)
        }

        shadowProgram.stop()

        GraphicsContext.disable(GraphicsOption.ALPHA_BLENDING, GraphicsOption.DEPTH_TESTING)
    }

}