package com.nicholasnassar.musicbot;

import com.nicholasnassar.musicbot.web.WebPlayer;
import com.nicholasnassar.musicbot.web.WebSocketHandler;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


public class MusicBot {
    private final File musicFolder;

    private final Queue queue;

    private PausablePlayer player;

    private boolean paused;

    private boolean stopped;

    private FirefoxDriver browser;

    private boolean playingBrowser;

    private String title;

    public static MusicBot bot;

    private boolean autoplayFlipped;

    private Timer timer;

    public MusicBot() {
        musicFolder = new File("music");

        queue = new Queue();

        player = null;

        paused = false;

        stopped = true;

        playingBrowser = false;

        title = "Nothing";

        autoplayFlipped = false;

        bot = this;
    }

    public void start() {
        if (!musicFolder.exists() && !musicFolder.mkdir()) {
            log("Couldn't make music folder.");

            return;
        }

        log("MusicBot started!");

        Scanner scanner = new Scanner(System.in);

        browser = new FirefoxDriver();

        File loginFile = new File("login.txt");

        if (loginFile.exists()) {
            try {
                String[] loginDetails = FileUtils.readFileToString(loginFile).split(System.lineSeparator());

                String username = loginDetails[0];

                String password = loginDetails[1];

                browser.get("https://accounts.google.com/ServiceLogin?service=youtube&uilel=3&continue=https%3A%2F%2Fwww.youtube.com%2Fsignin%3Fapp%3Ddesktop%26hl%3Den%26feature%3Dsign_in_button%26action_handle_signin%3Dtrue%26next%3D%252F&hl=en&passive=true#identifier");

                WebElement element = browser.findElement(By.name("Email"));

                if (element != null) {
                    element.sendKeys(username);

                    element = browser.findElement(By.name("signIn"));

                    if (element != null) {
                        element.click();

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        element = browser.findElement(By.name("Passwd"));

                        if (element != null) {
                            element.sendKeys(password);

                            element = browser.findElement(By.id("signIn"));

                            element.click();
                        }
                    }
                }
            } catch (IOException e) {

            }
        }

        // Test browser view for debugging
        /*BrowserView view = new BrowserView(browser);

        JFrame frame = new JFrame();

        frame.add(view);

        frame.setSize(640, 480);
        frame.setVisible(true);*/

        WebPlayer webPlayer;

        try {
            webPlayer = new WebPlayer(7999);
        } catch (Exception e) {
            e.printStackTrace();

            return;
        }

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (playingBrowser) {
                    double currentTime = getCurrentTimeYT();
                    if (currentTime != -1 && currentTime >= getDurationTimeYT()) {
                        update();
                    }

                    if (!stopped) {
                        title = fixTitle(browser.getTitle());
                    }

                    if (!autoplayFlipped) {
                        WebElement element = browser.findElement(By.id("autoplay-checkbox"));

                        if (element != null) {
                            element.click();

                            autoplayFlipped = true;
                        }
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

                break;
            }
        }

        try {
            webPlayer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        browser.close();

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
        try {
            return (double) browser.executeScript("return document.querySelector('.html5-video-player')." + function + "();");
        } catch (Exception e) {
            return -1;
        }
    }

    private String formatTime(double time) {
        if (time == -1) {
            return "-";
        }

        int seconds = (int) time;

        String output = seconds / 60 + ":";

        if (seconds % 60 < 10) {
            output += 0;
        }

        output += seconds % 60;

        return output;
    }

    public void log(String message) {
        System.out.println(message);
    }

    private void playYoutubeLink(String url) {
        browser.get(url);

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

    public void play(String name) {
        if (name != null && !name.isEmpty()) {
            if (musicExists(name)) {
                stop();

                getQueue().reset();

                playClip(name);
            } else {
                log(name + " doesn't exist!");
            }
        }
    }

    public void addToQueueWeb(String name) {
        if (name != null && !name.isEmpty()) {
            if (musicExists(name)) {
                addToQueue(name);
            } else {
                log(name + " doesn't exist!");
            }
        }
    }

    public void addToQueue(String name) {
        Request request = new Request(name, "Fetching Title", "Fetching Duration");

        queue.addRequest(request);

        WebSocketHandler.sendQueueUpdates();

        new Thread() {
            public void run() {
                try {
                    Document document = Jsoup.connect(request.getNameOrURL()).get();

                    request.setTitle(fixTitle(document.title()));

                    WebSocketHandler.sendQueueUpdates();
                } catch (IOException e) {
                }
            }
        }.start();
    }

    public void update() {
        if (queue.getCurrentRequest() != null) {
            playClip(queue.getCurrentRequest().getNameOrURL());

            queue.removeCurrentRequest();

            WebSocketHandler.sendQueueUpdates();
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
        WebElement element = browser.findElement(By.className("ytp-play-button"));

        if (element != null) {
            element.click();
            paused = !paused;
        }
    }

    public void stop() {
        if (!playingBrowser) {
            if (player != null) {
                player.stop();

                player.close();

                player = null;

                log("Stopped!");
            }
        } else {
            browser.get("about:blank");

            playingBrowser = false;
        }

        queue.reset();

        paused = false;

        title = "Nothing";

        stopped = true;

        WebSocketHandler.sendPlayingUpdates();

        WebSocketHandler.sendQueueUpdates();
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

    private String fixTitle(String title) {
        if (title.contains("- ")) {
            title = title.substring(0, title.lastIndexOf("- "));
        }

        return title.trim();
    }
}
