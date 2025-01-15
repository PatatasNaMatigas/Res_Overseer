package org.g5.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
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

        AppEntry lastView = null;
        int i = 0;
        String topApp = apps.getKeys().get(i);
        Drawable topAppIcon = AppUsage.getAppIcon(this, topApp);
        do {
            String app = apps.getKeys().get(i);
            int[] time = Time.convertSecondsToArray(apps.getEntry(app).getChildren().get(0).getValue1());
            String appTime = Time.formatTime(time);
            lastView = new AppEntry(this)
                    .setName(AppUsage.getAppName(this, app))
                    .setTimeSpent(appTime)
                    .setTimeRecordedRange("1:00pm - 2:00pm")
                    .setIcon(AppUsage.getAppIcon(this, app))
                    .build(lastView);
            i++;
        } while (i < apps.getKeys().size());

        ((ImageView) findViewById(R.id.app_icon)).setImageDrawable(topAppIcon);
        ((TextView) findViewById(R.id.app_name)).setText(AppUsage.getAppName(this, topApp));

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
                int[] time2 = new int[] {
                        morning ? currentTime.getHour() - 12 : currentTime.getHour(),
                        currentTime.getMinute()
                };

                runOnUiThread(() -> {
                    ((TextView) findViewById(R.id.time)).setText(Time.formatClockTime(time2, morning ? "am" : "pm"));
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
    }

    private class AppEntry {

        private View view;
        private String name = "";
        private String timeSpent = "";
        private String timeRecordedRange = "";
        private final AppCompatActivity appCompatActivity;
        private Drawable icon;
        private int code = 0;

        public AppEntry(AppCompatActivity appCompatActivity) {
            this.appCompatActivity = appCompatActivity;
        }

        public AppEntry setName(String name) {
            this.name = name;
            return this;
        }

        public AppEntry setIcon(Drawable icon) {
            this.icon = icon;
            return this;
        }

        public AppEntry setTimeSpent(String timeSpent) {
            this.timeSpent = timeSpent;
            return this;
        }

        public AppEntry setTimeRecordedRange(String timeRecordedRange) {
            this.timeRecordedRange = timeRecordedRange;
            return this;
        }

        public int getCode() {
            return code;
        }

        public View getView() {
            return view;
        }

        public AppEntry build(AppEntry lastView) {
            Typeface citrus = ResourcesCompat.getFont(appCompatActivity, R.font.citrus);
            ConstraintLayout cLayout = appCompatActivity.findViewById(R.id.apps);

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
                    ConstraintLayout.LayoutParams.MATCH_PARENT, 200
            );
            params.topMargin = 30;
            params.leftMargin = 30;
            params.rightMargin = 30;
            if (lastView != null) params.topToBottom = lastView.getView().getId();
            params.startToStart = ConstraintSet.PARENT_ID;
            params.endToEnd = ConstraintSet.PARENT_ID;
            cLayout.addView(view, params);

            ImageView icon = new ImageView(appCompatActivity);
            icon.setId(iconId);
            icon.setImageDrawable(this.icon);
            ConstraintLayout.LayoutParams iconParams = new ConstraintLayout.LayoutParams(
                    140, 0
            );
            iconParams.topMargin = 20;
            iconParams.bottomMargin = 80;
            iconParams.startToStart = viewId;
            iconParams.topToTop = viewId;
            iconParams.bottomToBottom = viewId;
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
            nameParams.topToTop = iconId;
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
}
