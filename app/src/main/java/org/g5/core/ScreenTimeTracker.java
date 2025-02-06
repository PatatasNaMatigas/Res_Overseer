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

import org.g5.util.LineWriter;
import org.g5.util.StringUtil;
import org.g5.util.Time;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ScreenTimeTracker {

    public static List<AppUsageEntry> getApps(Context context, Calendar start) {
        Log.d("ScreenTimeTracker | ScreenTimeTracker.class", "Getting apps");
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        if (usageStatsManager == null) {
            Log.d("ScreenTimeTracker | ScreenTimeTracker.class", "nvm");
            return new ArrayList<>();
        }

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        long startTime = start.getTimeInMillis();

        Log.d("Time period", "Time period: " + Time.formatMillis(endTime - startTime));
        List<AppUsageEntry> appUsageMap;
        if (startTime > endTime) {
            Calendar endBeforeMidnight = Calendar.getInstance();
            endBeforeMidnight.add(Calendar.HOUR, 23);
            endBeforeMidnight.add(Calendar.MINUTE, 59);
            endBeforeMidnight.add(Calendar.SECOND, 59);
            UsageEvents events = usageStatsManager.queryEvents(startTime, endBeforeMidnight.getTimeInMillis());
            UsageEvents.Event event = new UsageEvents.Event();

            appUsageMap = new ArrayList<>();
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
            try {
                Tracker.updateData(appUsageMap, context);
                LocalDateTime localDateTime = LocalDateTime.now();
                String record = localDateTime.getYear() + "y" + localDateTime.getMonthValue() + "m" + localDateTime.getDayOfYear() + "d" + localDateTime.getHour() + "h" + localDateTime.getMinute() + "o" + localDateTime.getSecond() + "s";
                new LineWriter(new File(context.getFilesDir(), "trackingRecord.txt")).writeLine(record, 0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Tracker.initData(false, context);
            Calendar endAfterMidnight = Calendar.getInstance();
            endAfterMidnight.add(Calendar.HOUR, 0);
            endAfterMidnight.add(Calendar.MINUTE, 0);
            endAfterMidnight.add(Calendar.SECOND, 1);
            events = usageStatsManager.queryEvents(endAfterMidnight.getTimeInMillis(), endTime);
            event = new UsageEvents.Event();

            appUsageMap = new ArrayList<>();
            activeSessions = new Stack<>();

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
        } else {
            UsageEvents events = usageStatsManager.queryEvents(startTime, endTime);
            UsageEvents.Event event = new UsageEvents.Event();

            appUsageMap = new ArrayList<>();
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
        }

        return compute(refine(appUsageMap), true);
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

    private static boolean isSystemApp(String packageName) {
        if (packageName != null && packageName.isEmpty())
            return false;

        String lowerCase = packageName.toLowerCase();
        boolean isSystemApp = lowerCase.equals("android") ||
                lowerCase.contains("systemui") ||
                lowerCase.contains("launcher") ||
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

        if (!isSystemApp)
            Log.d("Not_system_app", packageName);
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
