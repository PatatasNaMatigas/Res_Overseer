package org.g5.core;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import org.g5.util.LineIO;
import org.g5.util.Time;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ScreenTimeTracker {

    public static List<AppUsageEntry> getApps(Context context, LocalDate date) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        if (usageStatsManager == null) {
            return new ArrayList<>();
        }

        // Convert LocalDate to start time in UTC+8
        long startTime = date.atStartOfDay(ZoneId.of("GMT+8")).toInstant().toEpochMilli();
        long endTime = Calendar.getInstance().getTimeInMillis(); // Current time

        Log.d("ScreenTimeTracker--", "Tracking from " + startTime + " to " + endTime);

        // Query usage stats
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        List<AppUsageEntry> appUsageEntries = new ArrayList<>();

        if (usageStatsList != null) {
            for (UsageStats stats : usageStatsList) {
                String packageName = stats.getPackageName();
                long timeInForeground = stats.getTotalTimeInForeground(); // Usage in ms
                long lastUsed = stats.getLastTimeUsed(); // Last used timestamp

                // Strict filtering: Only count apps used today (after startTime)
                if (timeInForeground > 0 && lastUsed >= startTime && !isSystemApp(packageName)) {
                    appUsageEntries.add(new AppUsageEntry(packageName, timeInForeground));
                }
            }
        }

        return compute(appUsageEntries, true);
    }

    public static List<AppUsageEntry> compute(List<AppUsageEntry> apps, boolean convert) {
        Map<String, AppUsageEntry> appMap = new HashMap<>();

        List<AppUsageEntry> result = null;
        if (convert) {
            for (AppUsageEntry app : apps) {
                if (appMap.containsKey(app.packageName)) {
                    AppUsageEntry existingApp = appMap.get(app.packageName);
                    existingApp.time += (long) Time.millsToSeconds(app.time);
                } else {
                    appMap.put(app.packageName, new AppUsageEntry(app.packageName, (long) Time.millsToSeconds(app.time)));
                }
            }

            result = new ArrayList<>(appMap.values());

            for (AppUsageEntry app : result) {
                Log.d("ScreenTimeTracker.class", "Compute | App: " + app.packageName + " Time: " + Time.formatTime(Time.millsToTime(app.time)));
            }
        } else {
            for (AppUsageEntry app : apps) {
                if (appMap.containsKey(app.packageName)) {
                    AppUsageEntry existingApp = appMap.get(app.packageName);
                    existingApp.time += (long) Time.millsToSeconds(app.time);
                } else {
                    appMap.put(app.packageName, new AppUsageEntry(app.packageName, app.time));
                }
            }

            result = new ArrayList<>(appMap.values());

            for (AppUsageEntry app : result) {
                Log.d("ScreenTimeTracker.class", "Compute | App: " + app.packageName + " Time: " + Time.formatTime(Time.convertSecondsToArray((int) app.time)));
            }
        }

        return result;
    }

    public static long getTotalScreenTimeForDate(Context context, LocalDate date) {
        long screenTime = 0;

        for (AppUsageEntry app : getApps(context, date)) {
            screenTime += app.time;
            Log.d("ScreenTimeTracker--", Time.formatSeconds((int) screenTime) + " | App: " + app.packageName + " Time: " + Time.formatSeconds((int) app.time));
        }

        return screenTime;
    }


    private static boolean isSystemApp(String packageName) {
        if (packageName != null && packageName.isEmpty())
            return false;

        String lowerCase = packageName.toLowerCase();
        boolean isSystemApp = lowerCase.equals("android") ||
                lowerCase.contains("systemui") ||
                lowerCase.equals("com.android.launcher") ||
                lowerCase.contains("packageinstaller") ||
                lowerCase.contains("system") ||
                lowerCase.contains("mtp") ||
                lowerCase.contains("aod") ||
                lowerCase.contains("microintelligence") ||
                lowerCase.contains("traceur") ||
                lowerCase.contains("gms") ||
                lowerCase.contains("globalminusscreen") ||
                lowerCase.contains("swiftkey") ||
                lowerCase.contains("miui.home") ||
                lowerCase.contains("ugc.trill") ||
                lowerCase.contains("searchbox") ||
                lowerCase.contains("vending") ||
                lowerCase.contains("intentresolver");
        return isSystemApp;
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
