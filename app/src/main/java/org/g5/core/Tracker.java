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
import org.g5.util.LineWriter;
import org.g5.util.Time;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Tracker extends Application {

    public static final List<ScreenTimeTracker.AppUsageEntry>[] data = new ArrayList[3];

    private static ScreenTimeTracker.AppUsageEntry[] top3DailyApps = new ScreenTimeTracker.AppUsageEntry[3];
    private static ScreenTimeTracker.AppUsageEntry[] top3WeeklyApps = new ScreenTimeTracker.AppUsageEntry[3];
    private static ScreenTimeTracker.AppUsageEntry[] top3MonthlyApps = new ScreenTimeTracker.AppUsageEntry[3];
    public static File[] files = new File[3];

    private static boolean checked = false;
    private static Calendar startTime = Calendar.getInstance();
    private static List<ScreenTimeTracker.AppUsageEntry> appEntries = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        File trackingRecord = new File(getFilesDir(), "trackingRecord.txt");
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(Activity activity) {
                if (!checked) {
                    Log.d("ScreenTimeTracker | Tracker.class", "Activity started " + Time.formatClockTime(Time.ldtToArray(LocalDateTime.now())));

                    initData(false, activity);

                    checked = true;

                    try {
                        if (!trackingRecord.createNewFile())
                            trackingRecord.createNewFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    LineWriter lineWriter = new LineWriter(trackingRecord);
                    String lastTrack = lineWriter.getLine(0);
                    LocalDateTime localDateTime = LocalDateTime.now();
                    if (!lastTrack.isEmpty()) {
                        String[] date = lastTrack.split("\\s*[ymdhos]\\s*"); // Split using letters
                        int year = Integer.parseInt(date[0]);
                        int month = Integer.parseInt(date[1]) - 1; // Fix: Calendar months are 0-based
                        int day = Integer.parseInt(date[2]);
                        int hour = Integer.parseInt(date[3]);
                        int minute = Integer.parseInt(date[4]);
                        int second = Integer.parseInt(date[5]);

                        startTime.set(year, month, day, hour, minute, second);

                        Log.d("Date true", localDateTime.getDayOfMonth() + "/" + localDateTime.getMonthValue() + "/" + localDateTime.getYear());
                    } else {
                        startTime.set(
                                localDateTime.getYear(),
                                localDateTime.getMonthValue() - 1, // Fix: Convert 1-based month to 0-based
                                localDateTime.getDayOfMonth(), // Fix: Use day of month, not day of year
                                0, 0, 1
                        );
                        Log.d("Date false", localDateTime.getDayOfMonth() + "/" + localDateTime.getMonthValue() + "/" + localDateTime.getYear());
                    }

                    String record = localDateTime.getYear() + "y" + localDateTime.getMonthValue() + "m" + localDateTime.getDayOfMonth() + "d" + localDateTime.getHour() + "h" + localDateTime.getMinute() + "o" + localDateTime.getSecond() + "s";
                    lineWriter.writeLine(record, 0);
                    Log.d("Time period | update", record);

                    appEntries.addAll(ScreenTimeTracker.getApps(activity, startTime));
                    appEntries = ScreenTimeTracker.compute(appEntries, false);
                    Data.sortAppsDescending(appEntries);

                    for (int i = 0; i < appEntries.size(); i++) {
                        Log.d("All Apps And Time Yes", "App name: " + appEntries.get(i).packageName + " Time: " + appEntries.get(i).time);
                    }

                    try {
                        updateData(appEntries, activity);
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
            home.setAppNameDaily(top3DailyApps);
            home.setAppTimeDaily(top3DailyApps);
            home.setAppIconDaily(top3DailyApps);
            home.noDataDaily(top3DailyApps[0] != null && top3DailyApps[0].packageName.isEmpty());
            home.setAppNameWeekly(top3WeeklyApps);
            home.setAppTimeWeekly(top3WeeklyApps);
            home.setAppIconWeekly(top3WeeklyApps);
            home.noDataWeekly(top3WeeklyApps[0] != null && top3WeeklyApps[0].packageName.isEmpty());
            home.setAppNameMonthly(top3MonthlyApps);
            home.setAppTimeMonthly(top3MonthlyApps);
            home.setAppIconMonthly(top3MonthlyApps);
            home.noDataMonthly(top3MonthlyApps[0] != null && top3MonthlyApps[0].packageName.isEmpty());
        }
    }

    public static void updateData(List<ScreenTimeTracker.AppUsageEntry> apps, Context context) throws IOException {
        List<ScreenTimeTracker.AppUsageEntry> daily = Data.getDataFromFile(Data.createDailyFile(context));
        daily.addAll(apps);
        daily = ScreenTimeTracker.compute(daily, false);
        Data.sortAppsDescending(daily);
        Data.updateData(files[0], daily);
        data[0] = daily;
        top3DailyApps = Time.getTop3ByTime(data[0]);

        List<ScreenTimeTracker.AppUsageEntry> weekly = Data.getDataFromFile(Data.createWeeklyFile(context));
        weekly.addAll(apps);
        weekly = ScreenTimeTracker.compute(weekly, false);
        Data.sortAppsDescending(weekly);
        Data.updateData(files[1], weekly);
        data[1] = weekly;
        top3WeeklyApps = Time.getTop3ByTime(weekly);

        List<ScreenTimeTracker.AppUsageEntry> monthly = Data.getDataFromFile(Data.createMonthlyFile(context));
        monthly.addAll(apps);
        monthly = ScreenTimeTracker.compute(monthly, false);
        Data.sortAppsDescending(monthly);
        Data.updateData(files[2], monthly);
        data[2] = monthly;
        top3MonthlyApps = Time.getTop3ByTime(monthly);
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