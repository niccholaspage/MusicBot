package com.nicholasnassar.musicbot;

import java.util.ArrayList;
import java.util.List;

public class Queue {
    private final List<Request> requests;

    public Queue() {
        requests = new ArrayList<>();
    }

    public synchronized void addRequest(Request request) {
        requests.add(request);
    }

    public synchronized Request getCurrentRequest() {
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
