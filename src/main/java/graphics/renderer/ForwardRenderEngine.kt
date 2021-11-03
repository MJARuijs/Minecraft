package graphics.renderer

import environment.sky.SkyBox
import game.camera.Camera
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.rendertarget.RenderTarget
import graphics.rendertarget.RenderTargetManager
import graphics.shadows.ShadowData
import graphics.shadows.StencilShadowRenderer
import org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT

class ForwardRenderEngine(var multiSampled: Boolean) {

    private lateinit var forwardTarget: RenderTarget

    fun prepare(): RenderTarget {
        forwardTarget = RenderTargetManager.getAvailableTarget(multiSampled, 1, 0, 1, 0)
        return forwardTarget
    }

    fun render(camera: Camera, ambient: AmbientLight, sun: Sun, skyBox: SkyBox, shadows: List<ShadowData>, renderData: List<RenderData>, stencilShadowRenderer: StencilShadowRenderer, geometryTarget: RenderTarget): RenderTarget {

        if (renderData.any { data -> data.type == RenderType.DEFERRED }) {
            geometryTarget.renderTo(forwardTarget, GL_DEPTH_BUFFER_BIT)
            forwardTarget.start()
        } else {
            forwardTarget.start()
            stencilShadowRenderer.disableDepthBuffer()
            forwardTarget.clear()
        }
//        forwardTarget.start()
//        forwardTarget.clear()
        skyBox.render(camera)

        for (data in renderData) {
            if (data.type == RenderType.FORWARD) {
                data.renderer.render(camera, ambient, sun, data.data, shadows)
            }
        }
//        forwardTarget.stop()

        return forwardTarget
    }

}