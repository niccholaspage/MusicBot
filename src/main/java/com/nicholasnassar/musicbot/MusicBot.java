package com.nicholasnassar.musicbot;

import com.nicholasnassar.musicbot.web.WebPlayer;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.LoggerProvider;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;

import java.io.*;
import java.net.URL;
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

    private boolean stopped;

    private Browser browser;

    private boolean playingBrowser;

    private String title;

    public MusicBot() {
        musicFolder = new File("music");

        queue = new Queue();

        player = null;

        paused = false;

        stopped = true;

        playingBrowser = false;

        title = "Nothing";
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

        browser = new Browser();

        // Test browser view for debugging
        /*BrowserView view = new BrowserView(browser);

        JFrame frame = new JFrame();

        frame.add(view);

        frame.setSize(640, 480);
        frame.setVisible(true);*/

        browser.addTitleListener(titleEvent -> {
            title = browser.getTitle();

            title = title.substring(0, title.lastIndexOf("- "));
        });

        WebPlayer webPlayer;

        try {
            webPlayer = new WebPlayer(this, 8080);
        } catch (Exception e) {
            e.printStackTrace();

            return;
        }

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (playingBrowser) {
                    double currentTime = getCurrentTimeYT();
                    if (currentTime != -1 && currentTime == getDurationTimeYT()) {
                        update();
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
                        addToQueue(name);
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

    private double getCurrentTimeYT() {
        return getTime("getCurrentTime");
    }

    private double getDurationTimeYT() {
        return getTime("getDuration");
    }

    private double getTime(String function) {
        JSValue value = browser.executeJavaScriptAndReturnValue("document.querySelector('.html5-video-player')." + function + "();");

        if (value.isNull()) {
            return -1;
        }

        return value.getNumberValue();
    }

    private String formatTime(double time) {
        if (time == -1) {
            return "-";
        }

        int seconds = (int) time;

        return seconds / 60 + ":" + (seconds < 10 ? "0" : "") + seconds % 60;
    }

    public void log(String message) {
        System.out.println(message);
    }

    private void playYoutubeLink(String url) {
        String videoId = url.substring(url.lastIndexOf("=") + 1);

        browser.loadURL("https://www.youtube.com/embed/" + videoId + "?autoplay=1");

        browser.getDocument().createElement("<style>.ytp-time-display {display: inline}</style>}");

        playingBrowser = true;
    }

    public void playClip(String name) {
        if (isPlayableLink(name)) {
            playYoutubeLink(name);
        } else {
            playingBrowser = false;

            playClip(new File(musicFolder.getPath() + File.separator + name));

            title = name;
        }

        paused = false;

        stopped = false;
    }

    private boolean isPlayableLink(String name) {
        return name.toLowerCase().contains("youtube.com");
    }

    private void playClip(File file) {
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

    public void addToQueue(String name) {
        queue.addRequest(new Request(name, name, "Fetching..."));

        new Thread() {
            public void run() {

            }
        }.start();
    }

    public void update() {
        if (queue.getCurrentRequest() != null) {
            playClip(queue.getCurrentRequest().getNameOrURL());

            queue.removeCurrentRequest();
        } else {
            stop();
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

        paused = false;

        title = "Nothing";

        stopped = true;
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

    public String getTitle() {
        return title;
    }

    public String getTime() {
        if (stopped) {
            return "- / -";
        } else if (playingBrowser) {
            return formatTime(getCurrentTimeYT()) + " / " + formatTime(getDurationTimeYT());
        } else {
            return "Not Implemented Yet (Normal MP3 files)";
        }
    }

    public String getStringFromURL(String url) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));

            String line = "";

            String inputLine;

            while ((inputLine = in.readLine()) != null)
                line += inputLine;
            in.close();

            return line;
        } catch (Exception e) {
            return null;
        }
    }
}
