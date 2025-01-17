package org.g5.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.g5.core.AppUsage;
import org.g5.core.Data;
import org.g5.overseer.R;
import org.g5.util.Time;
import org.g5.util.TriMap;

import java.time.LocalDateTime;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Summary extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_page);

        TriMap<String, Integer, int[]> apps = Data.sortAppsDescending(AppUsage.data[0]);

        DailyAppEntry lastView = null;
        int i = 0;
        String topApp = apps.getKeys().get(i);
        Drawable topAppIcon = AppUsage.getAppIcon(this, topApp);
        ((ImageView) findViewById(R.id.app_icon)).setImageDrawable(topAppIcon);
        ((TextView) findViewById(R.id.app_name)).setText(AppUsage.getAppName(this, topApp));
        do {
            String app = apps.getKeys().get(i);
            int[] time = Time.convertSecondsToArray(apps.getEntry(app).getChildren().get(0).getValue1());
            String appTime = Time.formatTime(time);
            lastView = new DailyAppEntry(this)
                    .setName(AppUsage.getAppName(this, app))
                    .setTimeSpent(appTime)
                    .setTimeRecordedRange("1:00pm - 2:00pm")
                    .setIcon(AppUsage.getAppIcon(this, app))
                    .build(lastView);
            i++;
        } while (i < apps.getKeys().size());

        initUi();

        List<LocalDate> ld = Time.getCurrentWeekDaysUntilToday();
        WeeklyAppEntry weeklyAppEntry = null;
        for (LocalDate date : ld) {
            weeklyAppEntry = createWeeklyAppEntry(weeklyAppEntry, Time.ldToDateArray(date));
        }
    }

    private WeeklyAppEntry createWeeklyAppEntry(WeeklyAppEntry weeklyAppEntry, int[] date) {
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

        return new WeeklyAppEntry(this)
                .setDate(new String[]{
                        localDate.format(DateTimeFormatter.ofPattern("dd")),
                        localDate.format(DateTimeFormatter.ofPattern("MMM")),
                        localDate.format(DateTimeFormatter.ofPattern("E"))})
                .setTimeSpent(app)
                .setIcon(appIcon)
                .build(weeklyAppEntry);
    }

    private void initUi() {
        LocalDateTime localDateTime = LocalDateTime.now();
        boolean am = (localDateTime.getHour() >= 12);
        int[] time = new int[] {
                am ? localDateTime.getHour() - 12 : localDateTime.getHour(),
                localDateTime.getMinute()
        };
        ((TextView) findViewById(R.id.time)).setText(Time.formatClockTime(time, am ? "am" : "pm"));
        String day = localDateTime.getDayOfMonth() + "";
        ((TextView) findViewById(R.id.day)).setText(day);
        ((TextView) findViewById(R.id.month)).setText(localDateTime.getMonth().toString());

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(() -> {
            long currentTimeMillis = System.currentTimeMillis();
            long delayUntilNextMinute = 60000 - (currentTimeMillis % 60000);

            scheduler.scheduleAtFixedRate (() -> {
                LocalDateTime currentTime = LocalDateTime.now();

                boolean morning = (currentTime.getHour() >= 12);
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
        });

        Button dailyId = findViewById(R.id.daily);
        Button weeklyId = findViewById(R.id.weekly);
        Button monthlyId = findViewById(R.id.monthly);

        dailyId.setOnClickListener(view -> {
            findViewById(R.id.daily_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.weekly_layout).setVisibility(View.GONE);
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
            dailyId.setBackgroundResource(R.drawable.button_unactivated_rounded_pro_max);
            dailyId.setTextColor(ContextCompat.getColor(this, R.color.unactivatedTextColor));
            weeklyId.setBackgroundResource(R.drawable.button_activated_rounded_pro_max);
            weeklyId.setTextColor(ContextCompat.getColor(this, R.color.activatedTextColor));
            monthlyId.setBackgroundResource(R.drawable.button_unactivated_rounded_pro_max);
            monthlyId.setTextColor(ContextCompat.getColor(this, R.color.unactivatedTextColor));
        });
    }

    private class DailyAppEntry {

        private View view;
        private String name = "";
        private String timeSpent = "";
        private String timeRecordedRange = "";
        private final AppCompatActivity appCompatActivity;
        private Drawable icon;
        private int code = 0;

        public DailyAppEntry(AppCompatActivity appCompatActivity) {
            this.appCompatActivity = appCompatActivity;
        }

        public DailyAppEntry setName(String name) {
            this.name = name;
            return this;
        }

        public DailyAppEntry setIcon(Drawable icon) {
            this.icon = icon;
            return this;
        }

        public DailyAppEntry setTimeSpent(String timeSpent) {
            this.timeSpent = timeSpent;
            return this;
        }

        public DailyAppEntry setTimeRecordedRange(String timeRecordedRange) {
            this.timeRecordedRange = timeRecordedRange;
            return this;
        }

        public int getCode() {
            return code;
        }

        public View getView() {
            return view;
        }

        public DailyAppEntry build(DailyAppEntry lastView) {
            Typeface citrus = ResourcesCompat.getFont(appCompatActivity, R.font.citrus);
            ConstraintLayout cLayout = appCompatActivity.findViewById(R.id.daily_apps);

            int viewId = View.generateViewId();
            int iconId = View.generateViewId();
            int nameId = View.generateViewId();
            int timeId = View.generateViewId();
            int rangeId = View.generateViewId();

            code = (lastView != null)
                    ? (lastView.getCode() < 2)
                    ? lastView.getCode() + 1
                    : 0
                    : 0;

            int frameId;

            if (code == 0)
                frameId = R.drawable.rounded_corner_variant_2;
            else if (code == 1)
                frameId = R.drawable.rounded_corner_variant_3;
            else
                frameId = R.drawable.rounded_corner_variant_4;

            view = new View(appCompatActivity);
            view.setBackground(AppCompatResources.getDrawable(appCompatActivity, frameId));
            view.setId(viewId);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT, 0
            );
            params.topMargin = 30;
            params.leftMargin = 30;
            params.rightMargin = 30;
            if (lastView != null)
                params.topToBottom = lastView.getView().getId();
            else
                params.topToTop = ConstraintSet.PARENT_ID;
            params.startToStart = ConstraintSet.PARENT_ID;
            params.endToEnd = ConstraintSet.PARENT_ID;
            params.bottomToBottom = rangeId;
            cLayout.addView(view, params);

            ImageView icon = new ImageView(appCompatActivity);
            icon.setId(iconId);
            icon.setImageDrawable(this.icon);
            ConstraintLayout.LayoutParams iconParams = new ConstraintLayout.LayoutParams(
                    140, 0
            );
            iconParams.topMargin = 20;
            iconParams.bottomMargin = 40;
            iconParams.startToStart = viewId;
            iconParams.topToTop = nameId;
            iconParams.bottomToBottom = rangeId;
            cLayout.addView(icon, iconParams);

            TextView name = new TextView(appCompatActivity);
            name.setId(nameId);
            name.setText(this.name);
            name.setTypeface(citrus);
            name.setTextSize(20);
            name.setEllipsize(TextUtils.TruncateAt.END);
            name.setSingleLine(true);
            name.setMaxLines(1);
            name.setTextColor(ContextCompat.getColor(appCompatActivity, R.color.opposite));
            ConstraintLayout.LayoutParams nameParams = new ConstraintLayout.LayoutParams(
                    0, ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            nameParams.rightMargin = 20;
            nameParams.topMargin = 5;
            nameParams.bottomToTop = timeId;
            nameParams.startToEnd = iconId;
            nameParams.topToTop = viewId;
            nameParams.endToEnd = viewId;
            cLayout.addView(name, nameParams);

            TextView time = new TextView(appCompatActivity);
            time.setId(timeId);
            time.setText(this.timeSpent);
            time.setTypeface(citrus);
            time.setTextSize(10);
            time.setTextColor(ContextCompat.getColor(appCompatActivity, R.color.opposite));
            ConstraintLayout.LayoutParams timeParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            timeParams.rightMargin = 20;
            timeParams.bottomToTop = rangeId;
            timeParams.startToEnd = iconId;
            timeParams.topToBottom = nameId;
            cLayout.addView(time, timeParams);

            TextView range = new TextView(appCompatActivity);
            range.setId(rangeId);
            range.setText(this.timeRecordedRange);
            range.setTypeface(citrus);
            range.setTextSize(25);
            range.setTextColor(ContextCompat.getColor(appCompatActivity, R.color.opposite));
            ConstraintLayout.LayoutParams rangeParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            rangeParams.topMargin = 10;
            rangeParams.rightMargin = 20;
            rangeParams.bottomMargin = 20;
            rangeParams.startToEnd = iconId;
            rangeParams.topToBottom = timeId;
            rangeParams.bottomToBottom = viewId;
            cLayout.addView(range, rangeParams);

            return this;
        }
    }

    private class WeeklyAppEntry {

        private final AppCompatActivity appCompatActivity;
        private View view;
        private String[] timeSpent;
        private String[] date;
        private Drawable[] icon;
        private int code = 0;

        private WeeklyAppEntry(AppCompatActivity appCompatActivity) {
            this.appCompatActivity = appCompatActivity;
        }

        public WeeklyAppEntry setTimeSpent(String[] timeSpent) {
            this.timeSpent = timeSpent;
            return this;
        }


        public WeeklyAppEntry setDate(String[] date) {
            this.date = date;
            return this;
        }

        public WeeklyAppEntry setIcon(Drawable[] icon) {
            this.icon = icon;
            return this;
        }

        public View getView() {
            return view;
        }

        public int getCode() {
            return code;
        }

        public WeeklyAppEntry build(WeeklyAppEntry lastView) {
            Typeface citrus = ResourcesCompat.getFont(appCompatActivity, R.font.citrus);
            ConstraintLayout cLayout = appCompatActivity.findViewById(R.id.weekly_apps);

            int viewId = View.generateViewId();
            int dateId = View.generateViewId();
            int monthId = View.generateViewId();
            int dayId = View.generateViewId();
            int div1Id = View.generateViewId();
            int div2Id = View.generateViewId();
            int div3Id = View.generateViewId();
            int appTime1 = View.generateViewId();
            int appTime2 = View.generateViewId();
            int appTime3 = View.generateViewId();
            int appIcon1 = View.generateViewId();
            int appIcon2 = View.generateViewId();
            int appIcon3 = View.generateViewId();

            code = (lastView != null)
                    ? (lastView.getCode() < 2)
                    ? lastView.getCode() + 1
                    : 0
                    : 0;

            int frameId;

            if (code == 0)
                frameId = R.drawable.rounded_corner_variant_2;
            else if (code == 1)
                frameId = R.drawable.rounded_corner_variant_3;
            else
                frameId = R.drawable.rounded_corner_variant_4;

            view = new View(appCompatActivity);
            view.setBackground(AppCompatResources.getDrawable(appCompatActivity, frameId));
            view.setId(viewId);
            view.setPadding(0, 0, 0, 30);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT, 0
            );
            params.topMargin = 30;
            params.leftMargin = 30;
            params.rightMargin = 30;
            if (lastView != null)
                params.topToBottom = lastView.getView().getId();
            else
                params.topToTop = ConstraintSet.PARENT_ID;
            params.startToStart = ConstraintSet.PARENT_ID;
            params.endToEnd = ConstraintSet.PARENT_ID;
            params.bottomToBottom = dayId;
            cLayout.addView(view, params);

            TextView dateTextView = new TextView(appCompatActivity);
            dateTextView.setId(dateId);
            dateTextView.setText(this.date[0]);
            dateTextView.setTypeface(citrus);
            dateTextView.setTextSize(20);
            dateTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 2, TypedValue.COMPLEX_UNIT_SP);
            dateTextView.setPadding(20, 10, 0, 0);
            dateTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_START);
            dateTextView.setTextColor(ContextCompat.getColor(appCompatActivity, R.color.opposite));
            ConstraintLayout.LayoutParams dateTextViewParams = new ConstraintLayout.LayoutParams(
                    0, ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            dateTextViewParams.startToStart = viewId;
            dateTextViewParams.endToEnd = viewId;
            dateTextViewParams.topToTop = viewId;
            dateTextViewParams.bottomToTop = monthId;
            dateTextViewParams.topMargin = 10;
            cLayout.addView(dateTextView, dateTextViewParams);

            TextView monthTextView = new TextView(appCompatActivity);
            monthTextView.setId(monthId);
            monthTextView.setText(this.date[1]);
            monthTextView.setTypeface(citrus);
            monthTextView.setTextSize(20);
            monthTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 2, TypedValue.COMPLEX_UNIT_SP);
            monthTextView.setPadding(20, 0, 0, 0);
            monthTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_START);
            monthTextView.setTextColor(ContextCompat.getColor(appCompatActivity, R.color.opposite));
            ConstraintLayout.LayoutParams monthlyTextViewParams = new ConstraintLayout.LayoutParams(
                    0, ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            monthlyTextViewParams.startToStart = viewId;
            monthlyTextViewParams.topToBottom = dateId;
            monthlyTextViewParams.bottomToTop = dayId;
            cLayout.addView(monthTextView, monthlyTextViewParams);

            TextView dayTextView = new TextView(appCompatActivity);
            dayTextView.setId(dayId);
            dayTextView.setText(this.date[2]);
            dayTextView.setTypeface(citrus);
            dayTextView.setTextSize(20);
            dayTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 2, TypedValue.COMPLEX_UNIT_SP);
            dayTextView.setPadding(20, 0, 0, 0);
            dayTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_START);
            dayTextView.setTextColor(ContextCompat.getColor(appCompatActivity, R.color.opposite));
            ConstraintLayout.LayoutParams dayTextViewParams = new ConstraintLayout.LayoutParams(
                    0, ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            dayTextViewParams.bottomMargin = 40;
            dayTextViewParams.startToStart = viewId;
            dayTextViewParams.topToBottom = monthId;
            dayTextViewParams.bottomToBottom = viewId;
            cLayout.addView(dayTextView, dayTextViewParams);

            View div1 = new View(appCompatActivity);
            div1.setId(div1Id);
            div1.setBackgroundColor(ContextCompat.getColor(appCompatActivity, R.color.opposite));
            ConstraintLayout.LayoutParams div1Params = new ConstraintLayout.LayoutParams(
                    3, 0
            );
            div1Params.leftMargin = 180;
            div1Params.startToStart = viewId;
            div1Params.topToTop = viewId;
            div1Params.bottomToBottom = viewId;
            cLayout.addView(div1, div1Params);

            TextView appTime1TextView = new TextView(appCompatActivity);
            appTime1TextView.setId(appTime1);
            appTime1TextView.setText(this.timeSpent[0]);
            appTime1TextView.setTypeface(citrus);
            appTime1TextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            dayTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 20, 2, TypedValue.COMPLEX_UNIT_SP);
            appTime1TextView.setTextColor(ContextCompat.getColor(appCompatActivity, R.color.opposite));
            ConstraintLayout.LayoutParams appTime1Params = new ConstraintLayout.LayoutParams(
                    0, ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            appTime1Params.topMargin = 5;
            appTime1Params.rightMargin = 5;
            appTime1Params.leftMargin = 5;
            appTime1Params.startToEnd = div1Id;
            appTime1Params.topToTop = viewId;
            appTime1Params.endToStart = div2Id;
            cLayout.addView(appTime1TextView, appTime1Params);

            ImageView appIcon1ImageView = new ImageView(appCompatActivity);
            appIcon1ImageView.setId(appIcon1);
            appIcon1ImageView.setImageDrawable(this.icon[0]);
            ConstraintLayout.LayoutParams appIcon1Params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT, 0
            );
            appIcon1Params.rightMargin = 20;
            appIcon1Params.leftMargin = 20;
            appIcon1Params.topMargin = 20;
            appIcon1Params.bottomMargin = 20;
            appIcon1Params.startToEnd = div1Id;
            appIcon1Params.endToStart = div2Id;
            appIcon1Params.topToBottom = appTime1;
            appIcon1Params.bottomToBottom = viewId;
            cLayout.addView(appIcon1ImageView, appIcon1Params);

            View div2 = new View(appCompatActivity);
            div2.setId(div2Id);
            div2.setBackgroundColor(ContextCompat.getColor(appCompatActivity, R.color.opposite));
            ConstraintLayout.LayoutParams div2Params = new ConstraintLayout.LayoutParams(
                    3, 0
            );
            div2Params.startToEnd = div1Id;
            div2Params.topToTop = viewId;
            div2Params.bottomToBottom = viewId;
            div2Params.endToStart = div3Id;
            cLayout.addView(div2, div2Params);

            TextView appTime2TextView = new TextView(appCompatActivity);
            appTime2TextView.setId(appTime2);
            appTime2TextView.setText(this.timeSpent[1]);
            appTime2TextView.setTypeface(citrus);
            appTime2TextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            dayTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 20, 2, TypedValue.COMPLEX_UNIT_SP);
            appTime2TextView.setTextColor(ContextCompat.getColor(appCompatActivity, R.color.opposite));
            ConstraintLayout.LayoutParams appTime2Params = new ConstraintLayout.LayoutParams(
                    0, ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            appTime2Params.topMargin = 5;
            appTime2Params.rightMargin = 5;
            appTime2Params.leftMargin = 5;
            appTime2Params.startToEnd = div2Id;
            appTime2Params.endToStart = div3Id;
            appTime2Params.topToTop = viewId;
            cLayout.addView(appTime2TextView, appTime2Params);

            ImageView appIcon2ImageView = new ImageView(appCompatActivity);
            appIcon2ImageView.setId(appIcon2);
            appIcon2ImageView.setImageDrawable(this.icon[1]);
            ConstraintLayout.LayoutParams appIcon2Params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT, 0
            );
            appIcon2Params.rightMargin = 20;
            appIcon2Params.leftMargin = 20;
            appIcon2Params.topMargin = 20;
            appIcon2Params.bottomMargin = 20;
            appIcon2Params.startToEnd = div2Id;
            appIcon2Params.endToStart = div3Id;
            appIcon2Params.topToBottom = appTime2;
            appIcon2Params.bottomToBottom = viewId;
            cLayout.addView(appIcon2ImageView, appIcon2Params);

            View div3 = new View(appCompatActivity);
            div3.setId(div3Id);
            div3.setBackgroundColor(ContextCompat.getColor(appCompatActivity, R.color.opposite));
            ConstraintLayout.LayoutParams div3Params = new ConstraintLayout.LayoutParams(
                    3, 0
            );
            div3Params.startToEnd = div2Id;
            div3Params.endToEnd = viewId;
            div3Params.topToTop = viewId;
            div3Params.bottomToBottom = viewId;
            cLayout.addView(div3, div3Params);

            TextView appTime3TextView = new TextView(appCompatActivity);
            appTime3TextView.setId(appTime3);
            appTime3TextView.setText(this.timeSpent[2]);
            appTime3TextView.setTypeface(citrus);
            appTime3TextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            dayTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 20, 2, TypedValue.COMPLEX_UNIT_SP);
            appTime3TextView.setTextColor(ContextCompat.getColor(appCompatActivity, R.color.opposite));
            ConstraintLayout.LayoutParams appTime3Params = new ConstraintLayout.LayoutParams(
                    0, ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            appTime3Params.topMargin = 5;
            appTime3Params.rightMargin = 5;
            appTime3Params.leftMargin = 5;
            appTime3Params.startToEnd = div3Id;
            appTime3Params.topToTop = viewId;
            appTime3Params.endToEnd = viewId;
            cLayout.addView(appTime3TextView, appTime3Params);

            ImageView appIcon3ImageView = new ImageView(appCompatActivity);
            appIcon3ImageView.setId(appIcon3);
            appIcon3ImageView.setImageDrawable(this.icon[2]);
            ConstraintLayout.LayoutParams appIcon3Params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT, 0
            );
            appIcon3Params.rightMargin = 20;
            appIcon3Params.leftMargin = 20;
            appIcon3Params.topMargin = 20;
            appIcon3Params.bottomMargin = 20;
            appIcon3Params.startToEnd = div3Id;
            appIcon3Params.endToEnd = viewId;
            appIcon3Params.topToBottom = appTime3;
            appIcon3Params.bottomToBottom = viewId;
            cLayout.addView(appIcon3ImageView, appIcon3Params);

            return this;
        }
    }
}
