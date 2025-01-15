package org.g5.core;

import android.content.Context;
import android.util.Log;

import org.g5.overseer.Index;
import org.g5.util.Family;
import org.g5.util.Pair;
import org.g5.util.Time;
import org.g5.util.TriMap;

import java.io.File;
import java.io.IOException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Data {

    public static File createDailyFile(Context context) throws IOException {
        String date = new SimpleDateFormat("dd_MM_yy").format(new Date());
        File file = new File(context.getFilesDir(), date + ".txt");
        if (!file.createNewFile()) {
            file.createNewFile();
        }
        return file;
    }

    //create a weekly file
    public static File createWeeklyFile(Context context) throws IOException {
        Calendar cal = Calendar.getInstance();
        int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
        String date = new SimpleDateFormat("MM").format(new Date()) + "week" + weekOfMonth + "" + new SimpleDateFormat("yy").format(new Date());
        File file = new File(context.getFilesDir(), date + ".txt");
        if (!file.createNewFile()) {
            file.createNewFile();
        }
        return file;
    }

    //create a monthly file
    public static File createMonthlyFile(Context context) throws IOException {
        String date = new SimpleDateFormat("MMyy").format(new Date());
        File file = new File(context.getFilesDir(), date + ".txt");
        if (!file.createNewFile()) {
            file.createNewFile();
        }
        return file;
    }

    //delete a daily file
    public static void deleteDailyFile() throws IOException {
        String date = new SimpleDateFormat("dd_MM_yy").format(new Date());
        File file = new File(Index.getFilesDirectory(), date + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    //create a weekly file
    public static void deleteWeeklyFile() throws IOException {
        Calendar cal = Calendar.getInstance();
        int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
        String date = new SimpleDateFormat("MM").format(new Date()) + "week" + weekOfMonth + "" + new SimpleDateFormat("yy").format(new Date());
        File file = new File(Index.getFilesDirectory(), date + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    //create a monthly file
    public static void deleteMonthlyFile() throws IOException {
        String date = new SimpleDateFormat("MMyy").format(new Date());
        File file = new File(Index.getFilesDirectory(), date + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    public static TriMap<String, int[], int[]> getDataFromFile(File file) {
        TriMap<String, int[], int[]> data = new TriMap<>();
        String line;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = null;
                String appName;
                String[] timeParts;
                String[] timeRecordParts;
                try {
                    parts = line.split(": ");
                    appName = parts[0];
                    timeParts = parts[1].split("\\s*[hms]\\s*");
                    timeRecordParts = parts[2].split("\\s*[hms]\\s*");
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }

                int[] timeArray = {
                        Integer.parseInt(timeParts[0]),  // hours
                        Integer.parseInt(timeParts[1]),  // minutes
                        Integer.parseInt(timeParts[2])   // seconds
                };

                int[] timeRecordedArray = {
                        Integer.parseInt(timeRecordParts[0]),  // hours
                        Integer.parseInt(timeRecordParts[1]),  // minutes
                        Integer.parseInt(timeRecordParts[2])   // seconds
                };

                data.newEntry(appName, timeArray, timeRecordedArray);
            }
        } catch (IOException e) {}
        return data;
    }

    public static void updateData(File file, TriMap<String, int[], int[]> map) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
            List<String> keys = map.getKeys();

            // app entry
            for (String key : keys) {
                Family<String, Pair<int[], int[]>> childParent = map.getEntry(key);
                ArrayList<Pair<int[], int[]>> children = childParent.getChildren();
                String app = childParent.getParent();

                // app data
                for (Pair<int[], int[]> child : children) {
                    String time = Time.formatTime(child.getValue1());
                    String timeRecorded = Time.formatTime(child.getValue2());

                    int hashOfTimeRecorded = TriMap.hash(key, child.getValue1(), child.getValue2());
                    writer.write(app + ": " + time + ": " + timeRecorded + " [Hash: " + hashOfTimeRecorded + "]\n");
                }
            }

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int[] computeData(TriMap<String, int[], int[]> data, String key) {
        int[] time = null;
        Family<String, Pair<int[], int[]>> childParent = data.getEntry(key);
        if (childParent != null) {
            ArrayList<Pair<int[], int[]>> children = childParent.getChildren();
            int timeSpent = 0;
            for (int j = 0; j < children.size(); j++)
                timeSpent += Time.convertToSeconds(children.get(j).getValue1());
            time = Time.convertSecondsToArray(timeSpent);
        }
        return time;
    }

    public static HashMap<String, int[]> extractData(TriMap<String, int[], int[]> map) {
        HashMap<String, int[]> data = new HashMap<>();
        List<String> keys = map.getKeys();

        for (int i = 0; i < map.size(); i++) {
            String appName = map.getEntry(keys.get(i)).getParent();
            data.put(appName, computeData(map, appName));
        }

        return data;
    }

    public static Pair<String, int[]> getHighestScreenTime(File file) {
        Pair<String, int[]> highestApp = new Pair<>();
        TriMap<String, int[], int[]> map = getDataFromFile(file);
        List<String> keys = map.getKeys();
        int highest = 0;

        for (int i = 0; i < map.size(); i++) {
            String key = keys.get(i);
            if (Time.convertToSeconds(computeData(map, key)) > highest)
                highestApp.setPair(key, computeData(map, key));
        }

        return highestApp;
    }

    public static ArrayList<int[]> getFilteredScreenTime(File file) {
        ArrayList<int[]> screenTime = new ArrayList<>();
        HashMap<String, int[]> appUsage = extractData(getDataFromFile(file));

        for (Map.Entry<String, int[]> entry : appUsage.entrySet())
            screenTime.add(entry.getValue());
        return screenTime;
    }

    public static int[] getScreenTime(File file) {
        int[] screenTime = new int[] {0, 0, 0};
        HashMap<String, int[]> appUsage = extractData(getDataFromFile(file));

        for (Map.Entry<String, int[]> entry : appUsage.entrySet())
            screenTime = Time.getTimeCombination(screenTime, entry.getValue());
        return screenTime;
    }

    public static TriMap<String, Integer, int[]> sortAppsDescending(TriMap<String, int[], int[]> apps) {
        // Extract keys and initialize a list to hold apps with total times
        List<String> keys = apps.getKeys();
        List<Pair<String, Integer>> appTimeList = new ArrayList<>();

        // Compute total time for each app and store it in the list
        for (String key : keys) {
            int totalTimeInSeconds = Time.convertToSeconds(computeData(apps, key));
            appTimeList.add(new Pair<>(key, totalTimeInSeconds));
        }

        // Sort the list in descending order based on total time
        appTimeList.sort((a, b) -> Integer.compare(b.getValue2(), a.getValue2()));

        // Create a new TriMap and populate it with sorted apps
        TriMap<String, Integer, int[]> sortedApps = new TriMap<>();
        for (Pair<String, Integer> entry : appTimeList) {
            String appName = entry.getValue1();
            int totalTime = entry.getValue2();

            // Get the original entry from the unsorted TriMap
            Family<String, Pair<int[], int[]>> originalEntry = apps.getEntry(appName);
            for (Pair<int[], int[]> child : originalEntry.getChildren()) {
                sortedApps.newEntry(appName, totalTime, child.getValue1());
            }
        }

        return sortedApps;
    }


}

