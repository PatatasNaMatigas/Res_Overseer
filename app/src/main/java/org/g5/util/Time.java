package org.g5.util;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.g5.core.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    public static String[][] getTop3ByTime(TriMap<String, int[], int[]> map) {
        ArrayList<Pair<String, Integer>> app = new ArrayList<>();
        List<String> keys = map.getKeys();

        for (String key : keys) {
            int timeInSeconds = Time.convertToSeconds(Data.computeData(map, key));
            app.add(new Pair<>(key, timeInSeconds));
        }

        app.sort((a, b) -> b.getValue2() - a.getValue2());

        int min = Math.min(app.size(), 3);
        String[][] entries = new String[3][2];
        for (int i = 0; i < 3; i++) {
            if (i < min) {
                entries[i][0] = app.get(i).getValue1();
                entries[i][1] = Time.formatTime(Time.convertSecondsToArray((app.get(i).getValue2())));
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

    public static float secondToMills(int second) {
        return second * 1000;
    }

    public static String formatTime(int[] time) {
        return time[0] + "h " + time[1] + "m " + time[2] + "s";
    }

    public static String formatClockTime(int[] time, String period) {
        return time[0] + ":" + ((time[1] < 10) ? " " + time[1] : time[1]) + period;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getMonthName(int month) {
        return Month.of(month).getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getDayOfWeek(int year, int month, int dayOfMonth) {
        // Create a LocalDate object
        LocalDate date = LocalDate.of(year, month, dayOfMonth);

        // Get the day of the week and return its display name
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

}