package com.nicholasnassar.musicbot.web;

import com.nicholasnassar.musicbot.MusicBot;
import spark.ModelAndView;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import java.util.HashMap;
import java.util.Map;

public class WebPlayer {
    public WebPlayer(MusicBot bot, int port) throws Exception {
        Spark.port(port);
        Spark.staticFileLocation("web");
        Map<String, String> map = new HashMap<>();
        Spark.get("/", (req, res) -> new ModelAndView(map, "index.html"), new FreeMarkerEngine());
        Spark.post("/", (request, response) -> {
            if (request.queryParams("pause") != null) {
                if (bot.isPlaying()) {
                    bot.pause();
                } else {
                    bot.play();
                }

                return new ModelAndView(map, "index.html");
            }

            if (request.queryParams("stop") != null && bot.isPlaying()) {
                bot.stop();

                return new ModelAndView(map, "index.html");
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

            return new ModelAndView(map, "index.html");
        }, new FreeMarkerEngine());

    }

    public void stop() throws Exception {
        Spark.stop();
    }
}
