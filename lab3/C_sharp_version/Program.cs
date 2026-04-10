using System;
using System.Collections.Generic;
using System.Threading;

namespace ProducerConsumer
{
    class Program
    {
        private static Semaphore _access; // М'ютекс (один потік)
        private static Semaphore _fullSlots; // Кількість вільних місць
        private static Semaphore _emptySlots; // Кількість заповнених місць

        private static List<string> _storage = new List<string>();

        static void Main(string[] args)
        {
            int storageSize = 5; 
            int[] producerItems = { 3, 5, 4 }; // Різна к-ть товарів для кожного виробника
            int[] consumerItems = { 7, 5 };    // Різна к-ть товарів для кожного споживача

            _access = new Semaphore(1, 1);
            _fullSlots = new Semaphore(storageSize, storageSize);
            _emptySlots = new Semaphore(0, storageSize);

            if (Sum(producerItems) != Sum(consumerItems))
            {
                throw new InvalidOperationException("Сумарна кiлькiсть вироблених i спожитих товарів має збiгатися.");
            }

            Console.WriteLine("--- Старт роботи ---");

            // Запуск виробників
            for (int i = 0; i < producerItems.Length; i++)
            {
                Thread pThread = new Thread(Producer);
                pThread.Start(new WorkerInfo { Id = i + 1, Count = producerItems[i] });
            }

            // Запуск споживачів
            for (int i = 0; i < consumerItems.Length; i++)
            {
                Thread cThread = new Thread(Consumer);
                cThread.Start(new WorkerInfo { Id = i + 1, Count = consumerItems[i] });
            }
        }
        
        static int Sum(int[] values)
        {
            int total = 0;

            for (int i = 0; i < values.Length; i++)
            {
                total += values[i];
            }

            return total;
        }

        static void Producer(object obj)
        {
            var info = (WorkerInfo)obj;
            for (int i = 1; i <= info.Count; i++)
            {
                _fullSlots.WaitOne(); // Чекаємо на вільне місце
                _access.WaitOne();    // Заходимо в критичну секцію, більше ніхто не заходить

                string item = $"Продукт {i} (вiд Виробника {info.Id})";
                _storage.Add(item);
                Console.WriteLine($"Виробник {info.Id} додав: {item}. Всього у сховищi: {_storage.Count}");

                _access.Release();    // Виходимо з критичної секції
                _emptySlots.Release(); // Збільшуємо лічильник заповнених місць

                Thread.Sleep(500); 
            }
            Console.WriteLine($"--- Виробник {info.Id} завершив роботу ---");
        }

        static void Consumer(object obj)
        {
            var info = (WorkerInfo)obj;
            for (int i = 1; i <= info.Count; i++)
            {
                _emptySlots.WaitOne(); // Чекаємо, поки з'явиться хоча б один продукт
                _access.WaitOne();     // Заходимо в критичну секцію

                string item = _storage[0];
                _storage.RemoveAt(0);
                Console.WriteLine($"Споживач {info.Id} взяв: {item}. Залишилось: {_storage.Count}");

                _access.Release();    // Виходимо з критичної секції
                _fullSlots.Release();  // Звільняємо місце у сховищі

                Thread.Sleep(800); 
            }
            Console.WriteLine($"--- Споживач {info.Id} завершив роботу ---");
        }
    }

    class WorkerInfo
    {
        public int Id { get; set; }
        // Кількість товарів для виробника або споживача
        public int Count { get; set; }
    }
}