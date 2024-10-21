package org.g5.core;


import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.g5.ui.scene.Menu;
import org.g5.util.Time;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AppUsage extends AccessibilityService {

    public static HashMap<String, int[]> app = new HashMap<>();
    private static HashMap<String, List<int[]>> finalApp = new HashMap<>();
    private static Map<String, int[]> weekly = new HashMap<>();
    private static Map<String, int[]> monthly = new HashMap<>();
    private static String lastApp = "";
    private static String lastAppOnOff = "";
    private static LocalDateTime lastSwitchTime = null;
    private static String[][][] top3Apps = new String[3][3][];
    private static String[][] top3AppName = new String[3][];
    private static Drawable[][] appIcon = new Drawable[3][];
    private static File[] files = new File[3];

    private BroadcastReceiver screenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                try {
                    finalizeAppTime();  // This will store the time spent so far in the file
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                lastSwitchTime = LocalDateTime.now();
                lastApp = lastAppOnOff;
                refreshContent();
            }
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                files[0] = Data.createDailyFile();
                files[1] = Data.createWeeklyFile();
                files[2] = Data.createMonthlyFile();

                if (app.isEmpty())
                    app = Data.retrieveDataFromFile(files[0]);
                if (weekly.isEmpty())
                    weekly = Data.retrieveDataFromFile(files[1]);
                if (monthly.isEmpty())
                    monthly = Data.retrieveDataFromFile(files[2]);

                PackageManager packageManager = getPackageManager();
                ApplicationInfo appInfo = packageManager.getApplicationInfo(event.getPackageName().toString(), 0);
                boolean isAnApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0;

                if (isAnApp) {
                    String currentApp = event.getPackageName().toString();
                    LocalDateTime currentTime = LocalDateTime.now();

                    if (!lastApp.isEmpty()) {
                        int[] timeDifference = Time.getTimeDifference(
                                new int[] {
                                        lastSwitchTime.getHour(),
                                        lastSwitchTime.getMinute(),
                                        lastSwitchTime.getSecond()
                                },
                                new int[] {
                                        currentTime.getHour(),
                                        currentTime.getMinute(),
                                        currentTime.getSecond()
                                }
                        );

                        app.replace(lastApp, Time.getTimeCombination(app.get(lastApp), timeDifference));
                        // Instead of updating the total time, just store this session's time
                        finalApp.computeIfAbsent(lastApp, k -> new ArrayList<>()).add(timeDifference);

                        // Store the individual session time spent for the last app
                        Data.updateFileWithTime(files[0], finalApp);
                        Data.updateFileWithTime(files[1], finalApp);
                        Data.updateFileWithTime(files[2], finalApp);
                    }

                    // Reset for the new app session
                    lastApp = currentApp;
                    lastSwitchTime = currentTime;
                } else {
                    finalizeAppTime();
                    lastApp = "";
                    lastSwitchTime = null;
                    return;
                }

                top3Apps[0] = Time.getTop3ByTime(Data.retrieveDataFromFile(files[0]));
                top3Apps[1] = Time.getTop3ByTime(Data.retrieveDataFromFile(files[1]));
                top3Apps[2] = Time.getTop3ByTime(Data.retrieveDataFromFile(files[2]));
                for (int i = 0; i < 3; i++) {
                    top3AppName[i] = new String[]{
                            getAppName(top3Apps[i][0][0]),
                            getAppName(top3Apps[i][1][0]),
                            getAppName(top3Apps[i][2][0])
                    };
                }
                for (int i = 0; i < 3; i++) {
                    appIcon[i] = new Drawable[]{
                            getAppIcon(top3Apps[i][0][0]),
                            getAppIcon(top3Apps[i][1][0]),
                            getAppIcon(top3Apps[i][2][0])
                    };
                }

                refreshContent();

            }

        } catch (Exception e) {
            Log.e("AppUsage", "Error in onAccessibilityEvent", e);
        }
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "Error Occured", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenStateReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenStateReceiver);
    }

    private void finalizeAppTime() throws IOException {
        if (lastApp != null && lastSwitchTime != null) {
            LocalDateTime currentTime = LocalDateTime.now();

            int[] timeDifference = Time.getTimeDifference(
                    new int[] {
                            lastSwitchTime.getHour(),
                            lastSwitchTime.getMinute(),
                            lastSwitchTime.getSecond()
                    },
                    new int[] {
                            currentTime.getHour(),
                            currentTime.getMinute(),
                            currentTime.getSecond()
                    }
            );

            // Instead of updating the total time, store the individual session time spent
            app.replace(lastApp, Time.getTimeCombination(app.get(lastApp), timeDifference));
            finalApp.computeIfAbsent(lastApp, k -> new ArrayList<>()).add(timeDifference);

            // Save this session's time
            Data.updateFileWithTime(files[0], finalApp);
            Data.updateFileWithTime(files[1], finalApp);
            Data.updateFileWithTime(files[2], finalApp);

            lastSwitchTime = null;
            lastAppOnOff = lastApp;
            lastApp = null;
        }
    }

    public void refreshContent() {
        Log.d("AppUsage.class", "test: " + Arrays.toString(top3AppName[0]));
        Menu.setAppNameDaily(top3AppName[0]);
        Menu.setAppTimeDaily(top3Apps[0]);
        Menu.setAppIconDaily(appIcon[0]);
        Menu.noDataDaily(top3Apps[0] == null);
        Menu.setAppNameWeekly(top3AppName[1]);
        Menu.setAppTimeWeekly(top3Apps[1]);
        Menu.setAppIconWeekly(appIcon[1]);
        Menu.noDataWeekly(top3Apps[1] == null);
        Menu.setAppNameMonthly(top3AppName[2]);
        Menu.setAppTimeMonthly(top3Apps[2]);
        Menu.setAppIconMonthly(appIcon[2]);
        Menu.noDataMonthly(top3Apps[2] == null);
    }

    private Drawable getAppIcon(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationIcon(appInfo).getCurrent();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private String getAppName(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }
}
