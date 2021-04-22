package util

class StoppableThread(private val lambda: () -> Unit) : Thread() {

    @Volatile
    private var running = true

    fun stopRunning() {
        running = false
    }

    override fun run() {
        while (running) {
            lambda()
        }
    }

}