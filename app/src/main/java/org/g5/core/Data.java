package org.g5.core;

import android.content.Context;
import android.util.Log;

import org.g5.util.Time;

import java.io.File;
import java.io.IOException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        String date = new SimpleDateFormat("MM").format(new Date()) + "week" + weekOfMonth + new SimpleDateFormat("yy").format(new Date());
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
    public static void deleteDailyFile(Context context) throws IOException {
        String date = new SimpleDateFormat("dd_MM_yy").format(new Date());
        File file = new File(context.getFilesDir(), date + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    //create a weekly file
    public static void deleteWeeklyFile(Context context) throws IOException {
        Calendar cal = Calendar.getInstance();
        int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
        String date = new SimpleDateFormat("MM").format(new Date()) + "week" + weekOfMonth + "" + new SimpleDateFormat("yy").format(new Date());
        File file = new File(context.getFilesDir(), date + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    //create a monthly file
    public static void deleteMonthlyFile(Context context) throws IOException {
        String date = new SimpleDateFormat("MMyy").format(new Date());
        File file = new File(context.getFilesDir(), date + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    public static List<ScreenTimeTracker.AppUsageEntry> getDataFromFile(File file) {
        List<ScreenTimeTracker.AppUsageEntry> data = new ArrayList<>();
        String line;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts;
                String appName;
                String[] timeParts;
                try {
                    parts = line.split(": ");
                    appName = parts[0];
                    timeParts = parts[1].split("\\s*[hms]\\s*");
                    int[] timeArray = {
                            Integer.parseInt(timeParts[0]),
                            Integer.parseInt(timeParts[1]),
                            Integer.parseInt(timeParts[2])
                    };

                    data.add(new ScreenTimeTracker.AppUsageEntry(appName, Time.convertToSeconds(timeArray)));
                    Log.d("Data.class__", "App name: " + appName + " Time: " + Time.convertToSeconds(timeArray));
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {}
        return data;
    }

    public static void updateData(File file, List<ScreenTimeTracker.AppUsageEntry> apps) {
        if (apps.isEmpty())
            return;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

            for (ScreenTimeTracker.AppUsageEntry app : apps)
                writer.write(app.packageName + ": " + Time.formatSeconds((int) app.time) + '\n');

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ScreenTimeTracker.AppUsageEntry> computeTime(List<ScreenTimeTracker.AppUsageEntry> apps) {
        Map<String, ScreenTimeTracker.AppUsageEntry> dataMap = new HashMap<>();

        for (ScreenTimeTracker.AppUsageEntry app : apps) {
            if (dataMap.containsKey(app.packageName)) {
                dataMap.get(app.packageName).time += app.time;
            } else {
                dataMap.put(app.packageName, new ScreenTimeTracker.AppUsageEntry(app.packageName, app.time));
            }
        }

        return new ArrayList<>(dataMap.values());
    }

    public static long computeAll(List<ScreenTimeTracker.AppUsageEntry> data) {
        long time = 0;
        for (ScreenTimeTracker.AppUsageEntry app : data) {
            time += app.time;
        }
        return (int) time;
    }

    public static void sortAppsDescending(List<ScreenTimeTracker.AppUsageEntry> apps) {
        apps.sort((a, b) -> Math.toIntExact(b.time - a.time));
    }

    public static File getFileByDate(Context context, int[] date) {
        String formattedDate = String.format("%d_%02d_%02d.txt", date[0], date[1], date[2] % 100);
        return new File(context.getFilesDir(), formattedDate);
    }

    public static File getDailyFile(Context context, LocalDate date) throws IOException {
        Date d = new Date();
        d.setDate(date.getDayOfMonth());
        d.setMonth(date.getMonthValue() - 1);
        d.setYear(date.getYear());
        String formatted = new SimpleDateFormat("dd_MM_yy").format(d);
        File file = new File(context.getFilesDir(), formatted + ".txt");
        Log.d("Date--", file.getName() + " " + file.exists());
        return file;
    }

    public static File getWeeklyFile(Context context, int[] date) {
        LocalDate dateFinal = LocalDate.of(date[2], date[1], date[0]);

        // Format the month and year correctly
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM");
        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yy");

        String formattedDate = dateFinal.format(monthFormatter) + "week" +
                Calendar.getInstance().get(Calendar.WEEK_OF_MONTH) +
                dateFinal.format(yearFormatter);

        File file = new File(context.getFilesDir(), formattedDate + ".txt");
        return file;
    }

    public static File getMonthlyFile(Context context, int month, int year) {
        Date date = new Date();
        date.setMonth(month);
        date.setYear(year);
        String formatted = new SimpleDateFormat("MMyy").format(date);
        return new File(context.getFilesDir(), formatted + ".txt");
    }
}

