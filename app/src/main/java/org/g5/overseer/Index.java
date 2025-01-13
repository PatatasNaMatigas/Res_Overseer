package org.g5.overseer;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.g5.core.AppUsage;
import org.g5.pet.FloatingWindow;
import org.g5.ui.Login;
import org.g5.ui.Menu;
import org.g5.ui.Permission;
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

                }
            }
    );

    // TODO
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileDirectory = getFilesDirectory();

        if (!AccessibilityUtils.isAccessibilityServiceEnabled(this, AppUsage.class) || !FloatingWindow.permissionGranted(this)) {
            startActivity(new Intent(this, Permission.class));
        } else {
            resume();
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



    public static File getFilesDirectory() {
        return fileDirectory;
    }
}
