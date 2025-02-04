package org.g5.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.g5.core.Data;
import org.g5.core.ScreenTimeTracker;
import org.g5.core.Tracker;
import org.g5.overseer.R;
import org.g5.ui.adapters.DailyAppAdapter;
import org.g5.ui.adapters.WeeklyAppAdapter;
import org.g5.ui.models.DailyAppModel;
import org.g5.ui.models.WeeklyAppModel;
import org.g5.util.Time;

import java.time.LocalDateTime;

public class Summary extends AppCompatActivity {

    private Map<String, Drawable> iconCache = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_page);

        initUi();

        ExecutorService dataExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        dataExecutor.execute(() -> {
            List<ScreenTimeTracker.AppUsageEntry> apps = Tracker.appEntries;
            String topPackageName = !apps.isEmpty() ? apps.get(0).packageName : "";

            runOnUiThread(() -> {
                Drawable topAppIcon = getCachedIcon(this, topPackageName);
                ((ImageView) findViewById(R.id.app_icon)).setImageDrawable(topAppIcon);
                ((TextView) findViewById(R.id.app_name)).setText(Tracker.getAppName(this, topPackageName));

                initializeDailyAdapter(apps);
                initializeWeeklyAdapter();
                initializeMonthlyAdapter();
            });
        });
    }

    private void initializeDailyAdapter(List<ScreenTimeTracker.AppUsageEntry> apps) {
        RecyclerView dailyRecyclerView = findViewById(R.id.daily_recycler_view);
        dailyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        DailyAppModel last = null;
        List<DailyAppModel> dailyModels = new ArrayList<>();
        for (ScreenTimeTracker.AppUsageEntry app : apps) {
            Drawable icon = getCachedIcon(this, app.packageName);
            String timeSpent = Time.formatTime(Time.convertSecondsToArray((int) app.time));
            dailyModels.add(last = new DailyAppModel(
                    last,
                    Tracker.getAppName(this, app.packageName),
                    timeSpent,
                    icon
            ));
        }

        // Initialize adapter
        DailyAppAdapter adapter = new DailyAppAdapter();
        dailyRecyclerView.setAdapter(adapter);
        adapter.submitList(dailyModels);
    }

    private void initializeWeeklyAdapter() {
        RecyclerView weeklyRecyclerView = findViewById(R.id.weekly_recycler_view);
        weeklyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<WeeklyAppModel> weeklyModels = createWeeklyModels();
        weeklyRecyclerView.setAdapter(new WeeklyAppAdapter(weeklyModels));
    }

    private List<WeeklyAppModel> createWeeklyModels() {
        List<LocalDate> ld = Time.getCurrentWeekDaysUntilToday();
        WeeklyAppModel weeklyAppEntry = null;
        List<WeeklyAppModel> weeklyAppModels = new ArrayList<>();
        for (LocalDate date : ld) {
            weeklyAppEntry = createWeeklyAppEntry(weeklyAppEntry, Time.ldToDateArray(date));
            if (weeklyAppEntry == null)
                continue;
            weeklyAppModels.add(weeklyAppEntry);
        }

        return weeklyAppModels;
    }

    private void initializeMonthlyAdapter() {
//        RecyclerView monthlyRecyclerView = findViewById(R.id.monthly_recycler_view);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
//        monthlyRecyclerView.setLayoutManager(gridLayoutManager);
//
////        List<MonthlyAppModel> monthlyModels = createMonthlyModels();
//        monthlyRecyclerView.setAdapter(new MonthlyAppAdapter(monthlyModels));
    }

    private Drawable getCachedIcon(AppCompatActivity context, String packageName) {
        if (!iconCache.containsKey(packageName)) {
            iconCache.put(packageName, Tracker.getAppIcon(context, packageName));
        }
        return iconCache.get(packageName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("Summary.destroyed", "destroyed");
    }

    private WeeklyAppModel createWeeklyAppEntry(WeeklyAppModel weeklyAppModel, int[] date) {
        File fileByDate = Data.getWeeklyFile(this, date);
        if (!fileByDate.exists())
            return null;

        List<ScreenTimeTracker.AppUsageEntry> apps = Data.sortAppsDescending(Data.getDataFromFile(fileByDate));

        String[] app = new String[]{
                (!apps.isEmpty() && apps.get(0) != null)
                        ? Time.formatTime(Time.convertSecondsToArray((int) apps.get(0).time))
                        : "",
                (apps.size() > 1 && apps.get(1) != null)
                        ? Time.formatTime(Time.convertSecondsToArray((int) apps.get(1).time))
                        : "",
                (apps.size() > 2 && apps.get(2) != null)
                        ? Time.formatTime(Time.convertSecondsToArray((int) apps.get(2).time))
                        : "",
        };

        Drawable[] appIcon = new Drawable[]{
                (apps.isEmpty()) ? null : Tracker.getAppIcon(this, apps.get(0).packageName),
                (apps.size() > 1) ? Tracker.getAppIcon(this, apps.get(1).packageName) : null,
                (apps.size() > 2) ? Tracker.getAppIcon(this, apps.get(2).packageName) : null,
        };

        LocalDate localDate = LocalDate.of(date[2], date[1], date[0]);

        return new WeeklyAppModel(
                localDate.format(DateTimeFormatter.ofPattern("E")),
                localDate.format(DateTimeFormatter.ofPattern("dd")),
                localDate.format(DateTimeFormatter.ofPattern("MMM")),
                weeklyAppModel,
                app,
                appIcon
        );
    }

    private void initUi() {
        LocalDateTime localDateTime = LocalDateTime.now();

        ((TextView) findViewById(R.id.time)).setText(Time.formatClockTime(Time.ldtToArray(localDateTime)));
        ((TextView) findViewById(R.id.day)).setText(String.valueOf(localDateTime.getDayOfMonth()));
        ((TextView) findViewById(R.id.month)).setText(localDateTime.getMonth().toString());

        ConstraintLayout constraintLayout = findViewById(R.id.summary_layout);

        Button exitDrawer = findViewById(R.id.exit_drawer);
        ImageButton popDrawer = findViewById(R.id.popDrawer);

        popDrawer.setOnClickListener(view -> {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.clear(R.id.drawer, ConstraintSet.END); // Clear top constraint
            constraintSet.connect(R.id.drawer, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            popDrawer.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setStartDelay(0)
                    .start();
            popDrawer.setClickable(false);
            findViewById(R.id.filter).animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();

            findViewById(R.id.daily).setClickable(false);
            findViewById(R.id.weekly).setClickable(false);
            findViewById(R.id.monthly).setClickable(false);

            constraintSet.clear(R.id.exit_drawer, ConstraintSet.START);
            constraintSet.connect(R.id.exit_drawer, ConstraintSet.START, R.id.drawer, ConstraintSet.END);
            constraintSet.connect(R.id.exit_drawer, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        exitDrawer.setOnClickListener(view -> {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.clear(R.id.drawer, ConstraintSet.START);
            constraintSet.connect(R.id.drawer, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.START);
            popDrawer.animate()
                    .alpha(1f)
                    .setStartDelay(300)
                    .setDuration(300)
                    .start();
            popDrawer.setClickable(true);
            findViewById(R.id.filter).animate()
                    .alpha(0f)
                    .setDuration(300)
                    .start();

            findViewById(R.id.daily).setClickable(true);
            findViewById(R.id.weekly).setClickable(true);
            findViewById(R.id.monthly).setClickable(true);

            constraintSet.clear(R.id.exit_drawer, ConstraintSet.START);
            constraintSet.clear(R.id.exit_drawer, ConstraintSet.END);
            constraintSet.connect(R.id.exit_drawer, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END);
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        findViewById(R.id.summary).setOnClickListener(view -> {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.clear(R.id.drawer, ConstraintSet.START);
            constraintSet.connect(R.id.drawer, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.START);
            popDrawer.animate()
                    .alpha(1f)
                    .setStartDelay(300)
                    .setDuration(300)
                    .start();
            popDrawer.setClickable(true);
            findViewById(R.id.filter).animate()
                    .alpha(0f)
                    .setDuration(300)
                    .start();

            findViewById(R.id.daily).setClickable(true);
            findViewById(R.id.weekly).setClickable(true);
            findViewById(R.id.monthly).setClickable(true);

            constraintSet.clear(R.id.exit_drawer, ConstraintSet.START);
            constraintSet.clear(R.id.exit_drawer, ConstraintSet.END);
            constraintSet.connect(R.id.exit_drawer, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END);
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        findViewById(R.id.home).setOnClickListener(view -> {
            startActivity(new Intent(this, Home.class));
        });

        Button dailyId = findViewById(R.id.daily);
        Button weeklyId = findViewById(R.id.weekly);
        Button monthlyId = findViewById(R.id.monthly);

        dailyId.setOnClickListener(view -> {
            findViewById(R.id.daily_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.weekly_layout).setVisibility(View.GONE);
            findViewById(R.id.monthly_layout).setVisibility(View.GONE);
            dailyId.setBackgroundResource(R.drawable.button_activated_rounded_pro_max);
            dailyId.setTextColor(ContextCompat.getColor(this, R.color.activatedTextColor));
            weeklyId.setBackgroundResource(R.drawable.button_unactivated_rounded_pro_max);
            weeklyId.setTextColor(ContextCompat.getColor(this, R.color.unactivatedTextColor));
            monthlyId.setBackgroundResource(R.drawable.button_unactivated_rounded_pro_max);
            monthlyId.setTextColor(ContextCompat.getColor(this, R.color.unactivatedTextColor));
        });

        weeklyId.setOnClickListener(view -> {
            findViewById(R.id.daily_layout).setVisibility(View.GONE);
            findViewById(R.id.weekly_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.monthly_layout).setVisibility(View.GONE);
            dailyId.setBackgroundResource(R.drawable.button_unactivated_rounded_pro_max);
            dailyId.setTextColor(ContextCompat.getColor(this, R.color.unactivatedTextColor));
            weeklyId.setBackgroundResource(R.drawable.button_activated_rounded_pro_max);
            weeklyId.setTextColor(ContextCompat.getColor(this, R.color.activatedTextColor));
            monthlyId.setBackgroundResource(R.drawable.button_unactivated_rounded_pro_max);
            monthlyId.setTextColor(ContextCompat.getColor(this, R.color.unactivatedTextColor));
        });

        monthlyId.setOnClickListener(view -> {
            findViewById(R.id.daily_layout).setVisibility(View.GONE);
            findViewById(R.id.weekly_layout).setVisibility(View.GONE);
            findViewById(R.id.monthly_layout).setVisibility(View.VISIBLE);
            dailyId.setBackgroundResource(R.drawable.button_unactivated_rounded_pro_max);
            dailyId.setTextColor(ContextCompat.getColor(this, R.color.unactivatedTextColor));
            weeklyId.setBackgroundResource(R.drawable.button_unactivated_rounded_pro_max);
            weeklyId.setTextColor(ContextCompat.getColor(this, R.color.unactivatedTextColor));
            monthlyId.setBackgroundResource(R.drawable.button_activated_rounded_pro_max);
            monthlyId.setTextColor(ContextCompat.getColor(this, R.color.activatedTextColor));
        });
    }
}