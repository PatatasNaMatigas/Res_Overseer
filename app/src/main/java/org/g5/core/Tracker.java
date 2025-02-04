package org.g5.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.g5.ui.Home;
import org.g5.util.Time;
import org.g5.util.TriMap;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Tracker extends Application {

    private static final List<ScreenTimeTracker.AppUsageEntry>[] data = new ArrayList[3];

    private static final String[][][] top3Apps = new String[3][3][];
    private static final String[][] top3AppName = new String[3][];
    private static final Drawable[][] appIcon = new Drawable[3][];
    public static File[] files = new File[3];

    private static boolean checked = false;
    private static Calendar startTime = Calendar.getInstance();
    public static List<ScreenTimeTracker.AppUsageEntry> appEntries = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        startTime.add(Calendar.HOUR, -12);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(Activity activity) {
                if (!checked) {
                    Log.d("ScreenTimeTracker | Tracker.class", "Activity started " + Time.formatClockTime(Time.ldtToArray(LocalDateTime.now())));

                    initData(false, activity);

                    checked = true;
                    appEntries.addAll(ScreenTimeTracker.getApps(activity, startTime));
                    appEntries = Data.sortAppsDescending(appEntries);

                    try {
                        updateData(appEntries);
                        refreshContent();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    startTime = Calendar.getInstance();
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (!checked) {
                    Log.d("ScreenTimeTracker | Tracker.class", "Activity started " + Time.formatClockTime(Time.ldtToArray(LocalDateTime.now())));
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.d("ScreenTimeTracker | Tracker.class", "Activity paused " + Time.formatClockTime(Time.ldtToArray(LocalDateTime.now())));
                checked = false;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.d("ScreenTimeTracker | Tracker.class", "Activity stopped " + Time.formatClockTime(Time.ldtToArray(LocalDateTime.now())));
                checked = false;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.e("ScreenTimeTracker | Tracker.class", "Main activity finished 😵 " + Time.formatClockTime(Time.ldtToArray(LocalDateTime.now())));
                checked = false;
            }
        });
    }


    public static void refreshContent() {
        Home home = Home.getInstance();

        if (home != null) {
            home.setAppNameDaily(top3AppName[0]);
            home.setAppTimeDaily(top3Apps[0]);
            home.setAppIconDaily(appIcon[0]);
            try {
                home.noDataDaily(top3Apps[0] == null);
            } catch (Exception e) {
                home.noDataDaily(false);
            }
            home.setAppNameWeekly(top3AppName[1]);
            home.setAppTimeWeekly(top3Apps[1]);
            home.setAppIconWeekly(appIcon[1]);
            try {
                home.noDataWeekly(top3Apps[1] == null);
            } catch (Exception e) {
                home.noDataWeekly(false);
            }
            home.setAppNameMonthly(top3AppName[2]);
            home.setAppTimeMonthly(top3Apps[2]);
            home.setAppIconMonthly(appIcon[2]);
            try {
                home.noDataMonthly(top3Apps[2] == null);
            } catch (Exception e) {
                home.noDataMonthly(false);
            }
        }
    }

    public void updateData(List<ScreenTimeTracker.AppUsageEntry> apps) throws IOException {
        Log.d("Tracker.class", "Apps Length: " + apps.size());
        for (int i = 0; i < data.length; i++) {
            Data.updateData(files[i], apps);
            top3Apps[i] = Time.getTop3ByTime(apps);
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
}