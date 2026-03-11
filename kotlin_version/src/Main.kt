import java.util.Random

fun main() {
    Program().start()
}

class Program {

    private val threadsCount = 8
    private val random = Random()

    @Volatile
    private lateinit var stopFlags: BooleanArray
    private lateinit var startTimes: LongArray
    private lateinit var runDurations: IntArray

    @Volatile
    private var controllerDone = false

    fun start() {
        stopFlags    = BooleanArray(threadsCount)
        startTimes   = LongArray(threadsCount)
        runDurations = IntArray(threadsCount)

        for (i in 0 until threadsCount) {
            runDurations[i] = random.nextInt(20001) + 10000 
        }

        for (i in 0 until threadsCount) {
            val id = i
            startTimes[i] = System.currentTimeMillis()
            Thread { calculator(id) }.start()
        }

        Thread { controller() }.start()

        while (!controllerDone) {
            Thread.sleep(50)
        }
    }

    private fun controller() {
        var allFinished = false

        while (!allFinished) {
            allFinished = true

            for (i in 0 until threadsCount) {
                if (!stopFlags[i]) {
                    allFinished = false
                    val elapsed = System.currentTimeMillis() - startTimes[i]

                    if (elapsed >= runDurations[i]) {
                        stopFlags[i] = true
                    }
                }
            }

            Thread.sleep(1)
        }

        controllerDone = true 
    }

    private fun calculator(threadId: Int) {
        var sum = 0L
        var count = 0L
        val step = 2L

        while (!stopFlags[threadId]) {
            sum += step
            count++
        }

        println("Thread #$threadId --- Sum: $sum --- Elements: $count")
    }
}