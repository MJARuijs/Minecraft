package graphics.renderer

import graphics.Camera
import graphics.GraphicsContext
import graphics.GraphicsOption
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.rendertarget.RenderTarget
import graphics.rendertarget.RenderTargetManager
import graphics.rendertarget.attachments.AttachmentType
import graphics.samplers.Sampler
import graphics.shadows.ShadowData

class DeferredRenderEngine {

    private lateinit var geometryTarget: RenderTarget

    private val positionSampler = Sampler(0)
    private val normalSampler = Sampler(1)
    private val colorSampler = Sampler(2)

    fun render(camera: Camera, ambient: AmbientLight, sun: Sun, shadows: List<ShadowData>, renderData: List<RenderData>, forwardTarget: RenderTarget): RenderTarget {
        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.ALPHA_BLENDING)
        geometryTarget = RenderTargetManager.getAvailableTarget(AttachmentType.COLOR_TEXTURE, AttachmentType.COLOR_TEXTURE, AttachmentType.COLOR_TEXTURE, AttachmentType.DEPTH_TEXTURE)
        geometryTarget.start()
        geometryTarget.clear()

        for (data in renderData) {
            if (data.type == RenderType.DEFERRED) {
                data.renderer.renderDeferredGeometry(camera, ambient, data.data)
            }
        }

        geometryTarget.stop()

        forwardTarget.start()
        forwardTarget.clear()

        positionSampler.bind(geometryTarget.getColorMap(0))
        normalSampler.bind(geometryTarget.getColorMap(1))
        colorSampler.bind(geometryTarget.getColorMap(2))

        for (data in renderData) {
            if (data.type == RenderType.DEFERRED) {
                data.renderer.renderDeferredLighting(camera, sun, positionSampler.index, normalSampler.index, colorSampler.index)
            }
        }

        GraphicsContext.disable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.ALPHA_BLENDING)

        return geometryTarget
    }

}