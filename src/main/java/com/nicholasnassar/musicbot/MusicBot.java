package com.nicholasnassar.musicbot;

import com.nicholasnassar.musicbot.web.WebPlayer;
import com.teamdev.jxbrowser.chromium.Browser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class MusicBot {
    private final File musicFolder;

    private final File cacheFolder;

    private final Queue queue;

    private PausablePlayer player;

    private boolean paused;

    private Browser browser;

    public MusicBot() {
        musicFolder = new File("music");

        cacheFolder = new File("cache");

        queue = new Queue();

        player = null;
    }

    public void start() {
        if (!musicFolder.exists() && !musicFolder.mkdir()) {
            log("Couldn't make music folder.");

            return;
        }

        if (!cacheFolder.exists() && !cacheFolder.mkdir()) {
            log("Couldn't make cache folder.");

            return;
        } else {
            File youtubeFolder = new File(cacheFolder, "youtube");

            if (!youtubeFolder.exists() && !youtubeFolder.mkdir()) {
                log("Couldn't make youtube folder.");

                return;
            }
        }

        log("MusicBot started!");

        Scanner scanner = new Scanner(System.in);

        WebPlayer webPlayer;

        browser = new Browser();

        try {
            webPlayer = new WebPlayer(this, 8080);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                log("You need to enter something in!");
            }

            String[] split = line.split(" ");

            String command = split[0];

            if (command.equalsIgnoreCase("play")) {
                if (split.length > 1) {
                    String name = split[1];

                    if (musicExists(name)) {
                        stop();

                        queue.reset();

                        playClip(name);
                    } else {
                        log(name + " doesn't exist!");
                    }
                } else {
                    play();
                }
            } else if (command.equalsIgnoreCase("add")) {
                if (split.length > 1) {
                    String name = split[1];

                    if (musicExists(name)) {
                        queue.addRequest(name);
                    } else {
                        log(name + " doesn't exist!");
                    }
                }
            } else if (command.equalsIgnoreCase("pause")) {
                pause();
            } else if (command.equalsIgnoreCase("stop")) {
                stop();
            } else if (command.equalsIgnoreCase("quit")) {
                log("MusicBot closing...");

                System.exit(0);

                break;
            }
        }

        try {
            webPlayer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        scanner.close();
    }

    public void log(String message) {
        System.out.println(message);
    }

    private void playYoutubeLink(String url) {
        /*String videoName = url.substring(url.indexOf("v=") + 2);
        try {
            File file = new File(cacheFolder, "youtube" + File.separator + videoName + ".mp3");
            if (!file.exists()) {
                //URL website = new URL("http://youtubeinmp3.com/fetch/?video=" + url);
                //ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                //FileOutputStream fos = new FileOutputStream(file);
                //fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                VGet video = new VGet(new URL(url), file);
                video.download();
            }
            playClip(file);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        browser.loadURL(url);
    }

    public void playClip(String name) {
        if (name.toLowerCase().contains("youtube.com")) {
            playYoutubeLink(name);

            return;
        }

        playClip(new File(musicFolder.getPath() + File.separator + name));
    }

    public void playClip(File file) {
        paused = false;

        try {
            FileInputStream stream = new FileInputStream(file);
            if (player != null) {
                player.stop();

                player.close();
            }

            player = new PausablePlayer(stream);
            player.setStopListener(this::update);

            new Thread(() -> {
                try {
                    player.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            log("Playing " + file.getName());

            return;
        } catch (FileNotFoundException e) {
            log(file.getName() + " not found!");

            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        log("Couldn't play " + file.getName());
    }

    public void update() {
        if (queue.getCurrentRequest() != null) {
            playClip(queue.getCurrentRequest());

            queue.removeCurrentRequest();
        }
    }

    public void play() {
        if (player != null) {
            paused = false;

            player.resume();

            log("Resumed!");
        }
    }

    public void pause() {
        if (player != null) {
            paused = true;

            player.pause();

            log("Paused!");
        }
    }

    public void stop() {
        if (player != null) {
            paused = false;

            queue.reset();

            player.stop();

            player.close();

            player = null;

            log("Stopped!");
        }
    }

    public boolean isPlaying() {
        return !paused;
    }

    public boolean musicExists(String name) {
        return name.toLowerCase().contains("youtube.com") || new File(musicFolder + File.separator + name).exists();
    }

    public Queue getQueue() {
        return queue;
    }
}
