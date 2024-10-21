package org.g5.ui.scene;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.g5.core.Data;
import org.g5.overseer.R;

import java.io.IOException;

import me.grantland.widget.AutofitTextView;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Menu extends AppCompatActivity {
    private static AutofitTextView[] dataAvailabilityText;

    private ImageButton popDrawer;
    private static DrawerLayout drawerLayout;

    private static TextView[] dailyAppNames;
    private static TextView[] weeklyAppNames;
    private static TextView[] monthlyAppNames;
    private static TextView[] dailyAppTimes;
    private static TextView[] weeklyAppTimes;
    private static TextView[] monthlyAppTimes;
    private static ImageView[] dailyAppIcons;
    private static ImageView[] weeklyAppIcons;
    private static ImageView[] monthlyAppIcons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_page);

        // Initialize app name arrays
        dailyAppNames = new TextView[]{
                findViewById(R.id.dailyAppName1),
                findViewById(R.id.dailyAppName2),
                findViewById(R.id.dailyAppName3)
        };
        weeklyAppNames = new TextView[]{
                findViewById(R.id.weeklyAppName1),
                findViewById(R.id.weeklyAppName2),
                findViewById(R.id.weeklyAppName3)
        };
        monthlyAppNames = new TextView[]{
                findViewById(R.id.monthlyAppName1),
                findViewById(R.id.monthlyAppName2),
                findViewById(R.id.monthlyAppName3)
        };

        // Initialize app time arrays
        dailyAppTimes = new TextView[]{
                findViewById(R.id.dailyAppTime1),
                findViewById(R.id.dailyAppTime2),
                findViewById(R.id.dailyAppTime3)
        };
        weeklyAppTimes = new TextView[]{
                findViewById(R.id.weeklyAppTime1),
                findViewById(R.id.weeklyAppTime2),
                findViewById(R.id.weeklyAppTime3)
        };
        monthlyAppTimes = new TextView[]{
                findViewById(R.id.monthlyAppTime1),
                findViewById(R.id.monthlyAppTime2),
                findViewById(R.id.monthlyAppTime3)
        };

        // Initialize app icon arrays
        dailyAppIcons = new ImageView[]{
                findViewById(R.id.dailyApp1),
                findViewById(R.id.dailyApp2),
                findViewById(R.id.dailyApp3)
        };
        weeklyAppIcons = new ImageView[]{
                findViewById(R.id.weeklyApp1),
                findViewById(R.id.weeklyApp2),
                findViewById(R.id.weeklyApp3)
        };
        monthlyAppIcons = new ImageView[]{
                findViewById(R.id.monthlyApp1),
                findViewById(R.id.monthlyApp2),
                findViewById(R.id.monthlyApp3)
        };

        dataAvailabilityText = new AutofitTextView[]{
                findViewById(R.id.daily_no_data),
                findViewById(R.id.weekly_no_data),
                findViewById(R.id.monthly_no_data),
        };
        findViewById(R.id.reset).setOnClickListener(view -> {
            try {
                Data.deleteDailyFile();
                Data.deleteWeeklyFile();
                Data.deleteMonthlyFile();
            } catch (IOException e) {}
        });

        popDrawer = findViewById(R.id.popDrawer);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {}

            @Override
            public void onDrawerOpened(View drawerView) {}

            @Override
            public void onDrawerClosed(View drawerView) {
                drawerLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onDrawerStateChanged(int newState) {}
        });
        drawerLayout.setVisibility(View.INVISIBLE);

        popDrawer.setOnClickListener(view -> {
            Log.d("@Menu.java!", "clicked drawer");
            drawerLayout.openDrawer(GravityCompat.START);
            drawerLayout.setVisibility(View.VISIBLE);
        });

        // Set input filter for pet name EditText
        ((EditText) findViewById(R.id.pet_name)).setFilters(new InputFilter[]{
                new InputFilter.AllCaps()
        });
    }

    public static void setAppNameDaily(String[] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null) return;
            Log.d("@Menu.java", "app[i]: " + app[i]);
            dailyAppNames[i].setText(app[i]);
        }
    }

    public static void setAppNameWeekly(String[] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null) return;
            weeklyAppNames[i].setText(app[i]);
        }
    }

    public static void setAppNameMonthly(String[] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null) return;
            monthlyAppNames[i].setText(app[i]);
        }
    }

    public static void setAppTimeDaily(String[][] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null || app[i][1] == null) return; // Check if app[i][1] is not null
            dailyAppTimes[i].setText(app[i][1]);
        }
    }

    public static void setAppTimeWeekly(String[][] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null || app[i][1] == null) return; // Check if app[i][1] is not null
            weeklyAppTimes[i].setText(app[i][1]);
        }
    }

    public static void setAppTimeMonthly(String[][] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null || app[i][1] == null) return; // Check if app[i][1] is not null
            monthlyAppTimes[i].setText(app[i][1]);
        }
    }

    public static void setAppIconDaily(Drawable[] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null) return;
            dailyAppIcons[i].setImageDrawable(app[i]);
        }
    }

    public static void setAppIconWeekly(Drawable[] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null) return;
            weeklyAppIcons[i].setImageDrawable(app[i]);
        }
    }

    public static void setAppIconMonthly(Drawable[] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null) return;
            monthlyAppIcons[i].setImageDrawable(app[i]);
        }
    }


    public static void noDataDaily(boolean noData) {
        for (AutofitTextView textView : dataAvailabilityText) {
            textView.setVisibility(noData ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public static void noDataWeekly(boolean noData) {
        for (AutofitTextView textView : dataAvailabilityText) {
            textView.setVisibility(noData ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public static void noDataMonthly(boolean noData) {
        for (AutofitTextView textView : dataAvailabilityText) {
            textView.setVisibility(noData ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
