package com.nicholasnassar.musicbot.web;

import com.nicholasnassar.musicbot.MusicBot;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.util.HashSet;
import java.util.Set;

@WebSocket
public class WebSocketHandler {
    private static final Set<Session> sessions = new HashSet<>();

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        sessions.add(user);
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        sessions.remove(user);
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        if (message.startsWith("play,")) {
            String name = message.substring(message.indexOf(",") + 1);

            MusicBot.bot.play(name);
        } else if (message.startsWith("addtoqueue,")) {
            String name = message.substring(message.indexOf(",") + 1);

            MusicBot.bot.addToQueueWeb(name);
        } else if (message.equals("pause")) {
            if (MusicBot.bot.isPlaying()) {
                MusicBot.bot.pause();
            } else {
                MusicBot.bot.play();
            }
        } else if (message.equals("stop")) {
            MusicBot.bot.stop();
        }
    }
}
