package com.company;

import java.util.Random;

public class Main {

    private final int threadsCount = 10;
    private static final Random random = new Random();

    private volatile boolean[] stopFlags;
    private long[] startTimes;
    private int[] runDurations;

    private volatile boolean controllerDone = false;

    public static void main(String[] args) {
        new Main().start();
    }

    void start() {
        stopFlags    = new boolean[threadsCount];
        startTimes   = new long[threadsCount];
        runDurations = new int[threadsCount];

        for (int i = 0; i < threadsCount; i++) {
            runDurations[i] = random.nextInt(10001) + 5000;
        }

        for (int i = 0; i < threadsCount; i++) {
            int id = i;
            startTimes[i] = System.currentTimeMillis();
            new Thread(() -> workerTask(id)).start();
        }

        new Thread(this::controller).start();

        while (!controllerDone) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void workerTask(int threadId) {
        long sum   = 0;
        long count = 0;
        final long step = 2;

        while (!stopFlags[threadId]) {
            sum += step;
            count++;
        }

        System.out.println("Thread #" + (threadId + 1) +
                " --- Sum: " + sum + " --- Elements: " + count);
    }

    private void controller() {
        boolean allFinished = false;

        while (!allFinished) {
            allFinished = true;

            for (int i = 0; i < threadsCount; i++) {
                if (!stopFlags[i]) {
                    allFinished = false;
                    long elapsed = System.currentTimeMillis() - startTimes[i];

                    if (elapsed >= runDurations[i]) {
                        stopFlags[i] = true;
                    }
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        controllerDone = true;
    }
}