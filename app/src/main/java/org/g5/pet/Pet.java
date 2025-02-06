package org.g5.pet;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.app.NotificationCompat;

import org.g5.core.Data;
import org.g5.core.ScreenTimeTracker;
import org.g5.overseer.R;
import org.g5.ui.Home;
import org.g5.util.LineWriter;
import org.g5.util.NotificationBuilder;
import org.g5.util.Time;

import java.io.File;
import java.io.IOException;
import java.util.List;

@SuppressLint("NewApi")
public class Pet {

    private static float health = 100;
    private static int dyingTime = Time.hourToSecond(8);
    private long screenTime = 0;

    private static Home home;

    private String hulingTestamento = "You've been using your phone for more than 8 hours... I'm starting to lose health T_T";

    private static LineWriter petDataWriter;

    private static ImageView pet;

    public Pet(Home home) {
        this.home = home;
        petDataWriter = new LineWriter(new File(home.getFilesDir(), "petData.txt"));
        pet = home.findViewById(R.id.pet);
    }

    public String getName() {
        return petDataWriter.getLine(0);
    }

    // Checks every after app switch
    public void start() {
        List<ScreenTimeTracker.AppUsageEntry> dataFromFile;
        try {
            dataFromFile = Data.getDataFromFile(Data.createDailyFile(home));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        updateScreenTime(Data.computeAll(dataFromFile));

        if (getScreenTime() > dyingTime) {
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
                            .setContentTitle(getName())
                            .setContentText(hulingTestamento)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setSound(null)
            );
        }
    }

    public void init() throws IOException {
        updateScreenTime(Data.computeAll(Data.getDataFromFile(Data.createDailyFile(home))));
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
            startHealthDecay(getScreenTime());
            updatePetStatus(home.isLightMode());
        }
    }

    public synchronized void startHealthDecay(long screenTime) {
        try {
            if (screenTime < Integer.parseInt(petDataWriter.getLine(2)))
                return;
        } catch (NumberFormatException e) {
            if (screenTime < dyingTime)
                return;
        }

        Log.d("PET TEST", "DECAY | PAST HEALTH: " + health + " CURRENT HEALTH: " + calculateHealthDecay(screenTime));
        health = calculateHealthDecay(screenTime);
        petDataWriter.writeLine(health + "", 1);
        Log.d("PET TEST | Line writting", petDataWriter.getLine(1));

        String newHealth = "HEALTH: " + String.format("%.2f", health) + "/100";

        home.runOnUiThread(() -> {
            home.updateHealth(newHealth);
            updatePetStatus(home.isLightMode());
        });
    }

    public float calculateHealthDecay(long screenTimeSeconds) {
        double screenTimeHours = (double) screenTimeSeconds / 3600.0f;

        double threshold = 8.0;
        double maxScreenTime = 10.0;

        if (screenTimeHours <= threshold) {
            return 100;
        } else if (screenTimeHours >= maxScreenTime) {
            return 0;
        } else {
            double decayFactor = (screenTimeHours - threshold) / (maxScreenTime - threshold);
            return (float) (100 - (decayFactor * 100));
        }
    }

    public static synchronized void startHealthRegen(long breakTime) {
        Log.d("HEALTH-REGEN", "HEAL AMOUNT: " + Math.min(100, health + ((float) breakTime / Time.hourToSecond(5)) * (100 - health)));
        health = Math.min(100, health + ((float) breakTime / Time.hourToSecond(5)) * (100 - health));
        breakTime += Time.minToSecond(60);

        if (breakTime > Time.minToSecond(30)) {
            float breakTimeInMinutes = Time.secondToMin(breakTime); // Convert to minutes

            float hours = (breakTimeInMinutes / 30); // Every 30 minutes contributes to 1 hour
            float remainingMinutes = breakTimeInMinutes % 30; // Remaining minutes

            Log.d("HEALTH-REGEN", "old decay time: " + dyingTime);
            dyingTime += Time.hourToSecond((hours + (remainingMinutes > 0 ? 1 : 0)));
            Log.d("HEALTH-REGEN", "new decay time: " + dyingTime);
        }

        petDataWriter.writeLine(health + "", 1);
        petDataWriter.writeLine(dyingTime + "", 2);

        String newHealth = "HEALTH: " + String.format("%.2f", health) + "/100";

        home.runOnUiThread(() -> {
            home.updateHealth(newHealth);
            updatePetStatus(home.isLightMode());
        });
    }

    public void updateScreenTime(long newScreenTime) {
        synchronized (Pet.class) {
            screenTime = newScreenTime;
        }
    }

    public long getScreenTime() {
        synchronized (Pet.class) {
            Log.d("Screentime-amount", "-----------------------------------------------------------------------------------------");
            Log.d("Screentime-amount", "(long) = " + screenTime);
            Log.d("Screentime-amount", "(int[]) = " + Time.formatTime(Time.convertSecondsToArray(screenTime)));
            return screenTime;
        }
    }

    public static void updatePetStatus(boolean lightMode) {
        if (lightMode) {
            if (health <= 1) {
                pet.setImageResource(R.drawable.dead_pet_light);
            } else if (health <= 60) {
                pet.setImageResource(R.drawable.tired_pet_light);
            } else if (health <= 80) {
                pet.setImageResource(R.drawable.normal_pet_light);
            } else {
                pet.setImageResource(R.drawable.happy_pet_light);
            }
        } else {
            if (health <= 1) {
                pet.setImageResource(R.drawable.dead_pet_dark);
            } else if (health <= 60) {
                pet.setImageResource(R.drawable.tired_pet_dark);
            } else if (health <= 80) {
                pet.setImageResource(R.drawable.normal_pet_dark);
            } else {
                pet.setImageResource(R.drawable.happy_pet_dark);
            }
        }
    }
}