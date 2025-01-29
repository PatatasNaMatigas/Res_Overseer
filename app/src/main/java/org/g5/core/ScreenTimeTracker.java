package org.g5.core;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ScreenTimeTracker {

    public static Map<String, Long> getAccurateAppUsage(Context context) {
        UsageStatsManager usageStatsManager =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        if (usageStatsManager == null) {
            return new HashMap<>();
        }

        // Define time range (last hour)
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR, -1);
        long startTime = calendar.getTimeInMillis();

        // Get event logs
        UsageEvents events = usageStatsManager.queryEvents(startTime, endTime);
        UsageEvents.Event event = new UsageEvents.Event();

        Map<String, Long> appUsageMap = new HashMap<>();
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
                        long duration = eventTime - lastSession.startTime;

                        // Ignore very short interruptions from System UI
                        if (duration > 1000) { // Ignore events < 1s
                            appUsageMap.put(packageName, appUsageMap.getOrDefault(packageName, 0L) + duration);
                        }
                    }
                }
            }
        }

        return appUsageMap; // Returns app package names with total usage duration
    }

    private static String getAppName(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return (String) packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(packageName, 0));
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    public static Map<String, String> getAppNamesAndUsage(Context context) {
        Map<String, Long> usageMap = getAccurateAppUsage(context);
        Map<String, String> appNamesWithUsage = new HashMap<>();

        for (Map.Entry<String, Long> entry : usageMap.entrySet()) {
            String appName = getAppName(context, entry.getKey());
            long usageTimeInSeconds = entry.getValue() / 1000;
            appNamesWithUsage.put(appName, usageTimeInSeconds + " seconds");
        }

        return appNamesWithUsage;
    }

    public static void requestUsageAccess(Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        context.startActivity(intent);
    }

    private static class AppUsageEntry {
        String packageName;
        long startTime;

        public AppUsageEntry(String packageName, long startTime) {
            this.packageName = packageName;
            this.startTime = startTime;
        }
    }
}
