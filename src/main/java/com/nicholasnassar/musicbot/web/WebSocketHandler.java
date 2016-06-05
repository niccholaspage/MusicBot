package com.nicholasnassar.musicbot.web;

import com.nicholasnassar.musicbot.MusicBot;
import com.nicholasnassar.musicbot.Request;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebSocket
public class WebSocketHandler {
    private static final Set<Session> sessions = new HashSet<>();

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        sessions.add(user);

        sendQueue(user);
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

    public static void sendPlayingUpdates() {
        MusicBot bot = MusicBot.bot;

        sessions.stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString("{\"title\": \"" + bot.getTitle() + "\", \"time\": \"" + bot.getTime() + "\"}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void sendQueueUpdates() {
        sessions.stream().filter(Session::isOpen).forEach(WebSocketHandler::sendQueue);
    }

    public static void sendQueue(Session session) {
        MusicBot bot = MusicBot.bot;

        String queue = "{\"queue\": \"";

        if (bot.getQueue().getRequests().isEmpty()) {
            queue += "Empty";
        } else {
            for (Request request : bot.getQueue().getRequests()) {
                queue += "<a href=\\\"" + request.getNameOrURL().replace("\"", "\\") + "\\\">" + request.getTitle() + "</a><br>";
            }
        }

        try {
            session.getRemote().sendString(queue + "\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
