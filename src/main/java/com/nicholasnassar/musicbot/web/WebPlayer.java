package com.nicholasnassar.musicbot.web;

import com.nicholasnassar.musicbot.MusicBot;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class WebPlayer {
    private final Map<String, String> indexAttributes;

    public WebPlayer(MusicBot bot, int port) throws Exception {
        port(port);
        staticFileLocation("web");

        indexAttributes = new HashMap<>();

        indexAttributes.put("DURATION", "<> / <>");

        get("/", (req, res) -> new ModelAndView(indexAttributes, "index.html"), new FreeMarkerEngine());
        post("/", (request, response) -> {
            if (request.queryParams("pause") != null) {
                if (bot.isPlaying()) {
                    bot.pause();
                } else {
                    bot.play();
                }

                return new ModelAndView(indexAttributes, "index.html");
            }

            if (request.queryParams("stop") != null && bot.isPlaying()) {
                bot.stop();

                return new ModelAndView(indexAttributes, "index.html");
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

            return new ModelAndView(indexAttributes, "index.html");
        }, new FreeMarkerEngine());
    }

    public void update(MusicBot bot) {
        indexAttributes.remove("NOW_PLAYING");
        indexAttributes.put("NOW_PLAYING", "Now Playing: " + bot.getTitle());
    }

    public void stop() throws Exception {
        stop();
    }
}
