package graphics.rendertarget

import devices.Window
import graphics.rendertarget.attachments.AttachmentType
import graphics.textures.DataType

object RenderTargetManager {

    private val renderTargets = ArrayList<RenderTarget>()
    private lateinit var default: RenderTarget

    fun init(window: Window) {
        default = RenderTarget(window.width, window.height, Pair(AttachmentType.COLOR_TEXTURE, DataType.UNSIGNED_BYTE), Pair(AttachmentType.DEPTH_TEXTURE, DataType.UNSIGNED_BYTE), handle = 0)
    }

    fun getDefault() = default

    fun getAvailableTarget(vararg types: Pair<AttachmentType, DataType>, width: Int = default.getWidth(), height: Int = default.getHeight()): RenderTarget {
        return renderTargets.find {
            it.matches(width, height, *types)
        } ?: createTarget(width, height, *types)
    }

    private fun createTarget(width: Int, height: Int, vararg types: Pair<AttachmentType, DataType>): RenderTarget {
        val renderTarget = RenderTarget(width, height, *types)
        renderTargets += renderTarget
        return renderTarget
    }
}