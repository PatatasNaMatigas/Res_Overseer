package org.g5.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.g5.core.AppUsage;
import org.g5.core.Data;
import org.g5.overseer.Index;
import org.g5.overseer.R;
import org.g5.ui.adapters.DailyAppAdapter;
import org.g5.ui.adapters.MonthlyAppAdapter;
import org.g5.ui.adapters.WeeklyAppAdapter;
import org.g5.ui.model.DailyAppModel;
import org.g5.ui.model.MonthlyAppModel;
import org.g5.ui.model.WeeklyAppModel;
import org.g5.util.Time;
import org.g5.util.TriMap;

import java.time.LocalDateTime;

public class Summary extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_page);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            TriMap<String, Integer, int[]> apps = Data.sortAppsDescending(Data.getDataFromFile(AppUsage.getFiles()[0]));
            String topApp = (!apps.getKeys().isEmpty()) ? apps.getKeys().get(0) : "";
            Drawable topAppIcon = AppUsage.getAppIcon(this, topApp);

            RecyclerView dailyRecyclerView = findViewById(R.id.daily_recycler_view);

            DailyAppModel lastView = null;
            List<DailyAppModel> dailyAppModels = new ArrayList<>();
            for (String appName : apps.getKeys()) {
                Drawable icon = AppUsage.getAppIcon(this, appName);
                int[] time = Time.convertSecondsToArray(apps.getEntry(appName).getChildren().get(0).getValue1());
                String timeSpent = Time.formatTime(time);
                dailyAppModels.add(lastView = new DailyAppModel(
                        lastView,
                        AppUsage.getAppName(this, appName),
                        timeSpent,
                        icon)
                );
            }

            List<LocalDate> ld = Time.getCurrentWeekDaysUntilToday();
            WeeklyAppModel weeklyAppEntry = null;
            List<WeeklyAppModel> weeklyAppModels = new ArrayList<>();
            for (LocalDate date : ld) {
                weeklyAppEntry = createWeeklyAppEntry(weeklyAppEntry, Time.ldToDateArray(date));
                if (weeklyAppEntry == null)
                    continue;
                weeklyAppModels.add(weeklyAppEntry);
            }

            RecyclerView weeklyRecyclerView = findViewById(R.id.weekly_recycler_view);

            runOnUiThread(() -> {
                initUi();
                ((ImageView) findViewById(R.id.app_icon)).setImageDrawable(topAppIcon);
                ((TextView) findViewById(R.id.app_name)).setText(AppUsage.getAppName(this, topApp));

                DailyAppAdapter adapter = new DailyAppAdapter(dailyAppModels);
                dailyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                dailyRecyclerView.setAdapter(adapter);

                WeeklyAppAdapter weeklyAppAdapter = new WeeklyAppAdapter(weeklyAppModels);
                weeklyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                weeklyRecyclerView.setAdapter(weeklyAppAdapter);
            });
        });
        executor.shutdown();

        RecyclerView recyclerView = findViewById(R.id.monthly_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        List<MonthlyAppModel> monthlyAppModels = new ArrayList<>();

        List<LocalDate> localDate = Time.getMonths(getFilesDir());
        for (LocalDate date : localDate) {
            if (date.getMonthValue() > 12)
                break;
            TriMap<String, int[], int[]> dataFromFile = Data.getDataFromFile(Data.getMonthlyFile(this, date.getMonthValue(), date.getYear()));
            try {
                monthlyAppModels.add(new MonthlyAppModel(date.getMonth().toString(), new Drawable[] {
                        (!dataFromFile.getKeys().isEmpty()) ? AppUsage.getAppIcon(this, dataFromFile.getKeys().get(0)) : null,
                        (dataFromFile.getKeys().size() > 1) ? AppUsage.getAppIcon(this, dataFromFile.getKeys().get(1)) : null,
                        (dataFromFile.getKeys().size() > 2) ? AppUsage.getAppIcon(this, dataFromFile.getKeys().get(2)) : null,
                        (dataFromFile.getKeys().size() > 3) ? AppUsage.getAppIcon(this, dataFromFile.getKeys().get(3)) : null,
                }));
            } catch (IndexOutOfBoundsException e) {}
        }

        MonthlyAppAdapter adapter = new MonthlyAppAdapter(monthlyAppModels);
        recyclerView.setAdapter(adapter);
    }

    private WeeklyAppModel createWeeklyAppEntry(WeeklyAppModel weeklyAppModel, int[] date) {
        File fileByDate = Data.getFileByDate(this, date);
        if (!fileByDate.exists()) {
            return null;
        }
        TriMap<String, Integer, int[]> apps = Data.sortAppsDescending(Data.getDataFromFile(fileByDate));

        String[] app = new String[]{
                (!apps.getKeys().isEmpty() && apps.getEntry(apps.getKeys().get(0)) != null
                        && !apps.getEntry(apps.getKeys().get(0)).getChildren().isEmpty())
                        ? Time.formatTime(
                        Time.convertSecondsToArray(
                                apps.getEntry(apps.getKeys().get(0)).getChildren().get(0).getValue1()
                        )
                )
                        : "",
                (apps.getKeys().size() > 1 && apps.getEntry(apps.getKeys().get(1)) != null
                        && !apps.getEntry(apps.getKeys().get(1)).getChildren().isEmpty())
                        ? Time.formatTime(
                        Time.convertSecondsToArray(
                                apps.getEntry(apps.getKeys().get(1)).getChildren().get(0).getValue1()
                        )
                )
                        : "",
                (apps.getKeys().size() > 2 && apps.getEntry(apps.getKeys().get(2)) != null
                        && !apps.getEntry(apps.getKeys().get(2)).getChildren().isEmpty())
                        ? Time.formatTime(
                        Time.convertSecondsToArray(
                                apps.getEntry(apps.getKeys().get(2)).getChildren().get(0).getValue1()
                        )
                )
                        : "",
        };

        Drawable[] appIcon = new Drawable[]{
                (apps.getKeys().isEmpty()) ? null : AppUsage.getAppIcon(this, apps.getKeys().get(0)),
                (apps.getKeys().size() > 1) ? AppUsage.getAppIcon(this, apps.getKeys().get(1)) : null,
                (apps.getKeys().size() > 2)  ? AppUsage.getAppIcon(this, apps.getKeys().get(2)) : null,
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
        boolean am = (localDateTime.getHour() < 12);
        String h = (!am ? localDateTime.getHour() - 12 : localDateTime.getHour()) + "";
        String m = (localDateTime.getMinute() > 9) ? localDateTime.getMinute() + "" : ("0" + localDateTime.getMinute());
        String time = h + ":" + m + (am ? "am" : "pm");

        ((TextView) findViewById(R.id.time)).setText(time);
        ((TextView) findViewById(R.id.day)).setText(String.valueOf(localDateTime.getDayOfMonth()));
        ((TextView) findViewById(R.id.month)).setText(localDateTime.getMonth().toString());

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(() -> {
            long currentTimeMillis = System.currentTimeMillis();
            long delayUntilNextMinute = 60000 - (currentTimeMillis % 60000);

            scheduler.scheduleAtFixedRate (() -> {
                LocalDateTime currentTime = LocalDateTime.now();

                boolean morning = (currentTime.getHour() > 12);
                String hour = (morning ? currentTime.getHour() - 12 : currentTime.getHour()) + "";
                String minute = (currentTime.getMinute() > 9) ? currentTime.getMinute() + "" : ("0" + currentTime.getMinute());
                String timeString = hour + ":" + minute + (morning ? "am" : "pm");

                runOnUiThread(() -> {
                    ((TextView) findViewById(R.id.time)).setText(timeString);
                    ((TextView) findViewById(R.id.day)).setText(String.valueOf(currentTime.getDayOfMonth()));
                    ((TextView) findViewById(R.id.month)).setText(currentTime.getMonth().toString());
                });
            }, delayUntilNextMinute, 60000, TimeUnit.MILLISECONDS);
        }, 0, TimeUnit.MILLISECONDS);

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
            finish();
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