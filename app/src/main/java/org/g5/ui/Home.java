package org.g5.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import org.g5.core.AppUsage;
import org.g5.core.Data;
import org.g5.overseer.R;
import org.g5.pet.Pet;
import org.g5.util.LineWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import me.grantland.widget.AutofitTextView;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Home extends AppCompatActivity {
    private static AutofitTextView[] dataAvailabilityText;

    private ImageButton popDrawer;
    private static TextView[] dailyAppNames;
    private static TextView[] weeklyAppNames;
    private static TextView[] monthlyAppNames;
    private static TextView[] dailyAppTimes;
    private static TextView[] weeklyAppTimes;
    private static TextView[] monthlyAppTimes;
    private static ImageView[] dailyAppIcons;
    private static ImageView[] weeklyAppIcons;
    private static ImageView[] monthlyAppIcons;

    private static Pet pet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

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

        File petData = new File(getFilesDir(), "petData.txt");
        try {
            if (!petData.createNewFile()) {
                petData.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LineWriter lineWriter = new LineWriter(petData);

        EditText petName = findViewById(R.id.petName);
        petName.setText(lineWriter.getLine(0));
        petName.setOnFocusChangeListener((v, hasFocus) -> {
            petName.setCursorVisible(hasFocus);
        });
        petName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Called before the text is changed

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int editTextWidth = petName.getWidth();
                float currentTextWidth = petName.getPaint().measureText(s.toString());

                if (currentTextWidth >= editTextWidth) {
                    petName.removeTextChangedListener(this);
                    String trimmedText = s.toString();
                    while (petName.getPaint().measureText(trimmedText) > editTextWidth && !trimmedText.isEmpty()) {
                        trimmedText = trimmedText.substring(0, trimmedText.length() - 1);
                    }
                    petName.setText(trimmedText);
                    petName.setSelection(trimmedText.length());
                    petName.addTextChangedListener(this);

                    lineWriter.writeLine(trimmedText, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Called after the text is changed
            }
        });

        {
            if ((Login.getAccount()[0].equals("DebugVersion") || Login.getAccount()[0].equals("Developer")) && Login.getAccount()[1].equals("psvm")) {
                findViewById(R.id.reset).setOnClickListener(view -> {
                    try {
                        Data.deleteDailyFile();
                        Data.deleteWeeklyFile();
                        Data.deleteMonthlyFile();

                        StringBuilder logData = new StringBuilder();
                        File[] files = AppUsage.getFiles();

                        logData.append("[]=======LOG START=======[]\n\n");
                        Log.d("Menu.java!", "[]=======LOG START=======[]");

                        for (int i = 0; i < 3; i++) {
                            switch (i) {
                                case 0:
                                    logData.append("[]=======DAILY=======[]\n");
                                    Log.d("Menu.java!", "[]=======DAILY=======[]");
                                    break;
                                case 1:
                                    logData.append("[]=======WEEKLY=======[]\n");
                                    Log.d("Menu.java!", "[]=======DAILY=======[]");
                                    break;
                                case 2:
                                    logData.append("[]=======MONTHLY=======[]\n");
                                    Log.d("Menu.java!", "[]=======DAILY=======[]");
                                    break;
                            }
                            File file = files[i];
                            BufferedReader reader;
                            try {
                                reader = new BufferedReader(new FileReader(file));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    logData.append(file.getName()).append(" ").append(line).append("\n");
                                    Log.d("Menu.java!", file.getName() + " " + line);
                                }
                                reader.close();
                            } catch (IOException e) {
                                logData.append("Error reading file: ").append(file.getName()).append("\n");
                            }
                        }

                        // Set the generated log data to the TextView
                        findViewById(R.id.scrollViewDebug).setVisibility(View.INVISIBLE);
                        TextView logTextView = findViewById(R.id.logs);
                        logTextView.setText(logData.toString());
                        AppUsage.clearData();
                    } catch (IOException e) {}
                });
                findViewById(R.id.logData).setOnClickListener(view -> {
                    StringBuilder logData = new StringBuilder();
                    File[] files = AppUsage.getFiles();

                    logData.append("[]=======LOG START=======[]\n\n");
                    Log.d("Menu.java!", "[]=======LOG START=======[]");

                    for (int i = 0; i < 3; i++) {
                        switch (i) {
                            case 0:
                                logData.append("[]=======DAILY=======[]\n");
                                Log.d("Menu.java!", "[]=======DAILY=======[]");
                                break;
                            case 1:
                                logData.append("[]=======WEEKLY=======[]\n");
                                Log.d("Menu.java!", "[]=======DAILY=======[]");
                                break;
                            case 2:
                                logData.append("[]=======MONTHLY=======[]\n");
                                Log.d("Menu.java!", "[]=======DAILY=======[]");
                                break;
                        }
                        File file = files[i];
                        BufferedReader reader;
                        try {
                            reader = new BufferedReader(new FileReader(file));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                logData.append(file.getName()).append(" ").append(line).append("\n");
                                Log.d("Menu.java!", file.getName() + " " + line);
                            }
                            reader.close();
                        } catch (IOException e) {
                            logData.append("Error reading file: ").append(file.getName()).append("\n");
                        }
                    }

                    findViewById(R.id.scrollViewDebug).setVisibility(
                            (findViewById(R.id.scrollViewDebug).getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE)
                    );
                    TextView logTextView = findViewById(R.id.logs);
                    logTextView.setText(logData.toString());
                });

            } else {
                findViewById(R.id.reset).setVisibility(View.INVISIBLE);
                findViewById(R.id.logData).setVisibility(View.INVISIBLE);
            }
        }

        pet = new Pet(this);

        ConstraintLayout constraintLayout = findViewById(R.id.menu_layout);

        Button exitDrawer = findViewById(R.id.exit_drawer);

        popDrawer = findViewById(R.id.popDrawer);
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

            constraintSet.clear(R.id.exit_drawer, ConstraintSet.START);
            constraintSet.clear(R.id.exit_drawer, ConstraintSet.END);
            constraintSet.connect(R.id.exit_drawer, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END);
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        findViewById(R.id.home).setOnClickListener(view -> {
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

            constraintSet.clear(R.id.exit_drawer, ConstraintSet.START);
            constraintSet.clear(R.id.exit_drawer, ConstraintSet.END);
            constraintSet.connect(R.id.exit_drawer, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END);
            TransitionManager.beginDelayedTransition(constraintLayout);
            constraintSet.applyTo(constraintLayout);
        });

        // Set input filter for pet name EditText
        ((EditText) findViewById(R.id.petName)).setFilters(new InputFilter[]{
                new InputFilter.AllCaps()
        });

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            // Dark mode
            ((ImageView) findViewById(R.id.pet)).setImageResource(R.drawable.pet1);
        } else {
            // Light mode
            ((ImageView) findViewById(R.id.pet)).setImageResource(R.drawable.pet2);
        }

        findViewById(R.id.summary).setOnClickListener(view -> {
            startActivity(new Intent(this, Summary.class));
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int nightModeFlags = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            ((ImageView) findViewById(R.id.pet)).setImageResource(R.drawable.pet1);
        } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
            ((ImageView) findViewById(R.id.pet)).setImageResource(R.drawable.pet2);
        }
    }


    public static void setAppNameDaily(String[] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null) return;
            try {
                dailyAppNames[i].setText(app[i]);
            } catch (NullPointerException e) {
                return;
            }
        }
    }

    public static void setAppNameWeekly(String[] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            try {
                weeklyAppNames[i].setText(app[i]);
            } catch (NullPointerException e) {
                return;
            }
        }
    }

    public static void setAppNameMonthly(String[] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            try {
                monthlyAppNames[i].setText(app[i]);
            } catch (NullPointerException e) {
                return;
            }
        }
    }

    public static void setAppTimeDaily(String[][] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null || app[i][1] == null) return; // Check if app[i][1] is not null
            try {
                dailyAppTimes[i].setText(app[i][1]);
            } catch (NullPointerException e) {
                return;
            }
        }
    }

    public static void setAppTimeWeekly(String[][] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null || app[i][1] == null) return; // Check if app[i][1] is not null
            try {
                weeklyAppTimes[i].setText(app[i][1]);
            } catch (NullPointerException e) {
                return;
            }
        }
    }

    public static void setAppTimeMonthly(String[][] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null || app[i][1] == null) return; // Check if app[i][1] is not null
            try {
                monthlyAppTimes[i].setText(app[i][1]);
            } catch (NullPointerException e) {
                return;
            }
        }
    }

    public static void setAppIconDaily(Drawable[] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null) return;
            try {
                dailyAppIcons[i].setImageDrawable(app[i]);
            } catch (NullPointerException e) {
                return;
            }
        }
    }

    public static void setAppIconWeekly(Drawable[] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null) return;
            try {
                weeklyAppIcons[i].setImageDrawable(app[i]);
            } catch (NullPointerException e) {
                return;
            }
        }
    }

    public static void setAppIconMonthly(Drawable[] app) {
        if (app == null) return;
        for (int i = 0; i < app.length; i++) {
            if (app[i] == null) return;
            try {
                monthlyAppIcons[i].setImageDrawable(app[i]);
            } catch (NullPointerException e) {
                return;
            }
        }
    }

    public static void noDataDaily(boolean noData) {
        if (dataAvailabilityText != null)
            dataAvailabilityText[0].setVisibility(noData ? View.VISIBLE : View.INVISIBLE);
    }

    public static void noDataWeekly(boolean noData) {
        if (dataAvailabilityText != null)
            dataAvailabilityText[1].setVisibility(noData ? View.VISIBLE : View.INVISIBLE);
    }

    public static void noDataMonthly(boolean noData) {
        if (dataAvailabilityText != null)
            dataAvailabilityText[2].setVisibility(noData ? View.VISIBLE : View.INVISIBLE);
    }

    public static void checkForNotif(String appName, int appTime) {
        if (pet != null)
            pet.start(appName, appTime);
    }
}
