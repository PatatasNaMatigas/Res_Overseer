package org.g5.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;

import org.g5.core.ScreenTimeTracker;
import org.g5.overseer.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Permission extends AppCompatActivity {

    private boolean appUsagePermission;
    private boolean notificationPermission;
    private Button proceed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_request_page);

        proceed = findViewById(R.id.proceed);

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            // Dark mode
            ((ImageButton) findViewById(R.id.app_usage_help)).setImageResource(R.drawable.help_dark);
            ((ImageButton) findViewById(R.id.notifications_help)).setImageResource(R.drawable.help_dark);
        } else {
            // Light mode
            ((ImageButton) findViewById(R.id.app_usage_help)).setImageResource(R.drawable.help_light);
            ((ImageButton) findViewById(R.id.notifications_help)).setImageResource(R.drawable.help_light);
        }

        appUsagePermission = checkAppUsageAccess();
        notificationPermission = checkNotifications(this);

        proceed.setOnClickListener(view -> {
            if (checkAppUsageAccess() && checkNotifications(this)) {
                proceed.setBackgroundResource(R.drawable.activated_button);
                resume();
                finish();
            }
        });

        findViewById(R.id.grant_app_usage).setOnClickListener(view -> {
           if (!checkAppUsageAccess()) {
               Toast.makeText(this, "Please enable accessibility permission in this app", Toast.LENGTH_LONG).show();
               ScreenTimeTracker.requestUsageAccess(this);
           } else {
               appUsagePermission = checkAppUsageAccess();
               if (appUsagePermission && notificationPermission)
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
                   notificationPermission = checkNotifications(this);
                   findViewById(R.id.grant_notifications).setBackgroundResource(R.drawable.unactivated_button);
               }
           }
            if (appUsagePermission && notificationPermission)
                proceed.setBackgroundResource(R.drawable.activated_button);
            else
                proceed.setBackgroundResource(R.drawable.unactivated_button);
        });

        View filter = findViewById(R.id.filter);

        if (appUsagePermission && notificationPermission) {
            proceed.setBackgroundResource(R.drawable.activated_button);
        } else {
            proceed.setBackgroundResource(R.drawable.unactivated_button);
        }

        ConstraintLayout constraintLayout = findViewById(R.id.permission_layout);

        final boolean[] on = {false, false};

        findViewById(R.id.app_usage_help).setOnClickListener(v -> {
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
                findViewById(R.id.grant_app_usage).setClickable(false);
                filter.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();
                on[0] = !on[0];
            }
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        findViewById(R.id.exit_app_usage_perms).setOnClickListener(v -> {
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
                findViewById(R.id.grant_app_usage).setClickable(true);
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
                findViewById(R.id.grant_app_usage).setClickable(false);
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
                findViewById(R.id.grant_app_usage).setClickable(true);
                filter.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .start();
                on[1] = !on[1];
            }
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        if (appUsagePermission && notificationPermission) {
            resume();
        }
    }

    private boolean checkAppUsageAccess() {
        if (!ScreenTimeTracker.isUsageAccessGranted(this)) {
            findViewById(R.id.grant_app_usage).setBackgroundResource(R.drawable.unactivated_button);
            return false;
        } else {
            ((Button) findViewById(R.id.grant_app_usage)).setText("Granted");
            findViewById(R.id.grant_app_usage).setBackgroundResource(R.drawable.activated_button);
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
                notificationPermission = checkNotifications(this);
                ((Button) findViewById(R.id.grant_notifications)).setText("Granted");
                findViewById(R.id.grant_notifications).setBackgroundResource(R.drawable.activated_button);
                if (appUsagePermission && notificationPermission)
                    proceed.setBackgroundResource(R.drawable.activated_button);
                else
                    proceed.setBackgroundResource(R.drawable.unactivated_button);
            } else {
                notificationPermission = checkNotifications(this);
                findViewById(R.id.grant_notifications).setBackgroundResource(R.drawable.unactivated_button);
                if (appUsagePermission && notificationPermission)
                    proceed.setBackgroundResource(R.drawable.activated_button);
                else
                    proceed.setBackgroundResource(R.drawable.unactivated_button);
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
