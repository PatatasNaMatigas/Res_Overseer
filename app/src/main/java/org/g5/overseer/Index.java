package org.g5.overseer;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.g5.core.AppUsage;
import org.g5.ui.scene.Login;
import org.g5.ui.scene.Menu;
import org.g5.util.AccessibilityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Index extends AppCompatActivity {

    private static File fileDirectory;

    private ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fileDirectory = new File(getFilesDir().toString());

        if (!AccessibilityUtils.isAccessibilityServiceEnabled(this, AppUsage.class)) {
            Toast.makeText(this, "Please enable accessibility in this app", Toast.LENGTH_LONG).show();
            Intent settingsIntent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            settingsLauncher.launch(settingsIntent);
        } else {
            resume();
        }
    }

    private void resume() {
        new File(getFilesDir(), "accounts.txt").delete();

        try (BufferedReader reader = new BufferedReader(new FileReader(new File(getFilesDir(), "accounts.txt")))) {
            String username = reader.readLine();
            String password = reader.readLine();

            if (username != null && password != null && username.contains("[un]:") && password.contains("[pw]:")) {
                startActivity(new Intent(Index.this, Menu.class));
            } else {
                startActivity(new Intent(Index.this, Login.class));
            }
        } catch (IOException e) {
            startActivity(new Intent(Index.this, Login.class));
        }
    }

    private void launchSettings(Intent intent) {
        settingsLauncher.launch(intent);
    }

    public void startIntent(Class<?> cls) {
        Intent intent = new Intent(Index.this, cls);
        startActivity(intent);
    }

    public static File getFilesDirectory() {
        return fileDirectory;
    }
}
