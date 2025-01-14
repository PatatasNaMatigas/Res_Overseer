package org.g5.core;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.g5.ui.Menu;
import org.g5.ui.Permission;
import org.g5.util.Pair;
import org.g5.util.Time;
import org.g5.util.TriMap;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AppUsage extends AccessibilityService {

    private static int[] date;
    private static final int[] breakTimeArray = new int[] {0, 0, 0};
    private static final TriMap<String, int[], int[]>[] data = new TriMap[3];
    public static final Pair<String, int[]> lastApp = new Pair<>();
    public static File[] files = new File[3];
    private static final String[][][] top3Apps = new String[3][3][];
    private static final String[][] top3AppName = new String[3][];
    private static final Drawable[][] appIcon = new Drawable[3][];
    private static final ArrayList<int[]> breakTime = new ArrayList<>();
    private static boolean patayAngCP = false;

    // d nmn nagana to e waaaaaaaaaaaahhhhhhhhhhh 😭
    private final BroadcastReceiver screenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int[] currentTime = Time.ldtToArray(LocalDateTime.now());

            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                // Handle screen-off event
                if (!lastApp.bothEmpty()) {
                    for (int i = 0; i < data.length; i++) {
                        Log.d("AppUsage.class", "Screen turned off");
                        int[] totalTime = Time.getTimeDifference(currentTime, lastApp.getValue2());
                        data[i].newEntry(lastApp.getValue1(), totalTime, currentTime);
                    }
                    lastApp.setPair(lastApp.getValue1(), currentTime); // Update last app time
                    patayAngCP = true;
                }
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                // Handle screen-on event
                if (!lastApp.bothEmpty()) {
                    lastApp.setPair(lastApp.getValue1(), currentTime); // Refresh last app timestamp
                    patayAngCP = false;
                }
            }
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            initData(false, this);

            PackageManager packageManager = getPackageManager();
            ApplicationInfo appInfo;
            try {
                appInfo = packageManager.getApplicationInfo(event.getPackageName().toString(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
            boolean isAnApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0;

            // finalize app time
            if (isAnApp) {
                LocalDateTime ldt = LocalDateTime.now();

                // if date is null initialize value
                if (date == null) {
                    date = new int[] {
                            ldt.getMonthValue(),
                            ldt.getDayOfMonth(),
                            ldt.getYear()
                    };
                }
                String app = event.getPackageName().toString();
                int[] currentTime = Time.ldtToArray(ldt);

                if (!lastApp.bothEmpty()) {

                    int[] dateNow = new int[] {
                            ldt.getMonthValue(),
                            ldt.getDayOfMonth(),
                            ldt.getYear()
                    };

                    int[] totalTime = Time.getTimeDifference(currentTime, lastApp.getValue2());
                    if (!Arrays.equals(date, dateNow)) {
                        // pag kinabukasan na (adik mag phone)

                        // hatiin yung time ng before and after 00:00:00
                        /*
                            example
                            totalTime = 00:41:20
                            currentTime = 00:20:19

                            before = 24:00:00 - lastApp.getValue2();
                            after = totalTime - before;
                         */
                        int[] before = Time.getTimeCombination(totalTime, lastApp.getValue2());
                        int[] after = Time.getTimeDifference(Time.MIDNIGHT, before);

                        for (int i = 0; i < data.length; i++)
                            data[i].newEntry(lastApp.getValue1(), before, currentTime);

                        // new day, new data (walang kwentang comment)
                        initData(false, this);

                        data[0] = Data.getDataFromFile(files[0]);
                        data[1] = Data.getDataFromFile(files[1]);
                        data[2] = Data.getDataFromFile(files[2]);

                        for (int i = 0; i < data.length; i++)
                            data[i].newEntry(lastApp.getValue1(), after, currentTime);

                        date = dateNow;

                        Menu.checkForNotif(lastApp.getValue1(), Time.convertToSeconds(after));
                    } else {
                        // pag ndi kinabukasan (duhh)

                        for (int i = 0; i < data.length; i++)
                            data[i].newEntry(lastApp.getValue1(), totalTime, currentTime);
                    }
                }

                for (int i = 0; i < data.length; i++)
                    data[i].newEntry(app, Time.BLANK_TIME, currentTime);
                lastApp.setPair(app, currentTime);

                try {
                    updateData();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                refreshContent();
            }
        }
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "Error Occured", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter onOffFilter = new IntentFilter();
        onOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
        onOffFilter.addAction(Intent.ACTION_SCREEN_ON);
        onOffFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenStateReceiver, onOffFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenStateReceiver);
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        startActivity(new Intent(this, Permission.class));
        return super.onUnbind(intent);
    }

    public static void initData(boolean overwrite, Context context) {
        try {
            files[0] = Data.createDailyFile(context);
            files[1] = Data.createWeeklyFile(context);
            files[2] = Data.createMonthlyFile(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!overwrite) {
            for (int i = 0; i < 3; i++) {
                if (data[i] == null || data[i].isEmpty()) {
                    data[i] = Data.getDataFromFile(files[i]);
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                data[i] = Data.getDataFromFile(files[i]);
            }
        }
    }

    public void updateData() throws IOException {
        for (int i = 0; i < data.length; i++) {
            Data.updateData(files[i], data[i]);
            top3Apps[i] = Time.getTop3ByTime(data[i]);
        }

        for (int i = 0; i < 3; i++) {
            top3AppName[i] = new String[]{
                    getAppName(top3Apps[i][0][0]),
                    getAppName(top3Apps[i][1][0]),
                    getAppName(top3Apps[i][2][0])
            };
            appIcon[i] = new Drawable[]{
                    getAppIcon(top3Apps[i][0][0]),
                    getAppIcon(top3Apps[i][1][0]),
                    getAppIcon(top3Apps[i][2][0])
            };
        }
    }

    public static void refreshContent() {
        Log.d("AppUsage.class", "test: " + Arrays.toString(top3AppName[0]));
        Menu.setAppNameDaily(top3AppName[0]);
        Menu.setAppTimeDaily(top3Apps[0]);
        Menu.setAppIconDaily(appIcon[0]);
        try {
            Menu.noDataDaily(top3Apps[0] == null);
        } catch (Exception e) {
            Menu.noDataDaily(false);
        }
        Menu.setAppNameWeekly(top3AppName[1]);
        Menu.setAppTimeWeekly(top3Apps[1]);
        Menu.setAppIconWeekly(appIcon[1]);
        try {
            Menu.noDataWeekly(top3Apps[1] == null);
        } catch (Exception e) {
            Menu.noDataWeekly(false);
        }
        Menu.setAppNameMonthly(top3AppName[2]);
        Menu.setAppTimeMonthly(top3Apps[2]);
        Menu.setAppIconMonthly(appIcon[2]);
        try {
            Menu.noDataMonthly(top3Apps[2] == null);
        } catch (Exception e) {
            Menu.noDataMonthly(false);
        }
    }

    private Drawable getAppIcon(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationIcon(appInfo).getCurrent();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private String getAppName(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    public static String getAppName(String packageName, AppCompatActivity appCompatActivity) {
        PackageManager packageManager = appCompatActivity.getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    public static File[] getFiles() {
        return files;
    }

    public static ArrayList<int[]> getBreakTime() {
        return breakTime;
    }

    public static void clearData() {
        for (int i = 0; i < data.length; i++)
            data[i].clear();
    }
}
