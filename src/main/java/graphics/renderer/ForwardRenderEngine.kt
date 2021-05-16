package graphics.renderer

import environment.sky.SkyBox
import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.rendertarget.RenderTarget
import graphics.rendertarget.RenderTargetManager
import graphics.rendertarget.attachments.AttachmentType
import graphics.shadows.ShadowData
import graphics.textures.DataType
import org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT

class ForwardRenderEngine {

    private lateinit var forwardTarget: RenderTarget

    fun prepare(): RenderTarget {
        forwardTarget = RenderTargetManager.getAvailableTarget(Pair(AttachmentType.COLOR_TEXTURE, DataType.UNSIGNED_BYTE), Pair(AttachmentType.DEPTH_BUFFER, DataType.UNSIGNED_BYTE))
        return forwardTarget
    }

    fun render(camera: Camera, ambient: AmbientLight, sun: Sun, skyBox: SkyBox, shadows: List<ShadowData>, renderData: List<RenderData>, geometryTarget: RenderTarget): RenderTarget {
        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.ALPHA_BLENDING)

        geometryTarget.renderTo(forwardTarget, GL_DEPTH_BUFFER_BIT)
        forwardTarget.start()

        skyBox.render(camera)

        for (data in renderData) {
            if (data.type == RenderType.FORWARD) {
                data.renderer.render(camera, ambient, sun, data.data, shadows)
            }
        }

        GraphicsContext.disable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.ALPHA_BLENDING)
        return forwardTarget
    }

}