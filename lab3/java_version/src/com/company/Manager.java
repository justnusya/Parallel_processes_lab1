package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Manager {
    // М'ютекс для доступу до списку (1 потік працює зі сховищем)
    public final Semaphore access;
    // Лічильник вільних місць у сховищі
    public final Semaphore fullSlots;
    // Лічильник наявних товарів
    public final Semaphore emptySlots;
    public final List<String> storage = new ArrayList<>();

    public Manager(int storageSize) {
        // Дозволяємо лише одному потоку заходити в сховище
        this.access = new Semaphore(1);
        // Спочатку всі місця вільні
        this.fullSlots = new Semaphore(storageSize);
        // Товарів спочатку немає
        this.emptySlots = new Semaphore(0);
    }
}