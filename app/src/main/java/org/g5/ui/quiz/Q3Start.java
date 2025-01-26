package org.g5.ui.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.g5.overseer.R;

public class Q3Start extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_mood);

        findViewById(R.id.button_happy).setOnClickListener(v -> {
            QuizData.setAnswer(0, 2);
            Intent intent = new Intent(this, Q3Done.class);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.button_wow).setOnClickListener(v -> {
            QuizData.setAnswer(1, 2);
            Intent intent = new Intent(this, Q3Done.class);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.button_sad).setOnClickListener(v -> {
            QuizData.setAnswer(2, 2);
            Intent intent = new Intent(this, Q3Done.class);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.button_angry).setOnClickListener(v -> {
            QuizData.setAnswer(3, 2);
            Intent intent = new Intent(this, Q3Done.class);
            startActivity(intent);
            finish();
        });
    }
}
