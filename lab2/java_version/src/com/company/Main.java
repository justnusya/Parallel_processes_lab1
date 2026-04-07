package com.company;

public class Main {
    public static void main(String[] args) {
        int dim = 1000000000;
        ArrClass arrClass = new ArrClass(dim);
        int[] threadTests = {1, 2, 4, 8};

        for (int threadNum : threadTests) {
            arrClass.reset();
            ThreadMin[] threads = new ThreadMin[threadNum];
            int part = dim / threadNum;

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < threadNum; i++) {
                int start = i * part;
                int end = (i == threadNum - 1) ? dim : start + part;
                threads[i] = new ThreadMin(start, end, arrClass);
                threads[i].start();
            }

            int[] result = arrClass.getResult(threadNum);
            long endTime = System.currentTimeMillis();

            System.out.println("Threads: " + threadNum);
            System.out.println("Min value: " + result[0]);
            System.out.println("Index: " + result[1]);
            System.out.println("Elapsed time: " + (endTime - startTime) + " ms");
        }
    }
}