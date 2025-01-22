package org.g5.pet;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.Toast;

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
    private static int screenTime = 0;

    private static String lastApp = "";
    private int accumulatedTime = 0;

    private Home home;
    private static FloatingWindow floatingWindow;

    private static Timer decayTimer;

    private static String name;
    private static String hulingTestamento = "You've been using your phone for a total of at least 8 hours... I'm starting to lose health T_T";

    public Pet(Home home) {
        this.home = home;
        name = new LineWriter(new File(home.getFilesDir(), "petData.txt")).getLine(0);
        floatingWindow = new FloatingWindow();
    }

    // Checks every after app switch
    public void start(String appName, int appTime) {
        int minimumTime = Time.hourToSecond(2);

        String nahilo = "I'm feeling dizzy 😵‍💫. You've spent " + Time.formatTime(Time.convertSecondsToArray(accumulatedTime)) + " on " + AppUsage.getAppName(home, appName) + ". Maybe take a break??";
        Log.d("Accumulated time", Time.formatTime(Time.convertSecondsToArray(accumulatedTime)) + " " + appTime);
        Log.d("Accumulated time", Pet.lastApp + " " + appName);
        if (Pet.lastApp.isEmpty())
            Pet.lastApp = appName;
        if (Pet.lastApp.equals(appName)) {
            accumulatedTime += appTime;
            screenTime += appTime;
        } else {
            accumulatedTime = 0;
            Pet.lastApp = appName;
        }
        if (accumulatedTime >= minimumTime) {
            floatingWindow
                    .name(name)
                    .message(nahilo)
                    .react(FloatingWindow.DIZZY)
                    .start(home);
            accumulatedTime = 0;
        }

        if (screenTime > lastDecayTime) {
            floatingWindow
                    .name(name)
                    .message(hulingTestamento)
                    .react(FloatingWindow.DYING)
                    .start(home);
            startHealthDecay(screenTime);
        }
    }

    public static void updateHealth() {
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
                updateHealth();
            }
        }, 0, decayRatePerMin);
    }
}