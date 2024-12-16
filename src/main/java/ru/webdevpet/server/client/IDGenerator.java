package ru.webdevpet.server.client;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class IDGenerator {

    private static final AtomicInteger counter = new AtomicInteger(1);


    private static final Queue<Integer> freedIds = new LinkedList<>();


    public static int getNextId() {

        Integer freedId = freedIds.poll();
        if (freedId != null) {
            return freedId;
        } else {

            return counter.getAndIncrement();
        }
    }


    public static void releaseId(int id) {

        freedIds.offer(id);
    }
}