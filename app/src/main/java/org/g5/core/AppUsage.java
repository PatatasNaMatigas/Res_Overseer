package org.g5.core;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.g5.pet.Pet;
import org.g5.ui.Home;
import org.g5.ui.Permission;
import org.g5.util.Pair;
import org.g5.util.Time;
import org.g5.util.TriMap;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

public class AppUsage extends AccessibilityService {

    private static int[] date;
    private static int[] lastBreakTime = new int[] {0, 0, 0};
    private static final TriMap<String, int[], int[]>[] data = new TriMap[3];
    public static final Pair<String, int[]> lastApp = new Pair<>();
    public static File[] files = new File[3];
    private static final String[][][] top3Apps = new String[3][3][];
    private static final String[][] top3AppName = new String[3][];
    private static final Drawable[][] appIcon = new Drawable[3][];

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int[] currentTime = Time.ldtToArray(LocalDateTime.now());

            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                if (!lastApp.bothEmpty()) {
                    int[] totalTime = new int[3];
                    for (TriMap<String, int[], int[]> datum : data) {
                        totalTime = Time.getTimeDifference(currentTime, lastApp.getValue2());
                        datum.newEntry(lastApp.getValue1(), totalTime, currentTime);
                    }
                    lastBreakTime = currentTime;
                    Pet.startHealthDecay(Time.convertToSeconds(totalTime));
                }
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                lastApp.setPair(lastApp.getValue1(), currentTime);
                lastBreakTime = Time.getTimeDifference(lastBreakTime, currentTime);
                Pet.startHealthRegen(Time.convertToSeconds(lastBreakTime));
            }
        }
    };


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            initData(false, this);

            PackageManager packageManager = getPackageManager();
            ApplicationInfo appInfo;
            String appName;
            Log.d("TEST DEBUG", getAppName(event.getPackageName().toString()));
            try {
                appInfo = packageManager.getApplicationInfo(event.getPackageName().toString(), 0);
                appName = appInfo.packageName;
            } catch (PackageManager.NameNotFoundException e) {
                appName = event.getPackageName().toString();
            }

            Log.d("App Entry", appName + " isAnApp=" + isAnApp(appName));
            Log.d("App Entry - B/A", "A) Last: " + lastApp.getValue1() + " Now: " + appName);

            LocalDateTime ldt = LocalDateTime.now();
            int[] totalTime = new int[3];
            int[] currentTime = Time.ldtToArray(ldt);
            if (isAnApp(appName)) {
                if (date == null) {
                    date = new int[]{
                            ldt.getMonthValue(),
                            ldt.getDayOfMonth(),
                            ldt.getYear()
                    };
                }
                String app = event.getPackageName().toString();

                if (!lastApp.bothEmpty()) {
                    int[] dateNow = new int[]{
                            ldt.getMonthValue(),
                            ldt.getDayOfMonth(),
                            ldt.getYear()
                    };
                    Log.d("PET TEST | If", "------------------------------------");
                    totalTime = Time.getTimeDifference(currentTime, lastApp.getValue2());
                    Log.d("PET TEST | If", Time.convertToSeconds(totalTime) + "");
                    Home.checkForNotif(lastApp.getValue1(), Time.convertToSeconds(Time.getTimeDifference(currentTime, lastApp.getValue2())));
                    if (!Arrays.equals(date, dateNow)) {
                        int[] before = Time.getTimeDifference(Time.MIDNIGHT, lastApp.getValue2());
                        int[] after = new int[]{
                                ldt.getHour(),
                                ldt.getMinute(),
                                ldt.getSecond()
                        };

                        for (TriMap<String, int[], int[]> datum : data)
                            datum.newEntry(lastApp.getValue1(), before, currentTime);

                        initData(false, this);

                        data[0] = Data.getDataFromFile(files[0]);
                        data[1] = Data.getDataFromFile(files[1]);
                        data[2] = Data.getDataFromFile(files[2]);

                        for (TriMap<String, int[], int[]> datum : data)
                            datum.newEntry(lastApp.getValue1(), after, currentTime);

                        date = dateNow;

                    } else {
                        for (TriMap<String, int[], int[]> datum : data)
                            datum.newEntry(lastApp.getValue1(), totalTime, currentTime);
                    }
                }

                for (int i = 0; i < data.length; i++)
                    data[i].newEntry(app, Time.BLANK_TIME, currentTime);
                lastApp.setPair(app, currentTime);

                try {
                    updateData();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Log.d("App Entry - B/A", "B) Last: " + lastApp.getValue1() + " Now:" + event.getPackageName().toString());
                refreshContent();
            } else {
                Log.d("PET TEST | Else", "------------------------------------");
                Log.d("PET TEST | Else", Time.convertToSeconds(totalTime) + "");
                try {
                    Home.checkForNotif(lastApp.getValue1(), Time.convertToSeconds(Time.getTimeDifference(currentTime, lastApp.getValue2())));
                } catch (Exception e) {
                    Home.checkForNotif(lastApp.getValue1(), 0);
                }
            }
        }
    }

    public boolean isAnApp(String packageName) {
        if (packageName.contains("inputmethod"))
            return false;
        if (packageName.contains("settings") ||
                packageName.contains("google") ||
                !packageName.contains("com.android") ||
                packageName.contains("chrome"))
            return true;
        return !packageName.contains("android") &&
                !packageName.contains("launcher") &&
                !packageName.contains("games");
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "Error Occured", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Intent permissionIntent = new Intent(this, Permission.class);
        permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(permissionIntent);
        return super.onUnbind(intent);
    }

    public static void initData(boolean overwrite, Context context) {
        try {
            files[0] = Data.createDailyFile(context);
            files[1] = Data.createWeeklyFile(context);
            files[2] = Data.createMonthlyFile(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!overwrite) {
            for (int i = 0; i < 3; i++) {
                if (data[i] == null || data[i].isEmpty()) {
                    data[i] = Data.getDataFromFile(files[i]);
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                data[i] = Data.getDataFromFile(files[i]);
            }
        }
    }

    public void updateData() throws IOException {
        for (int i = 0; i < data.length; i++) {
            Data.updateData(files[i], data[i]);
            top3Apps[i] = Time.getTop3ByTime(data[i]);
        }

        for (int i = 0; i < 3; i++) {
            top3AppName[i] = new String[]{
                    getAppName(top3Apps[i][0][0]),
                    getAppName(top3Apps[i][1][0]),
                    getAppName(top3Apps[i][2][0])
            };
            appIcon[i] = new Drawable[]{
                    getAppIcon(top3Apps[i][0][0]),
                    getAppIcon(top3Apps[i][1][0]),
                    getAppIcon(top3Apps[i][2][0])
            };
        }
    }

    public static void refreshContent() {
        Home.setAppNameDaily(top3AppName[0]);
        Home.setAppTimeDaily(top3Apps[0]);
        Home.setAppIconDaily(appIcon[0]);
        try {
            Home.noDataDaily(top3Apps[0] == null);
        } catch (Exception e) {
            Home.noDataDaily(false);
        }
        Home.setAppNameWeekly(top3AppName[1]);
        Home.setAppTimeWeekly(top3Apps[1]);
        Home.setAppIconWeekly(appIcon[1]);
        try {
            Home.noDataWeekly(top3Apps[1] == null);
        } catch (Exception e) {
            Home.noDataWeekly(false);
        }
        Home.setAppNameMonthly(top3AppName[2]);
        Home.setAppTimeMonthly(top3Apps[2]);
        Home.setAppIconMonthly(appIcon[2]);
        try {
            Home.noDataMonthly(top3Apps[2] == null);
        } catch (Exception e) {
            Home.noDataMonthly(false);
        }
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

    public static Drawable getAppIcon(AppCompatActivity appCompatActivity, String packageName) {
        PackageManager packageManager = appCompatActivity.getPackageManager();
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

    public static String getAppName(AppCompatActivity appCompatActivity, String packageName) {
        PackageManager packageManager = appCompatActivity.getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    public static File[] getFiles() {
        return files;
    }
}
