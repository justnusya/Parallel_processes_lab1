using System;
using System.Threading;

namespace ThreadDemo
{
    class Program
    {
        private int threadsCount = 6;
        private Thread[] workers;
        private volatile bool[] stopFlags;
        private int[] startTimes;
        private int[] runDurations;
        private static Random rnd = new Random();

        private volatile bool controllerDone = false;

        static void Main(string[] args)
        {
            new Program().Start();
        }

        void Start()
        {
            workers      = new Thread[threadsCount];
            stopFlags    = new bool[threadsCount];
            startTimes   = new int[threadsCount];
            runDurations = new int[threadsCount];

            for (int i = 0; i < threadsCount; i++)
            {
                runDurations[i] = rnd.Next(2000, 10000);
            }

            for (int i = 0; i < threadsCount; i++)
            {
                int id    = i + 1;
                int index = i;
                startTimes[i] = Environment.TickCount;
                workers[i] = new Thread(() => Counter(id, index));
                workers[i].Start();
            }

            new Thread(Controller).Start();

            while (!controllerDone)
            {
                Thread.Sleep(50);
            }

            Console.WriteLine("All threads have finished.");
        }

        void Counter(int threadId, int index)
        {
            long sum = 0, count = 0, step = 2;

            while (stopFlags[index] == false)
            {
                sum += step;
                count++;
                Thread.Sleep(10);
            }

            Console.WriteLine($"Thread #{threadId} --- Sum: {sum} --- Elements: {count}");
        }

        void Controller()
        {
            bool allDone = false;

            while (!allDone)
            {
                allDone = true;

                for (int i = 0; i < threadsCount; i++)
                {
                    if (stopFlags[i] == false)
                    {
                        allDone = false;
                        int elapsed = Environment.TickCount - startTimes[i];

                        if (elapsed >= runDurations[i])
                        {
                            stopFlags[i] = true;
                        }
                    }
                }
            }

            controllerDone = true;
        }
    }
}