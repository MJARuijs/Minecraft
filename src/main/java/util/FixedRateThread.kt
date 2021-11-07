package util

class FixedRateThread(ticksPerSecond: Float, private val task: () -> Unit) {

    private val runTime = (1.0f / ticksPerSecond) * 1000L

    private var shouldStop = false

    fun run() {
        shouldStop = false

        println(runTime)
        Thread {
            while (!shouldStop) {
                val startTime = System.currentTimeMillis()
                task.invoke()
                val endTime = System.currentTimeMillis()
                val timeDifference = endTime - startTime
                val waitTime = runTime.toLong() - timeDifference

                if (waitTime > 0) {
                    Thread.sleep(waitTime)
                }
            }
        }.start()
    }

    fun stop() {
        shouldStop = true
    }

}