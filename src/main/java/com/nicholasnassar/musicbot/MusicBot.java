package com.nicholasnassar.musicbot;

import com.nicholasnassar.musicbot.web.WebPlayer;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.LoggerProvider;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;


public class MusicBot {
    private final File musicFolder;

    private final Queue queue;

    private PausablePlayer player;

    private boolean paused;

    private Browser browser;

    private boolean playingBrowser;

    public MusicBot() {
        musicFolder = new File("music");

        queue = new Queue();

        player = null;

        playingBrowser = false;
    }

    public void start() {
        LoggerProvider.getBrowserLogger().setLevel(Level.SEVERE);
        LoggerProvider.getIPCLogger().setLevel(Level.SEVERE);
        LoggerProvider.getChromiumProcessLogger().setLevel(Level.SEVERE);

        if (!musicFolder.exists() && !musicFolder.mkdir()) {
            log("Couldn't make music folder.");

            return;
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

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            private boolean updateNextTime;

            @Override
            public void run() {
                if (updateNextTime) {
                    update();

                    updateNextTime = false;
                } else if (playingBrowser) {
                    DOMDocument document = browser.getDocument();
                    List<DOMElement> currentTime = document.findElements(By.className("ytp-time-current"));
                    List<DOMElement> duration = document.findElements(By.className("ytp-time-duration"));

                    if (!currentTime.isEmpty() && !duration.isEmpty())
                        if (currentTime.get(0).getTextContent().equals(duration.get(0).getTextContent())) {
                            updateNextTime = true;
                        }
                }
            }
        }, 0, 1000);

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

        timer.cancel();

        timer.purge();

        scanner.close();
    }

    public void log(String message) {
        System.out.println(message);
    }

    private void playYoutubeLink(String url) {
        browser.loadURL(url);

        playingBrowser = true;
    }

    public void playClip(String name) {
        if (name.toLowerCase().contains("youtube.com")) {
            playYoutubeLink(name);

            return;
        }

        playingBrowser = false;

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
        if (!playingBrowser) {
            if (player != null) {
                paused = false;

                player.resume();

                log("Resumed!");
            }
        } else {
            togglePlayButton();
        }
    }

    public void pause() {
        if (!playingBrowser) {
            if (player != null) {
                paused = true;

                player.pause();

                log("Paused!");
            }
        } else {
            togglePlayButton();
        }
    }

    private void togglePlayButton() {
        DOMDocument document = browser.getDocument();
        List<DOMElement> elements = document.findElements(By.className("ytp-play-button ytp-button"));
        elements.get(0).click();
        paused = !paused;
    }

    public void stop() {
        if (!playingBrowser) {
            if (player != null) {
                paused = false;

                queue.reset();

                player.stop();

                player.close();

                player = null;

                log("Stopped!");
            }
        } else {
            browser.loadHTML("<html><html>");

            playingBrowser = false;
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
