package com.nicholasnassar.musicbot;

public class Request {
    private final String nameOrURL;

    private String title;

    private String length;

    public Request(String nameOrURL, String title, String length) {
        this.nameOrURL = nameOrURL;

        this.title = title;

        this.length = length;
    }

    public String getNameOrURL() {
        return nameOrURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }
}
