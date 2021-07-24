package graphics.shadows

import graphics.Camera
import graphics.lights.Sun
import graphics.renderer.RenderData
import graphics.rendertarget.RenderTargetManager
import graphics.rendertarget.attachments.AttachmentType

class ShadowRenderer {

    private val renderTarget = RenderTargetManager.getAvailableTarget(false, AttachmentType.DEPTH_TEXTURE, width = SHADOW_MAP_SIZE, height = SHADOW_MAP_SIZE)

    fun render(camera: Camera, sun: Sun, shadowBoxes: List<ShadowBox>, renderData: List<RenderData>): List<ShadowData> {
        val shadowData = ArrayList<ShadowData>()

        renderTarget.start()
        renderTarget.clear()

        for (box in shadowBoxes) {
            box.updateBox(camera, sun)

            renderData.forEach {
                it.renderer.renderForShadowMap(it.data, box.getProjectionMatrix(), box.getViewMatrix())
            }

            shadowData += ShadowData(
                    box.getProjectionMatrix(),
                    box.getViewMatrix(),
                    box.maxDistance,
                    renderTarget.getDepthTexture()
            )
        }

        renderTarget.stop()

        return shadowData
    }

    companion object {
        private const val SHADOW_MAP_SIZE = 4096 * 4
    }
}