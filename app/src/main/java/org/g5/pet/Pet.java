package org.g5.pet;

import android.annotation.SuppressLint;

import org.g5.core.AppUsage;
import org.g5.core.Data;
import org.g5.ui.scene.Menu;
import org.g5.util.Time;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private Menu menu;
    private static FloatingWindow floatingWindow;

    private static Timer decayTimer;

    // Dying message
    private static String hulingTestamento = "You've been using your phone for at least 8 hours. I'm starting to lose health T_T";

    public Pet(Menu menu) {
        this.menu = menu;
        floatingWindow = new FloatingWindow();
    }

    public void start() throws IOException {
        File daily = Data.createDailyFile(menu);
        int screenTime = Time.convertToSeconds(Data.getScreenTime(daily));
        String highestAppName = Data.getHighestScreenTime(daily).getValue1();
        int[] highestScreenTime = Data.getHighestScreenTime(daily).getValue2();
        ArrayList<int[]> screenTimeRecord = Data.getFilteredScreenTime(daily);
        ArrayList<int[]> breakTime = AppUsage.getBreakTime();

        // pet is dying womp womp
        if (screenTime > lastDecayTime) {
            floatingWindow
                    .pop(hulingTestamento)
                    .react(Reaction.DYING)
                    .start();
            startHealthDecay(screenTime);
        }

        String nahilo = "I'm feeling dizzy 😵‍💫. You've spent " + screenTime + " on " + highestAppName + ". Maybe take a break??";
        if (Time.convertToSeconds(highestScreenTime) > Time.hourToSecond(1)) {
            floatingWindow
                    .pop(nahilo)
                    .react(Reaction.DIZZY)
                    .start();
        }
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