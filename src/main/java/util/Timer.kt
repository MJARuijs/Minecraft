package util

object Timer {

    private val timers = ArrayList<Pair<Int, Long>>()

    fun start(): Int {
        val id = timers.size
        val currentTime = System.nanoTime()
        timers += Pair(id, currentTime)
        return id
    }

    fun getDelay(id: Int): Float {
        val timer = timers.find { timer -> timer.first == id } ?: return -1f
        return (System.nanoTime() - timer.second).toFloat() / 1000000.0f
    }

}