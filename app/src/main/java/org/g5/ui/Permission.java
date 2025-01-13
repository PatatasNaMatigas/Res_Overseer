package org.g5.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.g5.core.AppUsage;
import org.g5.overseer.Index;
import org.g5.overseer.R;
import org.g5.pet.FloatingWindow;
import org.g5.util.AccessibilityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Permission extends AppCompatActivity {

    private boolean accessibilityPermission;
    private boolean displayOverOtherAppsPermission;
    private ActivityResultLauncher<Intent> accessibilityPermissionLauncher = registerForActivityResult
            (new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (AccessibilityUtils.isAccessibilityServiceEnabled(this, AppUsage.class)) {
                        accessibilityPermission = checkAccessibilityPermission();
                        if (accessibilityPermission && displayOverOtherAppsPermission) {
                            findViewById(R.id.proceed).setBackgroundResource(R.drawable.activated_button);
                        } else {
                            findViewById(R.id.proceed).setBackgroundResource(R.drawable.unactivated_button);
                        }
                    }
            }
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_request_page);

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            // Dark mode
            ((ImageButton) findViewById(R.id.permission_help)).setImageResource(R.drawable.help_dark);
        } else {
            // Light mode
            ((ImageButton) findViewById(R.id.display_over_other_apps_help)).setImageResource(R.drawable.help_light);
        }

        accessibilityPermission = checkAccessibilityPermission();
        displayOverOtherAppsPermission = checkDisplayOverOtherAppsPermission();

        findViewById(R.id.proceed).setOnClickListener(view -> {
            if (checkAccessibilityPermission() && checkDisplayOverOtherAppsPermission()) {
                resume();
            }
        });

        findViewById(R.id.grant_accessibility).setOnClickListener(view -> {
           if (!checkAccessibilityPermission()) {
               Toast.makeText(this, "Please enable accessibility permission in this app", Toast.LENGTH_LONG).show();
               Intent settings = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
               launchAccessibilitySettings(settings);
           }
        });

        findViewById(R.id.grant_display_over_other_apps).setOnClickListener(view -> {
           if (!checkDisplayOverOtherAppsPermission()) {
               Toast.makeText(this, "Please enable display over other apps permission for this app", Toast.LENGTH_LONG).show();
               Intent settings = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                       Uri.parse("package:" + getPackageName()));
               startActivity(settings);
           } else {
               displayOverOtherAppsPermission = checkDisplayOverOtherAppsPermission();
               if (accessibilityPermission && displayOverOtherAppsPermission) {
                   findViewById(R.id.proceed).setBackgroundResource(R.drawable.activated_button);
               } else {
                   findViewById(R.id.proceed).setBackgroundResource(R.drawable.unactivated_button);
               }
           }
        });


        if (accessibilityPermission && displayOverOtherAppsPermission) {
            findViewById(R.id.proceed).setBackgroundResource(R.drawable.activated_button);
        } else {
            findViewById(R.id.proceed).setBackgroundResource(R.drawable.unactivated_button);
        }
    }

    private void launchAccessibilitySettings(Intent intent) {
        accessibilityPermissionLauncher.launch(intent);
    }

    private boolean checkAccessibilityPermission() {
        if (!AccessibilityUtils.isAccessibilityServiceEnabled(this, AppUsage.class)) {
            findViewById(R.id.grant_accessibility).setBackgroundResource(R.drawable.unactivated_button);
            return false;
        } else {
            ((Button) findViewById(R.id.grant_accessibility)).setText("Granted");
            findViewById(R.id.grant_accessibility).setBackgroundResource(R.drawable.activated_button);
            return true;
        }
    }

    private boolean checkDisplayOverOtherAppsPermission() {
        if (!FloatingWindow.permissionGranted(this)) {
            findViewById(R.id.grant_display_over_other_apps).setBackgroundResource(R.drawable.unactivated_button);
            return false;
        } else {
            ((Button) findViewById(R.id.grant_display_over_other_apps)).setText("Granted");
            findViewById(R.id.grant_display_over_other_apps).setBackgroundResource(R.drawable.activated_button);
            return true;
        }
    }

    private void resume() {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(getFilesDir(), "accounts.txt")))) {
            String username = reader.readLine();
            String password = reader.readLine();

            if (username != null && password != null && username.contains("[un]:") && password.contains("[pw]:")) {
                Login.setAccount(username, password);
                startActivity(new Intent(Permission.this, Menu.class));
            } else {
                startActivity(new Intent(Permission.this, Login.class));
            }
        } catch (IOException e) {
            startActivity(new Intent(Permission.this, Login.class));
        }
    }
}