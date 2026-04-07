package com.company;

public class ArrClass {
    private final int[] arr;
    private final int dim;
    
    private int globalMin = Integer.MAX_VALUE;
    private int globalIndex = -1;
    private int threadCount = 0;

    public ArrClass(int dim) {
        this.dim = dim;
        this.arr = new int[dim];
        initArr();
    }

    private void initArr() {
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < dim; i++) {
            arr[i] = rnd.nextInt(1000000) + 1;
        }
        arr[5000000] = -100;
    }

    public void reset() {
        globalMin = Integer.MAX_VALUE;
        globalIndex = -1;
        threadCount = 0;
    }

    public int[] partMin(int startIndex, int finishIndex) {
        int min = arr[startIndex];
        int index = startIndex;
        for (int i = startIndex; i < finishIndex; i++) {
            if (arr[i] < min) {
                min = arr[i];
                index = i;
            }
        }
        return new int[]{min, index}; 
    }

    synchronized public void collectMin(int min, int index) {
        if (min < globalMin) {
            globalMin = min;
            globalIndex = index;
        }
    }

    synchronized public void incThreadCount() {
        threadCount++;
        notifyAll();
    }

    synchronized public int[] getResult(int threadNum) {
        while (threadCount < threadNum) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new int[]{globalMin, globalIndex};
    }

    public int getDim() { return dim; }
}