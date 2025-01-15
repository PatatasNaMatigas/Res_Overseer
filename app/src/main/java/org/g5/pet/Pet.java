package org.g5.pet;

import android.annotation.SuppressLint;

import org.g5.core.AppUsage;
import org.g5.core.Data;
import org.g5.ui.Home;
import org.g5.util.LineWriter;
import org.g5.util.Time;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("NewApi")
public class Pet {

    private static int health = 100;
    private static int maxHealth = 100;
    private static int decayRatePerMin = 5;
    private static int timeTillDeath = Time.hourToSecond(2);
    private static int decayRate = timeTillDeath / decayRatePerMin;
    private static int healthDecayRate = maxHealth / decayRate;
    private static int lastDecayTime = Time.hourToSecond(8);
    private static int regenRatePerMin = 5;
    private static float regenRate = (float) maxHealth / Time.hourToMin(6);
    private static boolean dead = false;

    private static String lastApp = "";
    private int accumulatedTime = 0;

    private Home home;
    private static FloatingWindow floatingWindow;

    private static Timer decayTimer;

    // Dying message
    private static String name;
    private static String hulingTestamento = "You've been using your phone for a total of at least 8 hours... I'm starting to lose health T_T";

    public Pet(Home home) {
        this.home = home;
        name = new LineWriter(new File(home.getFilesDir(), "petData.txt")).getLine(0);
        floatingWindow = new FloatingWindow();
    }

    // Checks every after app switch
    public void start(String appName, int appTime) {
        int screenTime = Time.convertToSeconds(Data.getScreenTime(AppUsage.files[0]));
        int minimumTime = 10;

        if (Pet.lastApp.equals(appName)) {
            accumulatedTime += appTime;
        } else {
            accumulatedTime = appTime;
        }

        String nahilo = "I'm feeling dizzy 😵‍💫. You've spent " + Time.formatTime(Time.convertSecondsToArray(accumulatedTime)) + " on " + AppUsage.getAppName(home, appName) + ". Maybe take a break??";
        if (accumulatedTime >= minimumTime) {
            floatingWindow
                    .name(name)
                    .message(nahilo)
                    .react(FloatingWindow.DIZZY)
                    .start(home);
            Pet.lastApp = appName;
        }
//
//        if (screenTime > lastDecayTime) {
//            floatingWindow
//                    .name(name)
//                    .message(hulingTestamento)
//                    .react(FloatingWindow.DYING)
//                    .start(menu);
//            startHealthDecay(screenTime);
//        }
    }

    public static void updateHealth(int screenTime) {
        lastDecayTime += decayRatePerMin;
        health -= healthDecayRate;

        if (health <= 0) {
            dead = true;
        }
    }
    
    public static void startHealthDecay(int screenTime) {
        decayTimer = new Timer();
        decayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateHealth(screenTime);
            }
        }, 0, decayRatePerMin);
    }
}