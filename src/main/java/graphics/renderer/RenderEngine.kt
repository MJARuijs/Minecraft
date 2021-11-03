package graphics.renderer

import environment.sky.SkyBox
import game.camera.Camera
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.model.Quad
import graphics.rendertarget.RenderTargetManager
import graphics.samplers.Sampler
import graphics.shaders.ShaderProgram
import graphics.shadows.ShadowBox
import graphics.shadows.ShadowRenderer
import graphics.shadows.StencilShadowRenderer
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER
import org.lwjgl.opengl.GL30.glBindFramebuffer

class RenderEngine(multiSampled: Boolean) {

    private val forwardEngine = ForwardRenderEngine(multiSampled)
    private val deferredEngine = DeferredRenderEngine(multiSampled)

    private val shadowRenderer = ShadowRenderer()
    private val stencilShadowRenderer = StencilShadowRenderer()

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
        val geometryTarget = deferredEngine.render(camera, ambient, sun, shadows, renderData, forwardTarget)

        val forwardResultTarget = forwardEngine.render(camera, ambient, sun, sky, shadows, renderData, stencilShadowRenderer, geometryTarget)
        forwardResultTarget.renderToScreen(GL_COLOR_BUFFER_BIT)

//        glBindFramebuffer(GL_FRAMEBUFFER, 0)
//        glClear(GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)
//        program.start()
//        sampler.bind(forwardResultTarget.getColorMap())
//        program.set("sampler", sampler.index)
//        quad.draw()
//        program.stop()
    }

}