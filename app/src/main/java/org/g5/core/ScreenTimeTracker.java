package org.g5.core;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import org.g5.util.StringUtil;
import org.g5.util.Time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ScreenTimeTracker {

    public static List<AppUsageEntry> getApps(Context context, Calendar last) {
        Log.d("ScreenTimeTracker | ScreenTimeTracker.class", "Getting apps");
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        if (usageStatsManager == null) {
            Log.d("ScreenTimeTracker | ScreenTimeTracker.class", "nvm");
            return new ArrayList<>();
        }

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        long startTime = last.getTimeInMillis();

        UsageEvents events = usageStatsManager.queryEvents(startTime, endTime);
        UsageEvents.Event event = new UsageEvents.Event();

        List<AppUsageEntry> appUsageMap = new ArrayList<>();
        Stack<AppUsageEntry> activeSessions = new Stack<>();

        while (events.hasNextEvent()) {
            events.getNextEvent(event);

            String packageName = event.getPackageName();
            int eventType = event.getEventType();
            long eventTime = event.getTimeStamp();

            if (eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                // App opened, start tracking
                activeSessions.push(new AppUsageEntry(packageName, eventTime));

            } else if (eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                // App moved to background, calculate usage
                if (!activeSessions.isEmpty()) {
                    AppUsageEntry lastSession = activeSessions.pop();

                    if (lastSession.packageName.equals(packageName)) {
                        long duration = eventTime - lastSession.time;

                        // Ignore very short interruptions from System UI
                        if (duration > 1000) { // Ignore events < 1s
                            appUsageMap.add(new AppUsageEntry(packageName, duration));
                        }
                    }
                }
            }
        }

        return compute(refine(appUsageMap));
    }

    private static List<AppUsageEntry> refine(List<AppUsageEntry> appList) {
        List<AppUsageEntry> refinedList = new ArrayList<>();
        if (appList.isEmpty())
            return refinedList;

        AppUsageEntry currentApp = appList.get(0);

        for (int i = 1; i < appList.size(); i++) {
            AppUsageEntry nextApp = appList.get(i);

            Log.d("ScreenTimeTracker", currentApp.packageName + " " + currentApp.time);
            if (isSystemApp(nextApp.packageName)) {
                // Merge system UI time into the previous app
                currentApp.time += (nextApp.time - currentApp.time);
            } else {
                // Move to the next app
                refinedList.add(currentApp);
                currentApp = nextApp;
            }
        }
        refinedList.add(currentApp);

        return refinedList;
    }

    public static List<AppUsageEntry> compute(List<AppUsageEntry> apps) {
        Map<String, AppUsageEntry> appMap = new HashMap<>();

        for (AppUsageEntry app : apps) {
            if (appMap.containsKey(app.packageName)) {
                AppUsageEntry existingApp = appMap.get(app.packageName);
                existingApp.time += app.time;
            } else {
                appMap.put(app.packageName, new AppUsageEntry(app.packageName, app.time));
            }
        }

        List<AppUsageEntry> result = new ArrayList<>(appMap.values());

        for (AppUsageEntry app : result) {
            Log.d("ScreenTimeTracker.class", "Compute | App: " + app.packageName + " Time: " + Time.formatTime(Time.millsToTime(app.time)));
        }

        return result;
    }


    private static boolean isSystemApp(String packageName) {
        return packageName != null && (
                packageName.equals("android") ||
                        packageName.contains("systemui") ||
                        packageName.contains("launcher") ||
                        packageName.contains("packageinstaller") ||
                        packageName.contains("system") ||
                        packageName.contains("mtp") ||
                        packageName.contains("searchbox") ||
                        packageName.contains("settings")
        );
    }

    public static void requestUsageAccess(Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        context.startActivity(intent);
    }

    public static boolean isUsageAccessGranted(Context context) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 60 * 60 * 24, currentTime);

        return stats != null && !stats.isEmpty();
    }

    public static class AppUsageEntry {
        public String packageName;
        public long time;

        public AppUsageEntry(String packageName, long startTime) {
            this.packageName = packageName;
            this.time = startTime;
        }
    }
}
