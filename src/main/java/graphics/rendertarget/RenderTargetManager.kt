package graphics.rendertarget

import devices.Window

object RenderTargetManager {

    private val renderTargets = ArrayList<RenderTarget>()

    private lateinit var default: RenderTarget

    fun init(window: Window) {
        default = RenderTarget(window.width, window.height, false, 1, 0, 1, 0, handle = 0)
    }

    fun getDefault() = default

    fun getAvailableTarget(multiSampled: Boolean, numberOfColorTextures: Int, numberOfColorBuffers: Int, numberOfDepthTextures: Int, numberOfDepthBuffers: Int, width: Int = default.getWidth(), height: Int = default.getHeight()): RenderTarget {
        return renderTargets.find { target ->
            target.matches(width, height, multiSampled, numberOfColorTextures, numberOfColorBuffers, numberOfDepthTextures, numberOfDepthBuffers)
        } ?: createTarget(width, height, multiSampled, numberOfColorTextures, numberOfColorBuffers, numberOfDepthTextures, numberOfDepthBuffers)
    }

    private fun createTarget(width: Int, height: Int, multiSampled: Boolean, numberOfColorTextures: Int, numberOfColorBuffers: Int, numberOfDepthTextures: Int, numberOfDepthBuffers: Int): RenderTarget {
        val renderTarget = RenderTarget(width, height, multiSampled, numberOfColorTextures, numberOfColorBuffers, numberOfDepthTextures, numberOfDepthBuffers)
        renderTargets += renderTarget
        return renderTarget
    }
}