package graphics.renderer

import environment.sky.SkyBox
import graphics.Camera
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.model.Quad
import graphics.rendertarget.RenderTargetManager
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.shadows.ShadowBox
import graphics.shadows.ShadowRenderer

class RenderEngine {

    private val forwardEngine = ForwardRenderEngine()
    private val deferredEngine = DeferredRenderEngine()

    private val shadowRenderer = ShadowRenderer()

    private val shadowBoxes = ArrayList<ShadowBox>()
    private val program = ShaderProgram.load("shaders/debug/2D.vert", "shaders/debug/depth.frag")
    private val sampler = Sampler(0)
    private val quad = Quad()

    operator fun plusAssign(box: ShadowBox) {
        shadowBoxes.add(box)
    }

    fun add(vararg boxes: ShadowBox) {
        shadowBoxes.addAll(boxes)
    }

    fun render(camera: Camera, ambient: AmbientLight, sun: Sun, sky: SkyBox, renderData: List<RenderData>) {
        val shadows = shadowRenderer.render(camera, sun, shadowBoxes, renderData)

        val forwardTarget = forwardEngine.prepare()
        val geometryTarget = deferredEngine.render(camera, ambient, sun, shadows, renderData, forwardTarget = forwardTarget)

        val forwardResultTarget = forwardEngine.render(camera, ambient, sun, sky, shadows, renderData, geometryTarget = geometryTarget)

        val defaultTarget = RenderTargetManager.getDefault()
        defaultTarget.start()
        defaultTarget.clear()

        sampler.bind(forwardResultTarget.getColorMap())
        program.start()
        program.set("sampler", sampler.index)
        quad.draw()
        program.stop()
    }

}