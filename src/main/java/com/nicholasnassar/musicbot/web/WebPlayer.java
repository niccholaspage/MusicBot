package com.nicholasnassar.musicbot.web;

import com.nicholasnassar.musicbot.MusicBot;
import com.nicholasnassar.musicbot.Request;
import spark.ModelAndView;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class WebPlayer {
    public WebPlayer(MusicBot bot, int port) throws Exception {
        port(port);

        staticFileLocation("web");

        webSocket("/play-status", WebSocketHandler.class);

        JadeTemplateEngine jade = new JadeTemplateEngine();

        Map<String, String> emptyMap = new HashMap<>();

        get("/", (req, res) -> new ModelAndView(emptyMap, "index"), jade);

        /*get("/play-status", (req, res) -> {
            res.type("text/event-stream;charset=UTF-8");
            res.header("Cache-Control", "no-cache");

            return "retry: 1000\ndata: {\"" + "title" + "\": \"" + bot.getTitle() + "\", \"time\": \"" + bot.getTime() + "\"}\n\n";
        });*/

        get("/queue", (req, res) -> {
            res.type("text/event-stream;charset=UTF-8");
            res.header("Cache-Control", "no-cache");

            String queue = "retry: 1000\ndata:";

            if (bot.getQueue().getRequests().isEmpty()) {
                queue += "Empty";
            } else {
                for (Request request : bot.getQueue().getRequests()) {
                    queue += "<a href=\"" + request.getNameOrURL() + "\">" + request.getTitle() + "</a><br>";
                }
            }

            return queue + "\n\n";
        });
    }

    public void stop() {
        Spark.stop();
    }
}
