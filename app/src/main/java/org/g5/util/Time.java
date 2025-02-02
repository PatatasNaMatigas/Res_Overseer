package org.g5.util;

import android.annotation.SuppressLint;
import android.util.Log;

import org.g5.core.Data;
import org.g5.core.ScreenTimeTracker;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Time {
    public static int[] BLANK_TIME = new int[]{0, 0, 0};
    public static int[] MIDNIGHT = new int[]{24, 0, 0};

    private int h, m, s;

    public Time(int h, int m, int s) {
        this.h = h;
        this.m = m;
        this.s = s;
    }

    public int getHour() {
        return h;
    }

    public int getMinute() {
        return m;
    }

    public int getSecond() {
        return s;
    }

    public static int[] getTimeDifference(int[] time1, int[] time2) {
        int totalSeconds1 = time1[0] * 3600 + time1[1] * 60 + time1[2];
        int totalSeconds2 = time2[0] * 3600 + time2[1] * 60 + time2[2];

        int diffInSeconds = Math.abs(totalSeconds2 - totalSeconds1);

        int hours = diffInSeconds / 3600;
        int minutes = (diffInSeconds % 3600) / 60;
        int seconds = diffInSeconds % 60;

        return new int[] {hours, minutes, seconds};
    }

    public static int[] getTimeCombination(int[] time1, int[] time2) {
        if (time1 == null)
            return time2;
        if (time2 == null)
            return time1;

        int totalSeconds1 = time1[0] * 3600 + time1[1] * 60 + time1[2];
        int totalSeconds2 = time2[0] * 3600 + time2[1] * 60 + time2[2];

        int diffInSeconds = Math.abs(totalSeconds2 + totalSeconds1);

        int hours = diffInSeconds / 3600;
        int minutes = (diffInSeconds % 3600) / 60;
        int seconds = diffInSeconds % 60;

        return new int[] {hours, minutes, seconds};
    }

    public static int[] convertSecondsToArray(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int s = seconds % 60;

        return new int[] {hours, minutes, s};
    }

    public static int convertToSeconds(int[] time) {
        return time[0] * 3600 + time[1] * 60 + time[2];
    }

    public static String[][] getTop3ByTime(List<ScreenTimeTracker.AppUsageEntry> apps) {
        ArrayList<ScreenTimeTracker.AppUsageEntry> top3 = new ArrayList<>();

        for (ScreenTimeTracker.AppUsageEntry app : apps) {
            int timeInSeconds = Data.computeTime(apps, app.packageName);
            top3.add(new ScreenTimeTracker.AppUsageEntry(app.packageName, timeInSeconds));
            Log.d("Time.class | Before", "App: " + app.packageName + " " + timeInSeconds);
        }

        top3.sort((a, b) -> Long.compare(b.time, a.time));

        for (ScreenTimeTracker.AppUsageEntry app : top3) {
            Log.d("Time.class | After", "App: " + app.packageName + " " + app.time);
        }

        int min = Math.min(top3.size(), 3);
        String[][] entries = new String[3][2];
        for (int i = 0; i < 3; i++) {
            if (i < min) {
                entries[i][0] = top3.get(i).packageName;
                entries[i][1] = Time.formatTime(Time.convertSecondsToArray((int) top3.get(i).time));
            } else {
                entries[i][0] = "";
                entries[i][1] = "";
            }
        }

        return entries;
    }

    @SuppressLint("NewApi")
    public static int[] ldToDateArray(LocalDate localDateTime) {
        return new int[]{
                localDateTime.getDayOfMonth(),
                localDateTime.getMonthValue(),
                localDateTime.getYear(),
        };
    }


    @SuppressLint("NewApi")
    public static int[] ldtToArray(LocalDateTime localDateTime) {
        return new int[]{
                localDateTime.getHour(),
                localDateTime.getMinute(),
                localDateTime.getSecond(),
        };
    }

    public static int hourToSecond(int hour) {
        return hour * 3600;
    }

    public static int hourToMin(int hour) {
        return hour * 60;
    }

    public static int hourToMills(int hour) {
        return hour * 60 * 1000;
    }

    public static int minToSecond(int minute) {
        return minute * 60;
    }

    public static int minToMills(int minute) {
        return minute * 60 * 1000;
    }

    public static float minToHour(int minute) {
        return minute / 60.0f;
    }

    public static float secondToMin(int second) {
        return second / 60f;
    }

    public static float secondToHour(int second) {
        return second / 3600f;
    }

    public static long secondToMills(int second) {
        return second * 1000L;
    }

    public static float millsToSeconds(long mills) {
        return (float) mills / 1000;
    }

    public static int[] millsToTime(long millis) {
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (millis % (1000 * 60)) / 1000;

        return new int[] {
                (int) hours,
                (int) minutes,
                (int) seconds
        };
    }

    public static String formatTime(int[] time) {
        return time[0] + "h " + time[1] + "m " + time[2] + "s";
    }

    public static String formatMillis(long millis) {
        long totalSeconds = millis / 1000;
        return formatSeconds((int) totalSeconds);
    }

    public static String formatSeconds(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        return String.format("%dh%02dm%02ds", hours, minutes, secs);
    }

    public static String formatMinutes(int minutes) {
        return formatSeconds(minutes * 60);
    }

    public static String formatHours(int hours) {
        return formatSeconds(hours * 3600);
    }

    public static String formatTime(int[] time, boolean extract) {
        if (extract) {
            String h = (time[0] == 0) ? "" : time[0] + "h ";
            String m = (time[1] == 0) ? "" : time[1] + "m ";
            String s = (time[2] == 0) ? "" : time[2] + "s";
            return h + m + s;
        }
        return "";
    }
    public static String formatClockTime(int[] time) {
        return ((time[0] > 12) ? time[0] - 12 : time[0]) + ":" + ((time[1] < 10) ? "0" + time[1] : time[1]) + (time[0] > 12 ? " PM" : " AM");
    }

    public static List<LocalDate> getCurrentWeekDaysUntilToday() {
        List<LocalDate> daysOfWeek = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;
        LocalDate startOfWeek = today.with(firstDayOfWeek);

        for (LocalDate date = today; !date.isBefore(startOfWeek); date = date.minusDays(1)) {
            daysOfWeek.add(date);
        }

        Collections.reverse(daysOfWeek);

        return daysOfWeek;
    }

    public static List<LocalDate> getMonths(File directory) {
        List<LocalDate> dates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMyy");

        LocalDate currentDate = LocalDate.now();

        while (true) {
            // Format the current date as MMyy
            String formattedDate = currentDate.format(formatter);
            File file = new File(directory, formattedDate + ".txt");

            if (file.exists()) {
                dates.add(currentDate);
                currentDate = currentDate.plusMonths(1);
            } else {
                break;
            }
        }

        Collections.reverse(dates);

        return dates;
    }
}