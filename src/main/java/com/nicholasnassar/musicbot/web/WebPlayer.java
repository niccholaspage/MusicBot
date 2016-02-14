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
        post("/", (request, response) -> {
            if (request.queryParams("pause") != null) {
                if (bot.isPlaying()) {
                    bot.pause();
                } else {
                    bot.play();
                }

                return new ModelAndView(null, "index.html");
            }

            if (request.queryParams("stop") != null && bot.isPlaying()) {
                bot.stop();

                return new ModelAndView(null, "index.html");
            }

            String name = request.queryParams("url");
            if (name != null && !name.isEmpty()) {
                if (request.queryParams("play") != null) {
                    if (bot.musicExists(name)) {
                        bot.stop();

                        bot.getQueue().reset();

                        bot.playClip(name);
                    } else {
                        bot.log(name + " doesn't exist!");
                    }
                } else {
                    if (bot.musicExists(name)) {
                        bot.getQueue().addRequest(name);
                    } else {
                        bot.log(name + " doesn't exist!");
                    }
                }
            }

            return new ModelAndView(null, "index.html");
        }, new FreeMarkerEngine());

        get("/play-status", (req, res) -> {
            res.type("text/event-stream;charset=UTF-8");
            res.header("Cache-Control", "no-cache");

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("title", bot.getTime());
            jsonObject.addProperty("time", bot.getTime());

            String output = "retry: 1000\n";

            output += "data: {\"" + "title" + "\": \"" + bot.getTitle() + "\", \"time\": \"" + bot.getTime() + "\"}\n\n";

            return output;
        });
    }

    public void stop() {
        Spark.stop();
    }
}
