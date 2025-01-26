package org.g5.ui.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.g5.overseer.R;

public class LikertQuestion extends AppCompatActivity {

    private static String question = "On a scale of 1-10, How energetic are you feeling today?";
    private static String least = "Exhausted";
    private static String most = "Energetic";
    private static Intent intent;
    private static int qNum = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.likert_scale_popup);

        ask();
    }

    public static void setQuestion(String question) {
        LikertQuestion.question = question;
    }

    public static void setNextIntent(Intent intent) {
        LikertQuestion.intent = intent;
    }

    public static void setLeastAndMost(String least, String most) {
        LikertQuestion.least = least;
        LikertQuestion.most = most;
    }

    public static void setQuestionNumber(int qNum) {
        LikertQuestion.qNum = qNum;
    }

    public void ask() {
        ((TextView) findViewById(R.id.least)).setText(least);
        ((TextView) findViewById(R.id.most)).setText(most);
        ((TextView) findViewById(R.id.question_text)).setText(question);
        findViewById(R.id.button0).setOnClickListener(v -> {
            QuizData.setAnswer(0, qNum);
            startActivity(intent);
        });
        findViewById(R.id.button1).setOnClickListener(v -> {
            QuizData.setAnswer(1, qNum);
            startActivity(intent);
        });
        findViewById(R.id.button2).setOnClickListener(v -> {
            QuizData.setAnswer(2, qNum);
            startActivity(intent);
        });
        findViewById(R.id.button3).setOnClickListener(v -> {
            QuizData.setAnswer(3, qNum);
            startActivity(intent);
        });
        findViewById(R.id.button4).setOnClickListener(v -> {
            QuizData.setAnswer(4, qNum);
            startActivity(intent);
        });
        findViewById(R.id.button5).setOnClickListener(v -> {
            QuizData.setAnswer(5, qNum);
            startActivity(intent);
        });
        findViewById(R.id.button6).setOnClickListener(v -> {
            QuizData.setAnswer(6, qNum);
            startActivity(intent);
        });
        findViewById(R.id.button7).setOnClickListener(v -> {
            QuizData.setAnswer(7, qNum);
            startActivity(intent);
        });
        findViewById(R.id.button8).setOnClickListener(v -> {
            QuizData.setAnswer(8, qNum);
            startActivity(intent);
        });
        findViewById(R.id.button9).setOnClickListener(v -> {
            QuizData.setAnswer(9, qNum);
            startActivity(intent);
        });
        findViewById(R.id.button10).setOnClickListener(v -> {
            QuizData.setAnswer(10, qNum);
            startActivity(intent);
        });
    }
}
