package org.g5.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;

import org.g5.core.AppUsage;
import org.g5.overseer.R;
import org.g5.util.AccessibilityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Permission extends AppCompatActivity {

    private boolean accessibilityPermission;
    private boolean notificationPermission;
    private Button proceed;

    private ActivityResultLauncher<Intent> accessibilityPermissionLauncher = registerForActivityResult
            (new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (AccessibilityUtils.isAccessibilityServiceEnabled(this, AppUsage.class)) {
                        accessibilityPermission = checkAccessibilityPermission();
                        if (accessibilityPermission && notificationPermission) {
                            proceed.setBackgroundResource(R.drawable.activated_button);
                        } else {
                            proceed.setBackgroundResource(R.drawable.unactivated_button);
                        }
                    }
            }
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_request_page);

        proceed = findViewById(R.id.proceed);

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            // Dark mode
            ((ImageButton) findViewById(R.id.permission_help)).setImageResource(R.drawable.help_dark);
            ((ImageButton) findViewById(R.id.notifications_help)).setImageResource(R.drawable.help_dark);
        } else {
            // Light mode
            ((ImageButton) findViewById(R.id.permission_help)).setImageResource(R.drawable.help_light);
            ((ImageButton) findViewById(R.id.notifications_help)).setImageResource(R.drawable.help_light);
        }

        accessibilityPermission = checkAccessibilityPermission();

        proceed.setOnClickListener(view -> {
            if (checkAccessibilityPermission() && checkNotifications(this)) {
                proceed.setBackgroundResource(R.drawable.activated_button);
                resume();
                finish();
            }
        });

        findViewById(R.id.grant_accessibility).setOnClickListener(view -> {
           if (!checkAccessibilityPermission()) {
               Toast.makeText(this, "Please enable accessibility permission in this app", Toast.LENGTH_LONG).show();
               Intent settings = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
               launchAccessibilitySettings(settings);
           } else {
               accessibilityPermission = checkAccessibilityPermission();
               if (accessibilityPermission && notificationPermission)
                   proceed.setBackgroundResource(R.drawable.activated_button);
               else
                   proceed.setBackgroundResource(R.drawable.unactivated_button);
           }
        });

        findViewById(R.id.grant_notifications).setOnClickListener(view -> {
           if (!checkNotifications(this)) {
               Toast.makeText(this, "Please enable display over other apps permission for this app", Toast.LENGTH_LONG).show();
               checkNotifications(this);
           } else {
               notificationPermission = checkNotifications(this);
               if (notificationPermission) {
                   proceed.setBackgroundResource(R.drawable.activated_button);
                   ((Button) findViewById(R.id.grant_notifications)).setText("Granted");
                   findViewById(R.id.grant_notifications).setBackgroundResource(R.drawable.activated_button);
               } else {
                   proceed.setBackgroundResource(R.drawable.unactivated_button);
                   findViewById(R.id.grant_notifications).setBackgroundResource(R.drawable.unactivated_button);
               }
           }
        });

        View filter = findViewById(R.id.filter);

        if (accessibilityPermission && notificationPermission) {
            proceed.setBackgroundResource(R.drawable.activated_button);
        } else {
            proceed.setBackgroundResource(R.drawable.unactivated_button);
        }

        ConstraintLayout constraintLayout = findViewById(R.id.permission_layout);

        final boolean[] on = {false, false};

        findViewById(R.id.permission_help).setOnClickListener(v -> {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            if (!on[0] && !on[1]) {
                constraintSet.clear(R.id.permission_view, ConstraintSet.TOP); // Clear top constraint
                constraintSet.connect(R.id.permission_view, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                proceed.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .start();
                proceed.setClickable(false);
                findViewById(R.id.grant_accessibility).setClickable(false);
                filter.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();
                on[0] = !on[0];
            }
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        findViewById(R.id.exit_accessibility_perms).setOnClickListener(v -> {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            if (on[0]) {
                constraintSet.clear(R.id.permission_view, ConstraintSet.BOTTOM); // Clear top constraint
                constraintSet.connect(R.id.permission_view, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                proceed.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();
                proceed.setClickable(true);
                findViewById(R.id.grant_accessibility).setClickable(true);
                filter.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .start();
                on[0] = !on[0];
            }
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        findViewById(R.id.notifications_help).setOnClickListener(v -> {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            if (!on[1] && !on[0]) {
                constraintSet.clear(R.id.notifications_view, ConstraintSet.TOP); // Clear top constraint
                constraintSet.connect(R.id.notifications_view, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                proceed.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .start();
                proceed.setClickable(false);
                findViewById(R.id.grant_accessibility).setClickable(false);
                filter.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();
                on[1] = !on[1];
            }
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        findViewById(R.id.exit_notifications_perms).setOnClickListener(v -> {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            if (on[1]) {
                constraintSet.clear(R.id.notifications_view, ConstraintSet.BOTTOM); // Clear top constraint
                constraintSet.connect(R.id.notifications_view, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                proceed.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();
                proceed.setClickable(true);
                findViewById(R.id.grant_accessibility).setClickable(true);
                filter.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .start();
                on[1] = !on[1];
            }
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        if (accessibilityPermission && notificationPermission) {
            resume();
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

    private boolean checkNotifications(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Check if we should show rationale
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with showing notifications
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied, inform the user
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resume() {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(getFilesDir(), "accounts.txt")))) {
            String username = reader.readLine();
            String password = reader.readLine();

            if (username != null && password != null && username.contains("[un]:") && password.contains("[pw]:"))
                Login.setAccount(username, password);
            startActivity(new Intent(Permission.this, Login.class));
        } catch (IOException e) {
            startActivity(new Intent(Permission.this, Login.class));
        }
    }
}
