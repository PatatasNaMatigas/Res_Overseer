package org.g5.ui.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.g5.overseer.R;
import org.g5.ui.Login;

public class Q1Start extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_start);

        String text = "Greetings, " + Login.getAccount()[0] + " For the purpose of achieving an extensive overview of your screen-time habits, we encourage you to kindly answer this short query regarding your energy levels honestly. Thank you!";
        ((TextView) findViewById(R.id.question_text)).setText(text);

        LikertQuestion.setQuestion("On a scale of 0-10, How energetic are you feeling today?");
        LikertQuestion.setNextIntent(new Intent(this, Q2Start.class));
        findViewById(R.id.start_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, LikertQuestion.class);
            startActivity(intent);
            finish();
        });
    }
}
