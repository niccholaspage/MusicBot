package com.nicholasnassar.musicbot.web;

import com.google.gson.JsonObject;
import com.nicholasnassar.musicbot.MusicBot;
import spark.ModelAndView;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import static spark.Spark.*;

public class WebPlayer {
    public WebPlayer(MusicBot bot, int port) throws Exception {
        port(port);
        staticFileLocation("web");

        get("/", (req, res) -> new ModelAndView(null, "index.html"), new FreeMarkerEngine());

        post("/play", (req, res) -> {
            String name = req.queryParams("name");

            if (name != null && !name.isEmpty()) {
                if (bot.musicExists(name)) {
                    bot.stop();

                    bot.getQueue().reset();

                    bot.playClip(name);
                } else {
                    bot.log(name + " doesn't exist!");
                }
            }

            return "";
        });

        post("/addtoqueue", (req, res) -> {
            String name = req.queryParams("name");

            if (name != null && !name.isEmpty()) {
                if (bot.musicExists(name)) {
                    bot.addToQueue(name);
                } else {
                    bot.log(name + " doesn't exist!");
                }
            }

            return "";
        });

        get("/pause", (req, res) -> {
            if (bot.isPlaying()) {
                bot.pause();
            } else {
                bot.play();
            }

            return "";
        });

        get("/stop", (req, res) -> {
            bot.stop();

            return "";
        });

        get("/play-status", (req, res) -> {
            res.type("text/event-stream;charset=UTF-8");
            res.header("Cache-Control", "no-cache");

            return "retry: 1000\ndata: {\"" + "title" + "\": \"" + bot.getTitle() + "\", \"time\": \"" + bot.getTime() + "\"}\n\n";
        });

        get("/queue", (req, res) -> {
            res.type("text/event-stream;charset=UTF-8");
            res.header("Cache-Control", "no-cache");

            String queue = "retry: 1000\n";

            return queue;
        });
    }

    public void stop() {
        Spark.stop();
    }
}
