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
import org.g5.util.Pair;
import org.g5.util.Time;
import org.g5.util.TriMap;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AppUsage extends AccessibilityService {

    private static int[] breakTimeArray = new int[] {0, 0, 0};
    private static final TriMap<String, int[], int[]> finalApp = new TriMap<>();
    private static final TriMap<String, int[], int[]>[] data = new TriMap[3];
    private static final Pair<String, int[]> lastApp = new Pair<>();
    private static File[] files = new File[3];
    private static final String[][][] top3Apps = new String[3][3][];
    private static final String[][] top3AppName = new String[3][];
    private static final String[][] top3AppTime = new String[3][];
    private static Drawable[][] appIcon = new Drawable[3][];
    private static ArrayList<int[]> breakTime = new ArrayList<>();

    private BroadcastReceiver screenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int[] currentTime = Time.ldtToArray(LocalDateTime.now());

            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                for (int i = 0; i < data.length; i++) {
                    int[] totalTime = Time.getTimeDifference(currentTime, lastApp.getValue2());
                    data[i].newEntry(lastApp.getValue1(), totalTime, currentTime);
                    lastApp.setPair(lastApp.getValue1(), currentTime);
                }
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                lastApp.setPair(lastApp.getValue1(), currentTime);
            }
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            initData();

            PackageManager packageManager = getPackageManager();
            ApplicationInfo appInfo;
            try {
                appInfo = packageManager.getApplicationInfo(event.getPackageName().toString(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
            boolean isAnApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0;

            if (isAnApp) {
                String app = event.getPackageName().toString();
                int[] currentTime = Time.ldtToArray(LocalDateTime.now());

                if (!lastApp.bothEmpty()) {
                    int[] totalTime = Time.getTimeDifference(currentTime, lastApp.getValue2());

                    for (int i = 0; i < data.length; i++)
                        data[i].newEntry(lastApp.getValue1(), totalTime, currentTime);
                }

                for (int i = 0; i < data.length; i++)
                    data[i].newEntry(app, Time.BLANK_TIME, currentTime);
                lastApp.setPair(app, currentTime);

                try {
                    updateData();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                refreshContent();
            }
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
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenStateReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenStateReceiver);
    }

    public void initData() {
        try {
            files[0] = Data.createDailyFile(this);
            files[1] = Data.createWeeklyFile(this);
            files[2] = Data.createMonthlyFile(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < 3; i++) {
            if (data[i] == null || data[i].isEmpty()) {
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
            top3AppTime[i] = new String[]{
                    top3Apps[i][0][1],
                    top3Apps[i][1][1],
                    top3Apps[i][2][1]
            };
            appIcon[i] = new Drawable[]{
                    getAppIcon(top3Apps[i][0][0]),
                    getAppIcon(top3Apps[i][1][0]),
                    getAppIcon(top3Apps[i][2][0])
            };
        }
    }

    public void refreshContent() {
        Log.d("AppUsage.class", "test: " + Arrays.toString(top3AppName[0]));
        Menu.setAppNameDaily(top3AppName[0]);
        Menu.setAppTimeDaily(top3Apps[0]);
        Menu.setAppIconDaily(appIcon[0]);
        try {
            Menu.noDataDaily(top3Apps[0] == null);
        } catch (Exception e) {
            Menu.noDataDaily(false);
        }
        Menu.setAppNameWeekly(top3AppName[1]);
        Menu.setAppTimeWeekly(top3Apps[1]);
        Menu.setAppIconWeekly(appIcon[1]);
        try {
            Menu.noDataWeekly(top3Apps[1] == null);
        } catch (Exception e) {
            Menu.noDataWeekly(false);
        }
        Menu.setAppNameMonthly(top3AppName[2]);
        Menu.setAppTimeMonthly(top3Apps[2]);
        Menu.setAppIconMonthly(appIcon[2]);
        try {
            Menu.noDataMonthly(top3Apps[2] == null);
        } catch (Exception e) {
            Menu.noDataMonthly(false);
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

    private String getAppName(String packageName) {
        PackageManager packageManager = getPackageManager();
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

    public static ArrayList<int[]> getBreakTime() {
        return breakTime;
    }

    public static void clearData() {
        for (int i = 0; i < data.length; i++)
            data[i].clear();
        finalApp.clear();
    }
}
