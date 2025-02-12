package org.g5.overseer;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import org.g5.core.ScreenTimeTracker;
import org.g5.pet.Pet;
import org.g5.ui.Login;
import org.g5.ui.Home;
import org.g5.ui.Permission;
import org.g5.util.NotificationBuilder;
import org.g5.util.Time;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Index extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.checkup_history_page);

        if (!checkNotifications(this) || !ScreenTimeTracker.isUsageAccessGranted(this)) {
            startActivity(new Intent(this, Permission.class));
            NotificationBuilder notificationBuilder = new NotificationBuilder();
            notificationBuilder.createNotificationChannel(this);
            notificationBuilder.getID();

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    new Intent(this, Home.class),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            notificationBuilder.showNotification(this,
                    new NotificationCompat.Builder(
                            this,
                            notificationBuilder.getID())
                            .setSmallIcon(R.drawable.normal_icon)
                            .setContentTitle(Pet.getName())
                            .setContentText("Your total screen time is " + Time.formatTime(Time.convertSecondsToArray(Pet.getScreenTime())))
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setSound(null)
            );
            finish();
        } else {
            resume();
            finish();
        }
    }

    private boolean checkNotifications(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Check if we should show rationale
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.POST_NOTIFICATIONS)) {
                    // Show a rationale to the user (optional Toast here)
                    Toast.makeText(activity, "Notification permission is required for alerts!", Toast.LENGTH_SHORT).show();
                }

                // Request permission
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1);

                return false;  // Permission not granted yet, request made
            }
        }
        return true;  // Always return true for Android 12 and below
    }

    private void resume() {
        startActivity(new Intent(Index.this, Login.class));
    }
}
