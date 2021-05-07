package graphics.rendertarget

import devices.Window
import graphics.rendertarget.attachments.AttachmentType

object RenderTargetManager {

    private val renderTargets = ArrayList<RenderTarget>()
    private lateinit var default: RenderTarget

    fun init(window: Window) {
        default = RenderTarget(window.width, window.height, AttachmentType.COLOR_TEXTURE, AttachmentType.DEPTH_TEXTURE, handle = 0)
    }

    fun getDefault() = default

    fun getAvailableTarget(vararg types: AttachmentType, width: Int = default.getWidth(), height: Int = default.getHeight()): RenderTarget {
        return renderTargets.find {
            it.matches(width, height, *types)
        } ?: createTarget(width, height, *types)
    }

    private fun createTarget(width: Int, height: Int, vararg types: AttachmentType): RenderTarget {
        val renderTarget = RenderTarget(width, height, *types)
        renderTargets += renderTarget
        return renderTarget
    }
}