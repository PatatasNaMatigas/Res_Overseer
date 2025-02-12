package org.g5.ui.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.g5.overseer.R;
import org.g5.ui.Home;

public class Report extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_report);

        switch (QuizData.getAnswer(2)) {
            case 0: {
                ((ImageView) findViewById(R.id.mood)).setImageResource(R.drawable.happy_icon);
                ((TextView) findViewById(R.id.mood_desc)).setText("Happy");
            };
            case 1: {
                ((ImageView) findViewById(R.id.mood)).setImageResource(R.drawable.wow);
                ((TextView) findViewById(R.id.mood_desc)).setText("Wow");
            };
            case 2: {
                ((ImageView) findViewById(R.id.mood)).setImageResource(R.drawable.tired_icon);
                ((TextView) findViewById(R.id.mood_desc)).setText("Sad");
            }
            case 3: {
                ((ImageView) findViewById(R.id.mood)).setImageResource(R.drawable.angry);
                ((TextView) findViewById(R.id.mood_desc)).setText("Angry");
            }
        }

        new QuizData(this);
        try {
            String energy = "Energy level: " + QuizData.getFinalAnswer(0);
            String stress = "Stress level: " + QuizData.getFinalAnswer(1);
            ((TextView) findViewById(R.id.energy_level)).setText(energy);
            ((TextView) findViewById(R.id.screen_time)).setText(stress);
        } catch (NumberFormatException e) {
            startActivity(new Intent(this, Q1Start.class));
        }

        findViewById(R.id.finish).setOnClickListener(v -> {
            startActivity(new Intent(this, Home.class));
            finish();
        });
    }
}
