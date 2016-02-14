package com.nicholasnassar.musicbot;

import java.util.ArrayList;
import java.util.List;

public class Queue {
    private final List<String> requests;

    public Queue() {
        requests = new ArrayList<>();
    }

    public synchronized void addRequest(String request) {
        requests.add(request);
    }

    public synchronized String getCurrentRequest() {
        if (requests.isEmpty()) {
            return null;
        }

        return requests.get(0);
    }

    public synchronized void reset() {
        requests.clear();
    }

    public synchronized void removeCurrentRequest() {
        requests.remove(0);
    }
}
