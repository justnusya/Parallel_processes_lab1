import java.util.*
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

// Допоміжний клас для передачі даних потокові
data class WorkerInfo(val id: Int, val count: Int)

object ProducerConsumerApp {
    private lateinit var access: Semaphore      // М'ютекс для критичної секції (сховище)
    private lateinit var fullSlots: Semaphore   // Лічильник доступних вільних місць
    private lateinit var emptySlots: Semaphore  // Лічильник товарів, що знаходяться у сховищі

    private val storage: MutableList<String> = mutableListOf()

    @JvmStatic
    fun main(args: Array<String>) {
        val storageLimit = 5                           // Максимальна місткість сховища
        val producerItems = intArrayOf(3, 5, 4)        // Різна кількість товарів для кожного виробника
        val consumerItems = intArrayOf(7, 5)           // Різна кількість товарів для кожного споживача

        // Перевірка: загальна кількість вироблених повинна дорівнювати загальній кількості спожитих
        if (producerItems.sum() != consumerItems.sum()) {
            throw IllegalArgumentException("Загальна кiлькість вироблених товарiв має дорiвнювати загальнiй кількостi спожитих.")
        }

        // Ініціалізація семафорів
        access = Semaphore(1)                 // Лише 1 потік може мати доступ до списку за раз
        fullSlots = Semaphore(storageLimit)   // Спочатку всі місця вільні
        emptySlots = Semaphore(0)             // Спочатку 0 товарів доступно

        println("--- Розпочинаємо роботу (Kotlin) ---")

        // Запускаємо виробників
        for (i in producerItems.indices) {
            val info = WorkerInfo(id = i + 1, count = producerItems[i])
            thread { producer(info) }
        }

        // Запускаємо споживачів
        for (i in consumerItems.indices) {
            val info = WorkerInfo(id = i + 1, count = consumerItems[i])
            thread { consumer(info) }
        }
    }

    private fun producer(info: WorkerInfo) {
        for (i in 1..info.count) {
            try {
                fullSlots.acquire() // Чекаємо вільне місце
                access.acquire()    // Блокуємо доступ до сховища

                val item = "Товар $i від Виробника №${info.id}"
                storage.add(item)
                println("Виробник ${info.id} додав: $item. Усього в сховищі: ${storage.size}")

                access.release()    // Звільняємо доступ до сховища
                emptySlots.release() // Сигналізуємо споживачам, що товар готовий

                Thread.sleep(500)   // Імітуємо час виробництва
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        println(">>> Виробник ${info.id} завершив свою норму.")
    }

    private fun consumer(info: WorkerInfo) {
        for (i in 1..info.count) {
            try {
                emptySlots.acquire() // Чекаємо, поки товар буде доступним
                access.acquire()     // Блокуємо доступ до сховища

                val item = storage.removeAt(0) // Видаляємо перший товар (FIFO)
                println("Споживач ${info.id} взяв: $item. Залишилось: ${storage.size}")

                access.release()    // Звільняємо доступ до сховища
                fullSlots.release()  // Сигналізуємо виробникам, що місце тепер вільне

                Thread.sleep(800)   // Імітуємо час споживання
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        println("<<< Споживач ${info.id} завершив свою норму.")
    }
}