package org.g5.overseer;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.g5.core.AppUsage;
import org.g5.pet.FloatingWindow;
import org.g5.ui.scene.Login;
import org.g5.ui.scene.Menu;
import org.g5.util.AccessibilityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Index extends AppCompatActivity {

    private static File fileDirectory;

    private ActivityResultLauncher<Intent> accessibilityPermissionLauncher = registerForActivityResult
            (new ActivityResultContracts.StartActivityForResult(), result -> {
                if (AccessibilityUtils.isAccessibilityServiceEnabled(Index.this, AppUsage.class)) {
                    resume();
                } else {
                    Toast.makeText(this, "Accessibility service still not enabled", Toast.LENGTH_LONG).show();
                    Intent settingsIntent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    launchSettings(settingsIntent);
                }
            }
    );

    private ActivityResultLauncher<Intent> overlayPermissionLauncher = registerForActivityResult
            (new ActivityResultContracts.StartActivityForResult(), result -> {
                if (AccessibilityUtils.isOverlayPermissionEnabled(this)) {
                    resume();
                } else {
                    Toast.makeText(this, "Overlay service still not enabled", Toast.LENGTH_LONG).show();
                    Intent settingsIntent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    launchSettings(settingsIntent);
                }
            }
    );

    // TODO
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileDirectory = getFilesDirectory();
        try {
            if (!AccessibilityUtils.isAccessibilityServiceEnabled(this, AppUsage.class)) {
                Toast.makeText(this, "Please enable accessibility in this app", Toast.LENGTH_LONG).show();
                Intent settingsIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                accessibilityPermissionLauncher.launch(settingsIntent);
                Log.d("Accessibility service is not enabled", "not ok 1");
            } else {
                Log.d("Accessibility service is enabled", "ok 1");
                // Accessibility service is enabled, now check for overlay permission
                if (!AccessibilityUtils.isOverlayPermissionEnabled(this)) {
                    Toast.makeText(this, "Please enable app overlay in this app", Toast.LENGTH_LONG).show();
                    Intent settingsIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    overlayPermissionLauncher.launch(settingsIntent);
                    Log.d("Overlay permission is not enabled", "not ok 2");
                } else {
                    // Both permissions are granted, proceed to resume
                    Log.d("Overlay permission is enabled", "ok 2");
                    resume();
                }
            }
        } catch (Exception e) {
            Log.d("IROR", e.getLocalizedMessage());
        }
    }

    private void resume() {

        try (BufferedReader reader = new BufferedReader(new FileReader(new File(getFilesDir(), "accounts.txt")))) {
            String username = reader.readLine();
            String password = reader.readLine();

            if (username != null && password != null && username.contains("[un]:") && password.contains("[pw]:")) {
                Login.setAccount(username, password);
                startActivity(new Intent(Index.this, Menu.class));
            } else {
                startActivity(new Intent(Index.this, Login.class));
            }
        } catch (IOException e) {
            startActivity(new Intent(Index.this, Login.class));
        }
    }

    private void launchSettings(Intent intent) {
        accessibilityPermissionLauncher.launch(intent);
    }

    public static File getFilesDirectory() {
        return fileDirectory;
    }
}
