object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        val start = System.currentTimeMillis()
        println("A")

        Thread {
            for (i in 0 until 10000) {
                println(i)
//            x += i
//            x -= i
            }
        }.start()

        val end = System.currentTimeMillis()

        println("B: ${end - start}")
    }

}