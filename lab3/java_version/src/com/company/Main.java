package com.company;

public class Main {
    public static void main(String[] args) {
        // Налаштування лабораторної роботи
        int storageLimit = 5;                          // Макс. місткість
        int[] producerItems = {3, 5, 4};               // Різна кількість товарів для кожного виробника
        int[] consumerItems = {7, 5};                  // Різна кількість товарів для кожного споживача

        // Перевірка: загальна кількість вироблених = загальній кількості спожитих
        if (sum(producerItems) != sum(consumerItems)) {
            throw new IllegalArgumentException("Сумарна кiлькiсть вироблених i спожитих товарiв має збiгатися.");
        }

        Manager manager = new Manager(storageLimit);

        System.out.println("Розпочинаємо роботу");

        // Запускаємо виробників
        for (int i = 0; i < producerItems.length; i++) {
            new Thread(new Producer(i + 1, producerItems[i], manager)).start();
        }

        // Запускаємо споживачів
        for (int i = 0; i < consumerItems.length; i++) {
            new Thread(new Consumer(i + 1, consumerItems[i], manager)).start();
        }
    }

    private static int sum(int[] values) {
        int total = 0;
        for (int value : values) {
            total += value;
        }
        return total;
    }
}