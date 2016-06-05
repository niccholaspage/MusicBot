package com.nicholasnassar.musicbot.web;

import spark.ModelAndView;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static spark.Spark.*;

public class WebPlayer {
    public WebPlayer(int port) throws Exception {
        port(port);

        staticFileLocation("/web");

        webSocket("/play-status", WebSocketHandler.class);

        JadeTemplateEngine jade = new JadeTemplateEngine();

        Map<String, String> emptyMap = new HashMap<>();

        get("/", (req, res) -> new ModelAndView(emptyMap, "index"), jade);

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                WebSocketHandler.sendPlayingUpdates();
            }
        }, 0, 1000);
    }

    public void stop() {
        Spark.stop();
    }
}
