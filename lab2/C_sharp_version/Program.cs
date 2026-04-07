using System;
using System.Threading;
using System.Diagnostics;

namespace ThreadMinSharp
{
    class Program
    {
        private static int dim = 1000000000;

        private static int threadNum;

        private Thread[] thread;

        private int[] arr = new int[dim];

        static void Main(string[] args)
        {
            Program main = new Program();
            main.InitArr();

            Stopwatch sw = new Stopwatch();

            // Набір тестів з різною кількістю потоків
            int[] threadTests = { 1, 2, 4, 8 };

            foreach (int t in threadTests)
            {
                threadNum = t;
                main.thread = new Thread[threadNum];

                main.threadCount = 0;
                main.globalMin = int.MaxValue;
                main.globalIndex = -1;

                sw.Reset();
                sw.Start();

                // Запуск паралельного пошуку мінімуму
                var result = main.ParallelMin();

                sw.Stop();

                Console.WriteLine("Threads: " + threadNum);
                Console.WriteLine("Min value: " + result.min);
                Console.WriteLine("Index: " + result.index);
                Console.WriteLine("Elapsed time: " + sw.ElapsedMilliseconds + " ms");
            }

            Console.ReadKey();
        }

        private void InitArr()
        {
            Random rnd = new Random();

            for (int i = 0; i < dim; i++)
            {
                arr[i] = rnd.Next(1, 1000000);
            }

            arr[5000000] = -100;
        }
        // діапазон індексів
        class Bound
        {
            public Bound(int startIndex, int finishIndex)
            {
                StartIndex = startIndex;
                FinishIndex = finishIndex;
            }

            public int StartIndex { get; set; }
            public int FinishIndex { get; set; }
        }

        // Лічильник завершених потоків
        private int threadCount = 0;

        // Об’єкт для синхронізації лічильника потоків
        private readonly object lockerForCount = new object();

        // Об’єкт для синхронізації глобального мінімуму
        private readonly object lockerForMin = new object();

        // Глобальний мінімум
        private int globalMin = int.MaxValue;

        // Індекс глобального мінімуму
        private int globalIndex = -1;

        //ділить масив на частини, запускає потоки і чекає їх завершення.
        private (int min, int index) ParallelMin()
        {
            int part = dim / threadNum;

            for (int i = 0; i < threadNum; i++)
            {
                int start = i * part;

                // Останній потік бере залишок масиву
                int end = (i == threadNum - 1) ? dim : start + part;

                thread[i] = new Thread(StarterThread);

                // Передаємо діапазон індексів у потік
                thread[i].Start(new Bound(start, end));
            }

            // Очікування завершення всіх потоків
            lock (lockerForCount)
            {
                while (threadCount < threadNum)
                {
                    // Потік переходить у стан очікування
                    Monitor.Wait(lockerForCount);
                }
            }

            return (globalMin, globalIndex);
        }

        //потік шукає мінімум у своїй частині і оновлює глобальний мінімум.
        private void StarterThread(object param)
        {
            if (param is Bound b)
            {
                // Локальний пошук мінімуму
                var result = PartMin(b.StartIndex, b.FinishIndex);

                // Синхронізоване оновлення глобального мінімуму
                lock (lockerForMin)
                {
                    if (result.min < globalMin)
                    {
                        globalMin = result.min;
                        globalIndex = result.index;
                    }
                }

                // Повідомлення про завершення потоку
                IncThreadCount();
            }
        }

        //збільшує лічильник завершених потоків і сигналізує головному потоку.
        private void IncThreadCount()
        {
            lock (lockerForCount)
            {
                threadCount++;

                // Сигнал одному очікуючому потоку
                Monitor.Pulse(lockerForCount);
            }
        }

        //знаходить мінімальне значення і його індекс у заданому діапазоні./
        private (int min, int index) PartMin(int startIndex, int finishIndex)
        {
            int min = arr[startIndex];
            int index = startIndex;

            for (int i = startIndex; i < finishIndex; i++)
            {
                if (arr[i] < min)
                {
                    min = arr[i];
                    index = i;
                }
            }

            return (min, index);
        }
    }
}