package com.company;

public class Producer implements Runnable {
    private final int id;
    private final int count;
    private final Manager manager;

    public Producer(int id, int count, Manager manager) {
        this.id = id;
        this.count = count;
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= count; i++) {
                manager.fullSlots.acquire(); // Чекаємо, поки звільниться місце
                manager.access.acquire();    // Захоплюємо доступ до сховища

                String item = "Продукт " + i + " від Виробника #" + id;
                manager.storage.add(item);
                System.out.println("Виробник " + id + " додав: [" + item + "]. Всього: " + manager.storage.size());

                manager.access.release();    // Звільняємо сховище
                manager.emptySlots.release(); // Кажемо споживачам: "Є товар!"

                Thread.sleep(500); // Імітація часу виробництва
            }
            System.out.println(">>> Виробник " + id + " закiнчив свою норму.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}