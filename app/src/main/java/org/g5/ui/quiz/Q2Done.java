package org.g5.ui.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.g5.overseer.R;
import org.g5.ui.Login;

public class Q2Done extends AppCompatActivity {

    private String text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_start);
        if (QuizData.getAnswer(1) > 5)
            text = "I'm glad you're ready to take on the day! Go get them " + Login.getAccount()[0] + "!  I'll see you again tomorrow. <3";
        else
            text = "I'm sad to see you so burnt out today :( Take all the rest you need, OK? I'm always looking out for you; I'll check back in tomorrow, " + Login.getAccount()[0] + "!";
        ((TextView) findViewById(R.id.question_text)).setText(text);

        findViewById(R.id.start_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, Q3Start.class);
            startActivity(intent);
            finish();
        });
    }
}
