package graphics.rendertarget

import devices.Window

object RenderTargetManager {

    private val renderTargets = ArrayList<RenderTarget>()
    private var width = 0
    private var height = 0

    fun init(window: Window) {
        width = window.width
        height = window.height
    }

    fun getAspectRatio(): Float {
        return width.toFloat() / height.toFloat()
    }

    fun getAvailableTarget(multiSampled: Boolean, numberOfColorTextures: Int, numberOfColorBuffers: Int, numberOfDepthTextures: Int, numberOfDepthBuffers: Int, width: Int = this.width, height: Int = this.height): RenderTarget {
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