package org.g5.overseer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.g5.core.ScreenTimeTracker;
import org.g5.ui.Login;
import org.g5.ui.Home;
import org.g5.ui.Permission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Index extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkNotifications(this) || !ScreenTimeTracker.isUsageAccessGranted(this)) {
            startActivity(new Intent(this, Permission.class));
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
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(getFilesDir(), "accounts.txt")))) {
            String username = reader.readLine();
            String password = reader.readLine();

            if (username != null && password != null && username.contains("[un]:") && password.contains("[pw]:")) {
                Login.setAccount(username, password);
                startActivity(new Intent(Index.this, Home.class));
                finish();
            } else {
                startActivity(new Intent(Index.this, Login.class));
                finish();
            }
        } catch (IOException e) {
            startActivity(new Intent(Index.this, Login.class));
            finish();
        }
    }
}
