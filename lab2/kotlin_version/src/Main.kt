import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.Random

const val DIM = 100_000_000
// Масив чисел для заповнення
lateinit var arr: IntArray

fun main() {
    arr = IntArray(DIM)
    initArr()

    // Масив кількостей потоків для тестування
    val threadTests = arrayOf(1, 2, 4, 8)

    for (threadNum in threadTests) {
        val startTime = System.currentTimeMillis()

        val result = parallelMin(threadNum)

        val elapsed = System.currentTimeMillis() - startTime 

        println("Threads: $threadNum")
        println("Min value: ${result.first}") 
        println("Index: ${result.second}")
        println("Elapsed time: $elapsed ms") 
    }
}

fun initArr() {
    val rnd = Random()
    for (i in 0 until DIM) {
        arr[i] = rnd.nextInt(1_000_000) + 1
    }
    arr[5_000_000] = -100 

fun parallelMin(threadNum: Int): Pair<Int, Int> {
    // Створюємо пул потоків з заданою кількістю потоків
    val executor = Executors.newFixedThreadPool(threadNum)
    // Кількість елементів, які обробляє один потік
    val part = DIM / threadNum

    // Список завдань для потоків
    val tasks = mutableListOf<Callable<Pair<Int, Int>>>()

    // Створюємо завдання для кожного потоку
    for (i in 0 until threadNum) {
        val start = i * part
        val end = if (i == threadNum - 1) DIM else start + part  // останній потік обробляє залишок

        // Додаємо Callable, яке знаходить мінімум у своїй частині масиву
        tasks.add(Callable {
            var localMin = arr[start]
            var localIndex = start

            for (j in start until end) {
                if (arr[j] < localMin) {
                    localMin = arr[j]
                    localIndex = j
                }
            }
            Pair(localMin, localIndex) // повертаємо локальний мінімум і індекс
        })
    }

    // Виконуємо всі завдання паралельно і отримуємо список Future
    val futures = executor.invokeAll(tasks)

    // Закриваємо пул потоків після завершення завдань
    executor.shutdown()

    // Змінні для глобального мінімуму
    var globalMin = Int.MAX_VALUE
    var globalIndex = -1

    // Перевіряємо результати всіх потоків і знаходимо глобальний мінімум
    for (future in futures) {
        val res = future.get() // отримуємо результат з Future
        if (res.first < globalMin) {
            globalMin = res.first
            globalIndex = res.second
        }
    }

    // Повертаємо глобальний мінімум і його індекс
    return Pair(globalMin, globalIndex)
}