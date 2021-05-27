package graphics.renderer

import environment.sky.SkyBox
import graphics.Camera
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.rendertarget.RenderTarget
import graphics.rendertarget.RenderTargetManager
import graphics.rendertarget.attachments.AttachmentType
import graphics.shadows.ShadowData
import org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT

class ForwardRenderEngine {

    private lateinit var forwardTarget: RenderTarget

    fun prepare(): RenderTarget {
        forwardTarget = RenderTargetManager.getAvailableTarget(true, AttachmentType.COLOR_TEXTURE, AttachmentType.DEPTH_TEXTURE)
        return forwardTarget
    }

    fun render(camera: Camera, ambient: AmbientLight, sun: Sun, skyBox: SkyBox, shadows: List<ShadowData>, renderData: List<RenderData>, geometryTarget: RenderTarget): RenderTarget {

        if (renderData.any { data -> data.type == RenderType.DEFERRED }) {
            geometryTarget.renderTo(forwardTarget, GL_DEPTH_BUFFER_BIT)
            forwardTarget.start()
        } else {
            forwardTarget.start()
            forwardTarget.clear()
        }

        skyBox.render(camera)

        for (data in renderData) {
            if (data.type == RenderType.FORWARD) {
                data.renderer.render(camera, ambient, sun, data.data, shadows)
            }
        }

        return forwardTarget
    }

}