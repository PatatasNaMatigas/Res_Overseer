package org.g5.core;

import org.g5.overseer.Index;
import org.g5.util.Time;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Data {

    //create a daily file
    public static File createDailyFile() throws IOException {
        String date = new SimpleDateFormat("dd_MM_yy").format(new Date());
        File file = new File(Index.getFilesDirectory(), date + ".txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    //create a weekly file
    public static File createWeeklyFile() throws IOException {
        Calendar cal = Calendar.getInstance();
        int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
        String date = new SimpleDateFormat("MM").format(new Date()) + "week" + weekOfMonth + "" + new SimpleDateFormat("yy").format(new Date());
        File file = new File(Index.getFilesDirectory(), date + ".txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    //create a monthly file
    public static File createMonthlyFile() throws IOException {
        String date = new SimpleDateFormat("MMyy").format(new Date());
        File file = new File(Index.getFilesDirectory(), date + ".txt");
        if (!file.exists()) {
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

    public static void updateFileWithTime(File file, HashMap<String, List<int[]>> appData) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) { // Overwrite the file
            for (Map.Entry<String, List<int[]>> entry : appData.entrySet()) {
                String appName = entry.getKey();
                List<int[]> timeArray = entry.getValue();
                int[] blank = new int[] {0, 0, 0};
                for (int[] time : timeArray) {
                    if (Arrays.equals(time, blank))
                        continue;
                    String timeFormatted = formatTime(time);
                    writer.write(appName + ": " + timeFormatted + "\n");
                }
            }
        }
    }

    // format time from int[] {hours, minutes, seconds}
    private static String formatTime(int[] timeArray) {
        int hours = timeArray[0];
        int minutes = timeArray[1];
        int seconds = timeArray[2];
        return hours + "h" + minutes + "m" + seconds + "s";
    }

    //retrieve data from the file into HashMap<String, int[]>
    public static HashMap<String, int[]> retrieveDataFromFile(File file) throws IOException {
        HashMap<String, List<int[]>> appData = new HashMap<>();
        HashMap<String, int[]> finalAppData = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(": ");
            String appName = parts[0];
            String[] timeParts = parts[1].split("[hms]");

            int[] timeArray = {
                    Integer.parseInt(timeParts[0]),  // hours
                    Integer.parseInt(timeParts[1]),  // minutes
                    Integer.parseInt(timeParts[2])   // seconds
            };
            appData.computeIfAbsent(appName, k -> new ArrayList<>()).add(timeArray);
        }

        // Combine times for each app
        for (Map.Entry<String, List<int[]>> entry : appData.entrySet()) {
            String appName = entry.getKey();
            List<int[]> timeArray = entry.getValue();

            // Initialize oldTime to zeroes or null if not present
            int[] oldTime = finalAppData.get(appName);
            if (oldTime == null) {
                oldTime = new int[]{0, 0, 0};  // Start with zero if no previous data exists
            }

            // Combine the oldTime with each new time entry
            for (int[] time : timeArray) {
                oldTime = Time.getTimeCombination(oldTime, time);
            }

            finalAppData.put(appName, oldTime);  // Update finalAppData with the new total time
        }

        reader.close();
        return finalAppData;
    }
}
