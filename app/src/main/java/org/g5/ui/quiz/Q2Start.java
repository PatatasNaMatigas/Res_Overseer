package org.g5.ui.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.g5.overseer.R;
import org.g5.ui.Login;

public class Q2Start extends AppCompatActivity {

    private boolean answered = false;
    private String text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_start);
        if (QuizData.getAnswer(0) > 5)
            text = "I'm glad you're ready to take on the day! Go get them, User!  I'll see you again tomorrow. <3";
        else
            text = "I'm sad to see you so burnt out today :( Take all the rest you need, OK? I'm always looking out for you; I'll check back in tomorrow, " + Login.getAccount()[0] + "!";
        ((TextView) findViewById(R.id.question_text)).setText(text);

        LikertQuestion.setQuestion("On a scale of 0-10, How stressed are you feeling right now?");
        LikertQuestion.setQuestionNumber(1);
        LikertQuestion.setNextIntent(new Intent(this, Q2Done.class));
        LikertQuestion.setLeastAndMost("Calm", "Stressed");
        findViewById(R.id.start_button).setOnClickListener(v -> {
            if (answered) {
                Intent intent = new Intent(this, LikertQuestion.class);
                startActivity(intent);
                finish();
            } else {
                text = "Greetings, " + Login.getAccount()[0] + " For the purpose of achieving an extensive overview of your screen-time habits, we encourage you to kindly answer this short query regarding your stress levels honestly. Thank you!";
                ((TextView) findViewById(R.id.question_text)).setText(text);
            }
            answered = true;
        });
    }
}
