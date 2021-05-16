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
import graphics.textures.DataType

class DeferredRenderEngine {

    private lateinit var geometryTarget: RenderTarget

    private val positionSampler = Sampler(0)
    private val surfaceNormal = Sampler(1)
    private val colorSampler = Sampler(2)
    private val normalSampler = Sampler(3)
    private val specularSampler = Sampler(4)
    private val shadowCoordinateSampler = Sampler(5)

    fun render(camera: Camera, ambient: AmbientLight, sun: Sun, shadows: List<ShadowData>, renderData: List<RenderData>, forwardTarget: RenderTarget): RenderTarget {
        GraphicsContext.enable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.ALPHA_BLENDING)
        geometryTarget = RenderTargetManager.getAvailableTarget(
                Pair(AttachmentType.COLOR_TEXTURE, DataType.FLOAT),
                Pair(AttachmentType.COLOR_TEXTURE, DataType.FLOAT),
                Pair(AttachmentType.COLOR_TEXTURE, DataType.FLOAT),
                Pair(AttachmentType.COLOR_TEXTURE, DataType.FLOAT),
                Pair(AttachmentType.COLOR_TEXTURE, DataType.FLOAT),
                Pair(AttachmentType.COLOR_TEXTURE, DataType.FLOAT),
                Pair(AttachmentType.DEPTH_TEXTURE, DataType.UNSIGNED_BYTE)
        )

        geometryTarget.start()
        geometryTarget.clear()

        for (data in renderData) {
            if (data.type == RenderType.DEFERRED) {
                data.renderer.renderDeferredGeometry(camera, ambient, data.data, shadows)
            }
        }

        geometryTarget.stop()

        forwardTarget.start()
        forwardTarget.clear()

        positionSampler.bind(geometryTarget.getColorMap(0))
        surfaceNormal.bind(geometryTarget.getColorMap(1))
        colorSampler.bind(geometryTarget.getColorMap(2))
        normalSampler.bind(geometryTarget.getColorMap(3))
        specularSampler.bind(geometryTarget.getColorMap(4))
        shadowCoordinateSampler.bind(geometryTarget.getColorMap(5))

        for (data in renderData) {
            if (data.type == RenderType.DEFERRED) {
                data.renderer.renderDeferredLighting(camera, sun,
                        Pair("positionMap", positionSampler.index),
                        Pair("surfaceNormal", surfaceNormal.index),
                        Pair("colorMap", colorSampler.index),
                        Pair("normalMap", normalSampler.index),
                        Pair("specularMap", specularSampler.index),
                        Pair("shadowCoordinatesMap", shadowCoordinateSampler.index),
                        shadows = shadows
                )
            }
        }

//        GraphicsContext.disable(GraphicsOption.DEPTH_TESTING, GraphicsOption.FACE_CULLING, GraphicsOption.ALPHA_BLENDING)

        return geometryTarget
    }

}