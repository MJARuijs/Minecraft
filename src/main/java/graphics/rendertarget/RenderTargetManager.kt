package graphics.rendertarget

import devices.Window

object RenderTargetManager {

    private lateinit var default: RenderTarget
    private lateinit var firstTarget: RenderTarget
    private lateinit var secondTarget: RenderTarget

    private var firstTargetAvailable = true

    fun init(window: Window) {
        default = RenderTarget(window.width, window.height, 0)
        firstTarget = RenderTarget(window.width, window.height)
        secondTarget = RenderTarget(window.width, window.height)
    }

    fun getDefault() = default

    fun get() = if (firstTargetAvailable) {
        firstTargetAvailable = false
        firstTarget
    } else {
        firstTargetAvailable = true
        secondTarget
    }

}