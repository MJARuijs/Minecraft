package devices

class DeviceManager(title: String, width: Int, height: Int, onWindowResized: (Int, Int) -> Unit) {

    val window = Window(title, width, height, onWindowResized)

    val keyboard = Keyboard(window.handle)
    val mouse = Mouse(window.handle, window.width, window.height)

    fun synchronize() {
        window.synchronize()
    }

    fun update() {
        keyboard.update()
        mouse.update()
    }

    fun destroy() {
        window.destroy()
    }
}