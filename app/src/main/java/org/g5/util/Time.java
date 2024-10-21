package org.g5.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Time {
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

    public static int convertToSeconds(int[] time) {
        return time[0] * 3600 + time[1] * 60 + time[2];
    }

    public static String[][] getTop3ByTime(Map<String, int[]> app) {
        int min = Math.min(app.size(), 3);
        String[][] entries = new String[][] {
                new String[] {"", ""},
                new String[] {"", ""},
                new String[] {"", ""}
        };

        List<Map.Entry<String, int[]>> entryList = new ArrayList<>(app.entrySet());

        entryList.sort((e1, e2) -> {
            int seconds1 = convertToSeconds(e1.getValue());
            int seconds2 = convertToSeconds(e2.getValue());
            return Integer.compare(seconds2, seconds1);
        });

        List<String> sortedEntry = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : entryList)
            sortedEntry.add(entry.getKey());

        for (int i = 0; i < min; i++) {
            entries[i] = new String[] {
                    sortedEntry.get(i),
                    timeArrayToString(app.get(sortedEntry.get(i)))
            };
        }

        return entries;
    }

    public static int hourToSecond(int hour) {
        return hour * 3600;
    }

    public static int hourToMin(int hour) {
        return hour * 60;
    }

    public static int minToSecond(int minute) {
        return minute * 60;
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

    private static String timeArrayToString(int[] time) {
        return time[0] + "h " + time[1] + "m " + time[2] + "s";
    }
}
