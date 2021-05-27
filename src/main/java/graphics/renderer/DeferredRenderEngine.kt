package graphics.renderer

import graphics.Camera
import graphics.lights.AmbientLight
import graphics.lights.Sun
import graphics.rendertarget.RenderTarget
import graphics.rendertarget.RenderTargetManager
import graphics.rendertarget.attachments.AttachmentType
import graphics.samplers.Sampler
import graphics.shadows.ShadowData

class DeferredRenderEngine {

    private lateinit var geometryTarget: RenderTarget

    private val multiSampled = true

    private val positionSampler = Sampler(0, multiSampled)
    private val surfaceNormal = Sampler(1, multiSampled)
    private val colorSampler = Sampler(2, multiSampled)
    private val normalSampler = Sampler(3, multiSampled)
    private val specularSampler = Sampler(4, multiSampled)
    private val shadowCoordinateSampler = Sampler(5)

    fun render(camera: Camera, ambient: AmbientLight, sun: Sun, shadows: List<ShadowData>, renderData: List<RenderData>, forwardTarget: RenderTarget): RenderTarget {
        geometryTarget = RenderTargetManager.getAvailableTarget(
                multiSampled,
                AttachmentType.COLOR_TEXTURE,
                AttachmentType.COLOR_TEXTURE,
                AttachmentType.COLOR_TEXTURE,
                AttachmentType.COLOR_TEXTURE,
                AttachmentType.COLOR_TEXTURE,
                AttachmentType.COLOR_TEXTURE,
                AttachmentType.DEPTH_TEXTURE
        )

        if (renderData.none { data -> data.type == RenderType.DEFERRED }) {
            return geometryTarget
        }

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
                        Pair("surfaceNormalMap", surfaceNormal.index),
                        Pair("colorMap", colorSampler.index),
                        Pair("normalMap", normalSampler.index),
                        Pair("specularMap", specularSampler.index),
                        Pair("shadowCoordinatesMap", shadowCoordinateSampler.index),
                        shadows = shadows
                )
            }
        }

        return geometryTarget
    }

}