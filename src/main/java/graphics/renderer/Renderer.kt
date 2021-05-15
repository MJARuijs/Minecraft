package graphics.renderer

import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.model.Quad
import graphics.shaders.ShaderProgram
import graphics.shadows.ShadowData
import math.matrices.Matrix4

abstract class Renderer {

    abstract val shadowProgram: ShaderProgram
    abstract val deferredLightingProgram: ShaderProgram

    private val quad = Quad()

    abstract fun render(camera: Camera, ambient: AmbientLight, sun: Sun, items: List<Renderable>, shadows: List<ShadowData>)

    abstract fun renderDeferredGeometry(camera: Camera, ambient: AmbientLight, items: List<Renderable>)

    fun renderDeferredLighting(camera: Camera, sun: Sun, positionSamplerIndex: Int, normalSamplerIndex: Int, colorSamplerIndex: Int) {
        deferredLightingProgram.start()

        deferredLightingProgram.set("cameraPosition", camera.position)
        deferredLightingProgram.set("positionMap", positionSamplerIndex)
        deferredLightingProgram.set("normalMap", normalSamplerIndex)
        deferredLightingProgram.set("colorMap", colorSamplerIndex)

        sun.apply(deferredLightingProgram)

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