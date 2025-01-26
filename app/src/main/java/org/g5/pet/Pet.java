package org.g5.pet;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.g5.core.AppUsage;
import org.g5.core.Data;
import org.g5.overseer.R;
import org.g5.ui.Home;
import org.g5.util.LineWriter;
import org.g5.util.NotificationBuilder;
import org.g5.util.Time;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressLint("NewApi")
public class Pet {

    private static float health = 100;
    private static final int dyingTime = Time.hourToSecond(8);
    private static final int minimumTime = Time.hourToSecond(2);
    private static boolean dead = false;
    private static boolean dying = false;
    private static int screenTime = 0;

    private static String lastApp = "";
    private int accumulatedTime = 0;

    private static Home home;

    private static String hulingTestamento = "You've been using your phone for more than 8 hours... I'm starting to lose health T_T";

    private static LineWriter petDataWriter;

    public Pet(Home home) {
        Pet.home = home;
        petDataWriter = new LineWriter(new File(home.getFilesDir(), "petData.txt"));
    }

    public static String getName() {
        return petDataWriter.getLine(0);
    }

    // Checks every after app switch
    public void start(String appName, int appTime) {
        Log.d("PET TEST | Last app", AppUsage.lastApp.getValue1());
        if (appName != null && !appName.isEmpty()) {
            updateScreenTime(Time.convertToSeconds(Data.getScreenTime(AppUsage.files[0])));

            Log.d("PET TEST", Time.formatTime(Time.convertSecondsToArray(accumulatedTime)) + " " + appTime);
            Log.d("PET TEST", Pet.lastApp + " " + appName);
            if (Pet.lastApp == null || Pet.lastApp.isEmpty())
                Pet.lastApp = appName;
            if (Pet.lastApp.contains(appName)) {
                accumulatedTime += appTime;
                if (accumulatedTime >= minimumTime) {
                    String nahilo = "I'm feeling dizzy. You've spent " + Time.formatTime(Time.convertSecondsToArray(accumulatedTime), true) + " on " + AppUsage.getAppName(home, Pet.lastApp) + ". Maybe take a break??";

                    accumulatedTime = 0;

                    Log.d("PET TEST", nahilo);
                    health -= 10 + (((float) (accumulatedTime - 7200) / 3600) * 10);

                    NotificationBuilder notificationBuilder = new NotificationBuilder();
                    notificationBuilder.createNotificationChannel(home);
                    notificationBuilder.getID();

                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            home,          // The context where the PendingIntent should be sent
                            0,                // Request code, can be any integer to identify the PendingIntent
                            new Intent(home, Home.class),           // The Intent to be fired when the PendingIntent is triggered
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE  // Flags
                    );

                    notificationBuilder.showNotification(home,
                            new NotificationCompat.Builder(
                                    home,
                                    notificationBuilder.getID())
                                    .setSmallIcon(R.drawable.dizzy_icon)
                                    .setContentTitle(Pet.getName())
                                    .setContentText(nahilo)
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)
                                    .setSound(null)
                    );
                }
            } else {
                if (accumulatedTime >= minimumTime) {
                    String nahilo = "I'm feeling dizzy. You've spent " + Time.formatTime(Time.convertSecondsToArray(accumulatedTime), true) + " on " + AppUsage.getAppName(home, Pet.lastApp) + ". Maybe take a break??";

                    Log.d("PET TEST", nahilo);
                    health -= 10 + (((float) (accumulatedTime - 7200) / 3600) * 10);

                    accumulatedTime = 0;

                    NotificationBuilder notificationBuilder = new NotificationBuilder();
                    notificationBuilder.createNotificationChannel(home);
                    notificationBuilder.getID();

                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            home,          // The context where the PendingIntent should be sent
                            0,                // Request code, can be any integer to identify the PendingIntent
                            new Intent(home, Home.class),           // The Intent to be fired when the PendingIntent is triggered
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE  // Flags
                    );

                    notificationBuilder.showNotification(home,
                            new NotificationCompat.Builder(
                                    home,
                                    notificationBuilder.getID())
                                    .setSmallIcon(R.drawable.dizzy_icon)
                                    .setContentTitle(Pet.getName())
                                    .setContentText(nahilo)
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)
                                    .setSound(null)
                    );
                }
                accumulatedTime = 0;
            }

            if (dying)
                startHealthDecay(getScreenTime());

            if (getScreenTime() > dyingTime) {
                if (!dying) {
                    startHealthDecay(getScreenTime());
                    NotificationBuilder notificationBuilder = new NotificationBuilder();
                    notificationBuilder.createNotificationChannel(home);
                    notificationBuilder.getID();

                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            home,
                            0,
                            new Intent(home, Home.class),
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );

                    notificationBuilder.showNotification(home,
                            new NotificationCompat.Builder(
                                    home,
                                    notificationBuilder.getID())
                                    .setSmallIcon(R.drawable.dead_icon)
                                    .setContentTitle(Pet.getName())
                                    .setContentText(hulingTestamento)
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)
                                    .setSound(null)
                    );
                    dying = true;
                }
            }
        }
    }

    public void init() throws IOException {
        updateScreenTime(Time.convertToSeconds(Data.getScreenTime(Data.createDailyFile(home))));
        Pet.lastApp = AppUsage.lastApp.getValue1();
        Log.d("PET TEST | Last app", AppUsage.lastApp.getValue1() + "");
        try {
            health = Float.parseFloat(petDataWriter.getLine(1));
            home.runOnUiThread(() -> {
                String newHealth = "HEALTH: " + String.format("%.2f", health) + "/100";
                home.updateHealth(newHealth);
            });
            Log.d("PET TEST | Health init", health + "");
        } catch (NumberFormatException e) {
            Log.d("PET TEST | Health init", "HEALTH NOT FOUND");
            Log.d("PET TEST | Health init", petDataWriter.getLine(1));
            Log.e("PET TEST | Health init",  e.getMessage());
            health = 100;
            petDataWriter.writeLine("100", 1);
        }
        if (getScreenTime() > dyingTime) {
            if (!dying) {
                dying = true;
            }
        }
    }

    public static synchronized void startHealthDecay(int screenTime) {
        Log.d("PET TEST", "DECAY | PAST HEALTH: " + health + " CURRENT HEALTH: " + calculateHealthDecay(screenTime));
        health = calculateHealthDecay(screenTime);
        petDataWriter.writeLine(health + "", 1);
        Log.d("PET TEST | Line writting", petDataWriter.getLine(1));

        String newHealth = "HEALTH: " + String.format("%.2f", health) + "/100";

        if (health <= 0) {
            health = 0;
            dead = true;
        }

        if (AppUsage.lastApp.getValue1().equals(home.getPackageName())) {
            home.runOnUiThread(() -> {
                home.updateHealth(newHealth);
            });
        }
    }

    public static float calculateHealthDecay(long screenTimeSeconds) {
        double screenTimeHours = (double) screenTimeSeconds / 3600.0f;

        double threshold = 8.0;
        double maxScreenTime = 10.0;

        if (screenTimeHours <= threshold) {
            return 100;
        } else if (screenTimeHours >= maxScreenTime) {
            return 0;
        } else {
            double decayFactor = (screenTimeHours - threshold) / (maxScreenTime - threshold);
            return (int) (100 - (decayFactor * 100));
        }
    }

    public static synchronized void startHealthRegen(int breakTime) {
        Log.d("PET TEST", "HEAL | PAST HEALTH: " + health + " CURRENT HEALTH: " + Math.min(100, health + (breakTime / 7200.0f) * (100 - health)));
        health = Math.min(100, health + (breakTime / 7200.0f) * (100 - health));

        petDataWriter.writeLine(health + "", 1);

        String newHealth = "HEALTH: " + String.format("%.2f", health) + "/100";

        if (health >= 100) {
            health = 100;
            dead = false;
            dying = false;
        }

        if (AppUsage.lastApp.getValue1().equals(home.getPackageName())) {
            home.runOnUiThread(() -> {
                home.updateHealth(newHealth);
            });
        }
    }

    public static void updateScreenTime(int newScreenTime) {
        synchronized (Pet.class) {
            screenTime = newScreenTime;
        }
    }

    public static int getScreenTime() {
        synchronized (Pet.class) {
            return screenTime;
        }
    }
}