package graphics.rendertarget

import devices.Window

object RenderTargetManager {

    private lateinit var firstTarget: RenderTarget
    private lateinit var secondTarget: RenderTarget

    private var firstTargetAvailable = true

    fun init(window: Window) {
        firstTarget = RenderTarget(window.width, window.height)
        secondTarget = RenderTarget(window.width, window.height)
    }

    fun get() = if (firstTargetAvailable) {
        firstTargetAvailable = false
        firstTarget
    } else {
        firstTargetAvailable = true
        secondTarget
    }

}