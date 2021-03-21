package util

object Timer {

    private val timers = ArrayList<Pair<Int, Long>>()

    fun start(): Int {
        val id = timers.size
        val currentTime = System.currentTimeMillis()
        timers += Pair(id, currentTime)
        return id
    }

    fun getDelay(id: Int): Long {
        val timer = timers.find { timer -> timer.first == id } ?: return -1
        return System.currentTimeMillis() - timer.second
    }

}