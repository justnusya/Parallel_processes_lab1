package com.company;

public class Consumer implements Runnable {
    private final int id;
    private final int count;
    private final Manager manager;

    public Consumer(int id, int count, Manager manager) {
        this.id = id;
        this.count = count;
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= count; i++) {
                manager.emptySlots.acquire(); // Чекаємо, поки з'явиться товар
                manager.access.acquire();     // Захоплюємо доступ до сховища

                String item = manager.storage.remove(0); // Беремо перший товар (FIFO)
                System.out.println("Споживач " + id + " взяв: [" + item + "]. Залишилось: " + manager.storage.size());

                manager.access.release();    // Звільняємо сховище
                manager.fullSlots.release();  // Кажемо виробникам: "Звільнилося місце!"

                Thread.sleep(800);
            }
            System.out.println("<<< Споживач " + id + " закiнчив свою норму.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}